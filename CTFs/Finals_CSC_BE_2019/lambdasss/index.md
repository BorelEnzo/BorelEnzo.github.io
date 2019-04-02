## Lambdasss 1 & 2 - Misc (120 & 210 pts)

### [~$ cd ..](../)

I really love python, and I have to admit, I also love putting lambdas in my scripts ! However, not this kind of lambdas ...

## Lambdasss 1

>Guess the flag, and let the nested lambdas judge

We are given a file containing the following nested lambdas:

> ```python
>#!/usr/bin/env python3
># -*- coding: utf-8 -*-
>
>print("Input flag: ", end="")
>if (lambda a:(lambda b:(lambda c:(lambda d:(lambda e:(lambda f:(lambda g:(lambda h:h)(g == b))(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2))([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]))(a()))(input):
>    print("Correct!!")
>else:
>    print("Try again :((")
> ```

This first one was that really difficult and one case easily guess that each number of the array is divided by 2, and that `chr` function takes the result as argument. All chars are then concatenated using `join`, and a comparison is done.
We had then to do the same:

> ```python
>print ''.join(chr(x//2) for x in [134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250])
>CSR{n0w_d0_1h3_h4rd_0n3!!}
> ```

### More detailed answer

Even if we solved this challenge very fast, I decided to provide a more detailed answer. I will describe each step, taking lambdas one by one:

* In the middle, we can see this really simple lambda: `lambda h:h`, returning what is passed as argument. It can be then removed:

> ```python
> if (lambda a:(lambda b:(lambda c:(lambda d:(lambda e:(lambda f:(lambda g:g == b)(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2))([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]))(a()))(input):
> ```

* Let's take now the first lambda, on the left. We can see on the right that it's been called with `input` as parameter. Let's simplify by rewriting in this way:

> ```python
>mainlambda = lambda b:(lambda c:(lambda d:(lambda e:(lambda f:(lambda g:g == b)(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2))([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250])
>if (lambda a:mainlambda(a()))(input):
> ```

or, even better:

> ```python
>mainlambda = lambda b:(lambda c:(lambda d:(lambda e:(lambda f:(lambda g:g == b)(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2))([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250])
>if mainlambda(input()):
> ```

* We can now see that the argument `b` of `mainlambda` is compared against the value of `g`, being equal to `f(e)`. Indeed, we can remove the first lambda of `mainlambda`, and rewrite it as follows:

> ```python
>mainlambda = (lambda c:(lambda d:(lambda e:(lambda f:(lambda g:g == input())(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2))([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250])
>if mainlambda:
> ```

* However, `mainlambda` is no more a lambda here, so let's rewrite it without the parenthesis, by passing the array as argument:

> ```python
>mainlambda = lambda c:(lambda d:(lambda e:(lambda f:(lambda g:g == input())(f(e)))(''.join))([chr(d(y))for y in c]))(lambda x:x//2)
>if mainlambda([134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]):
> ...
> ```

* We can now turn the array of numbers into a constant and use `mainlambda` as a boolean:

> ```python
>array = [134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]
>mainlambda = (lambda d:(lambda e:(lambda f:(lambda g:g == input())(f(e)))(''.join))([chr(d(y))for y in array]))(lambda x:x//2)
> ```

* Let's rewrite now the lambda on the right:

> ```python
>def divideme(x):
>	return x//2
>array = [134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]
>mainlambda = (lambda d:(lambda e:(lambda f:(lambda g:g == input())(f(e)))(''.join))([chr(d(y))for y in array]))(divideme)
> ```

or

> ```python
>def divideme(x):
>	return x//2
>array = [134,166,164,246,220,96,238,190,200,96,190,98,208,102,190,208,104,228,200,190,96,220,102,66,66,250]
>mainlambda = (lambda e:(lambda f:(lambda g:g == input())(f(e)))(''.join))([chr(divideme(y))for y in array])
> ```

* Since `array` is a constant we can immediately compute the result of `[chr(divideme(y))for y in array]`, which is: `['C', 'S', 'R', '{', 'n', '0', 'w', '_', 'd', '0', '_', '1', 'h', '3', '_', 'h', '4', 'r', 'd', '_', '0', 'n', '3', '!', '!', '}']`

We can now easily guess that these characters are `join`ed and compared against the input !

## Lambdasss 2

>The lambdas have returned, grown stronger than ever. Subdue them and conquer the flag!

We didn't actually solve this challenge during the contest, but found the solution a couple of days later. We were given another Python script with the following nested lambdas:

