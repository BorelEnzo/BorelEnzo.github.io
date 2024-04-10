---
layout: posts
title:  CVE-2024-27937 - CVE-2024-27930 - Walkthrough
date:   2024-02-29
categories: stuff
---

I was recently tasked with auditing the application [GLPI](https://github.com/glpi-project), a few days after its latest release (10.0.12 at the time of writing). The latter stands for _Gestionnaire Libre de Parc Informatique_, and is a french software solution meant to manage various assets, such as machines, software, or licenses.

Since GLPI is a free piece of software, I decided to install my own local instance, and I have to admit, it was really straightforward. After a few minutes, I had it running on a fresh Ubuntu server VM. After a few days of research, a pair of vulnerabilities finally came to light: as a low-privileged user, it was possible to take over the super-admin's account.

## A first oddity

By using the built-in `post-only` account, features are quite limited. This account is only allowed to create tickets, but is not supposed to enumerate others users nor to see advanced settings.

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Create ticket in GLPI" src="/assets/res/stuff/glpi/glpi_new_ticket.png">

However, one can notice a first interesting thing by creating a new ticket, and trying to add watchers: an AJAX call is made to `/ajax/actors.php`, returning details about existing users and groups (while the `Users` menu was not available to this user) ...

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Dropdown with existing users" src="/assets/res/stuff/glpi/glpi_enum_users.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get users in tenant 0" src="/assets/res/stuff/glpi/glpi_tenant0.png">

One can see that the query contains a parameter named `entity_restrict`. If the application is used to manage multiple entities (kind of tenants), it can be abused to enumerate the users of another one, which is not supposed to be permitted. For instance, resending the same request with the `entity_restrict` equal to 1 would return additional users, belonging to another entity. This issue is tracked as CVE-2024-27937.

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get users of tenant 1" src="/assets/res/stuff/glpi/glpi_tenant1.png">

By taking a look at the source code of `/ajax/actors.php`, one can see that such AJAX call would eventually trigger the function `User::getSqlSearchResult`, but only the following fields are returned to the user:

```php
$results[] = [
    'id'                => "User_$ID",
    'text'              => $text,
    'title'             => sprintf(__('%1$s - %2$s'), $text, $user['name']),
    'itemtype'          => "User",
    'items_id'          => $ID,
    'use_notification'  => strlen($user['default_email'] ?? "") > 0 ? 1 : 0,
    'default_email'     => $user['default_email'],
    'alternative_email' => '',
];
```

Interesting, because one could therefore leak personal details of other users, but not sufficient :grin:

## Dropdowns

GLPI creates numerous relationships between objects, with `User`s creating `Ticket`s about `Computer`s, pieces of `Software`, etc. The application therefore heavily relies on _dropdown list_ widgets to make it easy, the latter being populated based on the requested object types and user's rights. For instance, the `/ajax` folder contains the following scripts, meant to build such widgets:
* `getDropdownFindNum.php`
* `dropdownMassiveActionAddActor.php`
* `dropdownProjectTaskTicket.php`
* `getDropdownUsers.php`
* `dropdownValidator.php`
* `dropdownShowIPNetwork.php`
* `dropdownNotificationEvent.php`
* `dropdownMassiveActionAuthMethods.php`
* `dropdownConnectNetworkPort.php`
* `dropdownConnectNetworkPortDeviceType.php`
* `getDropdownConnect.php`
* `dropdownMassiveAction.php`
* `getDropdownNumber.php`
* `dropdownAllItems.php`
* `dropdownMassiveActionField.php`
* `dropdownUnicityFields.php`
* `getShareDashboardDropdownValue.php`
* `dropdownSoftwareLicense.php`
* `dropdownInstallVersion.php`
* `dropdownMassiveActionOs.php`
* `dropdownTrackingDeviceType.php`
* `dropdownTicketCategories.php`
* `dropdownFieldsBlacklist.php`
* `dropdownNotificationTemplate.php`
* `dropdownTypeCertificates.php`
* `dropdownLocation.php`
* `getAbstractRightDropdownValue.php`
* `dropdownValuesBlacklist.php`
* `dropdownMassiveActionAddValidator.php`
* `dropdownDelegationUsers.php`
* `dropdownConnect.php`
* `getDropdownValue.php`
* `dropdownRubDocument.php`
* `dropdownItilActors.php`

These scripts call routines of the class `Dropdown`, which is meant to _Print out an HTML "&lt;select&gt;" for a dropdown with preselected value_, as said by a comment in the aforementionned class. Normally, these routines are supposed to only display specific types of objects, for example (in `dropdownLocation.php`) this one with `Location` instances:

```php
echo Location::dropdown([
    'value'        => $locations_id,
    'entity'       => $entities_id,
    'entity_sons'  => $is_recursive,
]);
```

But what if we could ask for a dropdown containing arbitrary objects ?

## A suspicious parameter

GLPI offers a lot of menus, and I quickly got overwhelmed by the huge amount of requests that my browser sent. However, a request caught my attention, while trying to link a ticket to another one:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Link a ticket to another" src="/assets/res/stuff/glpi/glpi_link_ticket.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Request to enumerate the tickets" src="/assets/res/stuff/glpi/glpi_link_ticket1.png">

This request seemed to return a set of objects (`Ticket`) with some parameters. Interestingly enough, the field `id`, specified with the parameter `displaywith`, is returned in the answer. It really sounds like an SQL-query-as-a-service, so what if one asks for the item type `User`, displayed with the field `password` ?

The file `/ajax/getdropdownValue.php` is as follows, forwarding the POST'ed data to `Dropdown::getDropdownValue`:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Code of getdropdownValue.php" src="/assets/res/stuff/glpi/glpi_getdropdownvalue.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Code of getdropdownValue.php - 1" src="/assets/res/stuff/glpi/glpi_dropdown_1.png">


A first check against what is named an _IDOR token_ is made (line 2'619), and if everything goes well, a new object is created based on the advertised `itemtype`. If the parameter `displaywith` exists, then it persists inside the `$post` array, and instructs the function to include specific additional fields in the response. By reading the code thoroughly, one can realise that the POST'ed data are used to build the query without checking the access rights regarding the expected table. So normally, I should have been able to enumerate the table `glpi_users` and ask for any field in this table, but obvisouly, there was a catch ...

_N.B. the value of `itemtype` is not exactly the name of a DB table. It is supposed to be the name of a PHP class, inherited from the class `CommonDBTM`. Each child class is supposed to have its matching DB table, therefore one should set it to `User` (the PHP class) to read from the table `glpi_users` (or equivalent)._

## A strange protection mechanism

To make it work, the request must be authenticated, and this is done with the cookies. Moreover, AJAX calls made through POST need to contain the header `X-Glpi-Csrf-Token`. The appropriate value can be found in the `meta` tag `glpi:csrf_token`, in HTML pages returned by the application:

```html
<meta property="glpi:csrf_token" content="1fed5029e3d6a73af3352791f7ecff41e79c7644cbd89cf41d1b3e46e6ca99b6">
```

One important thing to notice is that dropdown lists are not directly written inside the HTML pages returned by the server. Instead, a piece of JavaScript code is returned, that would retrieve on-demand the list of the objects to display, and populate the dropdown lists. Therefore, an additional parameter named `_idor_token` must be put in the request body sent by JavaScript, in order to prevent from unauthorised access (hence the call to `validatIDOR`). The idea is that an _IDOR token_ is tied to an object type, a set of rights, an entity, and for a limited time. Multiple `_idor_tokens` can be found in a page (one per dropdown list), and for instance, a first one is used to get the list of the tickets, another one for the list of the users, and another one for the list of the machines.

Different ways exist to obtain an IDOR token, and they can be found by searching for calls to `Session::getNewIDORToken` in the source code. One of them is done in `User::dropdown`, which can be called from `/ajax/dropdownValidator.php`.

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Code to get token for users" src="/assets/res/stuff/glpi/glpi_get_token_user.png">

In the response, one can see that an IDOR token to enumerate users is returned. However, querying the endpoint `/ajax/getdropdownValue.php` by specifying the parameter `_idor_token` and `itemtype` does not work, and returns a blank page. By debugging the programme, one can realise that it is because of a failing check during the IDOR token validation.

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get a first IDOR token" src="/assets/res/stuff/glpi/glpi_idor_1.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Use first IDOR token, not working" src="/assets/res/stuff/glpi/glpi_idor_2.png">

To investigate and get clues about what was going wrong, I added a few lines in the file `src/Session.php` to print the current state of each IDOR token (not the cleanest way to debug, but easier and sufficient enough):

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Debugging Session::validateIDOR" src="/assets/res/stuff/glpi/glpi_idor_3.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Debug log file" src="/assets/res/stuff/glpi/glpi_idor_4.png">

By taking a look at the debug log, one can realise that the expected token is tied to an associative array with the keys `itemtype`, `right` and `entity_restrict` while our token only contains `itemtype`. I then asked for a new token, with the expected claims:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Obtain complete token" src="/assets/res/stuff/glpi/glpi_idor_5.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Enumerate users with valid toen" src="/assets/res/stuff/glpi/glpi_idor_6.png">

_N.B: note that the names of the POST parameters and the keys of the expected elements in the array of IDOR tokens are not the same, but this is how it works. To have an appropriate `entity_restrict`, the POST parameter to set in the first request is `entity`._

Now, by setting the parameter `displaywith[]=password` (can be specified multiple times), one can therefore extract all the hashes from the database. However, there can be hashed with bcrypt, and if they are strong enough, they might be uncrackable.

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get passwords hashes" src="/assets/res/stuff/glpi/glpi_get_passwords.png">

Yay :smile:

Another way to abuse such feature and compromise an account is to extract email addresses (first vulnerability), and ask for a password reset. The token would be stored as `password_forget_token` in the DB. The emails addresses are not stored in the same table, but as we saw at the beginning while requesting `/ajax/actors.php`, they can be easily obtained.

## Ask for any table

Since IDOR tokens are tied to an item type, one needs to obtain a valid one for each DB table. So far, it was possible because the script `/ajax/dropdownValidator.php` could return a piece of code to retrieve the list of the users, but what if we want to dump the content of the table `glpi_configs` ? A more generic way to obtain tokens exists, because of an arbitrary object creation. Let's take `/ajax/cable.php` as an example (not the only one):

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Source code /ajax/cable.php to get any token" src="/assets/res/stuff/glpi/glpi_get_token_any.png">

The item type is taken right from `$_POST["itemtype"]`, and the static routine `dropdown` is called on the advertised class, meaning that any child of `CommonDBTM` could be enumerated. To begin with, a request can be sent to `/ajax/cable.php`, specifying the appropriate `action` and `itemtype` parameters:


<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get an IDOR token for user, arbitrary - 1 " src="/assets/res/stuff/glpi/glpi_any_token_for_user1.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get an IDOR token for user, arbitrary - 2" src="/assets/res/stuff/glpi/glpi_any_token_for_user2.png">

In the first request, one can see that application returns a token alongside the attributes `right=id` and `entity_restrict=-1`. In the second request, these attributes must match, otherwise nothing is returned.

Now, let's retry it with the table `glpi_configs`:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get an IDOR token for configs -1 " src="/assets/res/stuff/glpi/glpi_any_token_for_configs1.png">

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="Get an IDOR token for configs - 2" src="/assets/res/stuff/glpi/glpi_any_token_for_configs2.png">

This issue is tracked as CVE-2024-27930.

## So what ?

In a real world scenario, an attacker might try to enumerate the email addresses to find the admin's one, ask for a password reset link, and abuse the second vulnerability to recover it. Once connected as super admin, there is a plenty of interesting things to do. I guess that one of the most straightforward ways to RCE is to add a plugin, like [shellcommands](https://github.com/InfotelGLPI/shellcommands).

The issues were reported to the vendor and patched in a few days. Let's note that the description for CVE-2024-27930 was registered as:

>GLPI is a Free Asset and IT Management Software package, Data center management, ITIL Service Desk, licenses tracking and software auditing. An authenticated user can access sensitive fields data from items on which he has read access. This issue has been patched in version 10.0.13.

This is not entirely true, since an authenticated user can access any type of object (child of `CommonDBTM`), regardless of their privileges.