> ```python
>#!/usr/bin/env python3
># -*- coding: utf-8 -*-
>
>print("Input flag: ", end="")
>if (lambda a:(lambda b:(lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:(lambda e:(lambda h:(lambda m:m)(h(c)))(e.__eq__))(h(e)))(a.list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(a.sum(f)))(a.range(f*f)))(g(f)))(a.int))(f(c[16])))(a.chr))(d(e,b)))(a.ord))(a.map))(b()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]))(a.input))(__builtins__):
>    print("Correct!!")
>else:
>    print("Try again :((")
> ```

The principle we will use it basically always the same. As we have a construction like this:

> ```python
>(lambda x:(lambda y:foo(y))(z))
> ```

we can try to integrate to rightmost argument ( `z` in this example) in the lambda on its left, and keep only the right part of the lambda:

> ```python
>(lambda x:foo(z))
> ```

As we previously did, we can remove `(lambda m:m)`:

> ```python
>if (lambda a:(lambda b:(lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:(lambda e:(lambda h:h(c))(e.__eq__))(h(e)))(a.list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(a.sum(f)))(a.range(f*f)))(g(f)))(a.int))(f(c[16])))(a.chr))(d(e,b)))(a.ord))(a.map))(b()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]))(a.input))(__builtins__):
> ```

The term `a` appears quite often, and is actually `__builtins__`, used to call some built-in routines such as `input`, `sum`, `int`, etc. Totally useless, so let's remove it:

> ```python
>if (lambda b:(lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:(lambda e:(lambda h:h(c))(e.__eq__))(h(e)))(list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(b()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]))(input):
> ```

The first lambda is called by passing `input` as parameter. We can simplify by removing this one and replace the argument `b` by `input()`. However, the third lambda takes an argument named `b` as well, then the correct `b` to replace is the callable one (just before the array):

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:(lambda e:(lambda h:h(c))(e.__eq__))(h(e)))(list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

The central lambda `(lambda e:(lambda h:h(c))(e.__eq__))` could then be written as : `(lambda e:e.__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:(lambda e:e.__eq__(c))(h(e)))(list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

It becomes here a little bit more difficult because some lambdas share the same argument name. If we look at the newly simplified central lambda, we can then rewrite `(lambda h:(lambda e:e.__eq__(c))(h(e)))` in this way : `(lambda h:h(e).__eq__(c))`.

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:(lambda h:h(e).__eq__(c))(list))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Let's continue our reduction by rewriting `(lambda e:(lambda h:h(e).__eq__(c))(list))` in this way: `(lambda e:list(e).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:(lambda e:list(e).__eq__(c))(d(h,e)))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

The new "central" reduced lambda is now `(lambda h:(lambda e:list(e).__eq__(c))(d(h,e)))`, and can be also simplified by writing: `(lambda h:list(d(h,e)).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:(lambda h:list(d(h,e)).__eq__(c))(lambda h:g(f,h)))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Just before the `__xor__` we have the following ugly lambda `(lambda g:(lambda h:list(d(h,e)).__eq__(c))(lambda h:g(f,h)))` with an argument named `g`, taking as argument another lambda ( the one with argument `h` ). To simplify, the principle remains the same: we remove the inner `lambda` and replace the argument outside the parentheses. It gives then `(lambda g:list(d((lambda h:g(f,h)),e)).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:(lambda g:list(d((lambda h:g(f,h)),e)).__eq__(c))(g.__xor__))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

It becomes here even more difficult as we have to deal with many `f` and `g` arguments. For example, in this nested lambdas, the 2 `g`s are different elements: `(lambda f:(lambda g:list(d((lambda h:g(f,h)),e)).__eq__(c))(g.__xor__))`. Indeed, the one on the right ( `g.__xor__` ) is not part of the second lambda, taking also a `g` as argument. We can rewrite it as follows: `(lambda f:list(d((lambda h:g.__xor__(f,h)),e)).__eq__(c))`:

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:(lambda f:list(d((lambda h:g.__xor__(f,h)),e)).__eq__(c))(sum(f)))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Once again, parameters `f`  should be carefully replaced: `(lambda f:(lambda f:list(d((lambda h:g.__xor__(f,h)),e)).__eq__(c))(sum(f)))` becomes now `(lambda f:list(d((lambda h:g.__xor__(sum(f),h)),e)).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:(lambda f:list(d((lambda h:g.__xor__(sum(f),h)),e)).__eq__(c))(range(f*f)))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

It's now time to integrate the argument `range(f*f)` inside the nested lambda: `(lambda f:(lambda f:list(d((lambda h:g.__xor__(sum(f),h)),e)).__eq__(c))(range(f*f)))` then becomes: `(lambda f:list(d((lambda h:g.__xor__(sum(range(f*f)),h)),e)).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:(lambda f:list(d((lambda h:g.__xor__(sum(range(f*f)),h)),e)).__eq__(c))(g(f)))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Let's continue the integration by simplifying `(lambda g:(lambda f:list(d((lambda h:g.__xor__(sum(range(f*f)),h)),e)).__eq__(c))(g(f)))`. The argument `g(f)` can be intergrated in the second lambda in this way: `(lambda g:list(d((lambda h:g.__xor__(sum(range(pow(g(f), 2))),h)),e)).__eq__(c))`. Pay attention, the `f` in `g(f)` is not part of the simplified lambda!

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:(lambda g:list(d((lambda h:g.__xor__(sum(range(pow(g(f), 2))),h)),e)).__eq__(c))(int))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Let's continue with the integration of `int` by rewrinting `(lambda f:(lambda g:list(d((lambda h:g.__xor__(sum(range(pow(g(f), 2))),h)),e)).__eq__(c))(int))`. It becomes then `(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f), 2))),h)),e)).__eq__(c))`:

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f), 2))),h)),e)).__eq__(c))(f(c[16])))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

The code becomes more clear now, we're almost done! We can then integrate the argument `f(c[16])` in `(lambda f:(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f), 2))),h)),e)).__eq__(c))(f(c[16])))`. It becomes therefore `(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f(c[16])), 2))),h)),e)).__eq__(c))`

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f(c[16])), 2))),h)),e)).__eq__(c))(chr))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

We can now integrate `chr` in `(lambda e:(lambda f:list(d((lambda h:int.__xor__(sum(range(pow(int(f(c[16])), 2))),h)),e)).__eq__(c))(chr))`. It can be written like this: `(lambda e:list(d((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),e)).__eq__(c))`:

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:(lambda e:list(d((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),e)).__eq__(c))(d(e,b)))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Unfortunately, we cannot easily isolate the `lambda h:int.__xor__ ...` as it uses variables that would not be declared. Hence we had to continue with our integrations. We can here try to integrate the argument `d(e,b)` by turning `(lambda e:(lambda e:list(d((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),e)).__eq__(c))(d(e,b)))` into
`(lambda e:list(d((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),d(e,b))).__eq__(c))`:

> ```python
>if (lambda c:(lambda b:(lambda d:(lambda e:list(d((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),d(e,b))).__eq__(c))(ord))(map))(input()))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Almost done, so for `ord`, `map` and `input`, let's do it in a row:

> ```python
>if (lambda c:list(map((lambda h:int.__xor__(sum(range(pow(int(chr(c[16])), 2))),h)),map(ord,input()))).__eq__(c))([69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]):
> ```

Let's now beautify this last snippet of code without any lambdas. We can see that the outer lambda takes an array of number as argument `c`, and this array must match the flag, after magical transformation:

> ```python
>print("Input flag: ", end="")
>numbers = [69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]
>flag = list(map((lambda h:int.__xor__(sum(range(pow(int(chr(numbers[16])), 2))),h)),map(ord,input())))
>if flag == numbers:
>	print("Correct!!")
>else:
>	print("Try again :((")
> ```

The routine `map` takes to arguments. The first one is a callable, and the second one is an iterable, applies this callable on each element of the iterable. Taking the result as argument of `list` returns a list of the results.

> ```python
>print("Input flag: ", end="")
>numbers = [69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]
>in_ascii_codes = map(ord,input())
>flag = list(map((lambda h:int.__xor__(sum(range(pow(int(chr(numbers[16])), 2))),h)), in_ascii_codes))
>print(flag)
>if flag == numbers:
>	print("Correct!!")
>else:
>	print("Try again :((")
> ```

We can now see that each character is XOR'ed against `sum(range(pow(int(chr(50)), 2)))` , 50 being the 15th character of the array. This can be simplified, as `chr(50)` is '2'. Therefore, it gives us

> ```python
>  sum(range(pow(int(chr(50)), 2)))
>= sum(range(pow(int('2'), 2)))
>= sum(range(pow(2, 2)))
>= sum(range(4))
>= 6
> ```

The constant value XOR'ed against each character is equal to 6 (1+2+3, 4 being excluded in `range` )

> ```python
>print("Input flag: ", end="")
>numbers = [69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123]
>in_ascii_codes = map(ord,input())
>flag = list(map((lambda h:int.__xor__(6, h)), in_ascii_codes))
>print(flag)
>if flag == numbers:
>	print("Correct!!")
>else:
>	print("Try again :((")
> ```


Since it's a XOR, we can recover the flag by applying the same computation on each character of the array:

> ```python
>>>> ''.join(chr(x^6) for x in [69,85,84,125,127,54,115,36,116,53,89,49,110,53,89,107,50,117,114,53,84,89,54,96,89,114,110,53,89,106,50,107,100,98,50,117,89,104,73,81,39,60,66,66,66,123])
>'CSR{y0u"r3_7h3_m4st3R_0f_th3_l4mbd4s_nOW!:DDD}'
> ```
