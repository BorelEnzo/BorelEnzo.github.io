# Ancient writings 1, 2 (Misc)

### [~$ cd ..](../)

For the first part of the challenge. we were given a text file with extension `bf`, containing this weird string

> ```
>>232+*""43*52**5+65+:*4::**3:*21+:** "b"$                 v    >
>^"78"v $  $ \ $ \ $<"x32P"    _                           "    v
>^|%24<"SC{key_Y3ByZXNzZXk=}"  ^7        *:+33 +*27*7 *44"key"  <
><>47+:*1+                                        47+:*2+vv"   >^
>v                                                       <<    ^<
>>                                        >    34*9g"SC":vv     ^
><                                                    v:,_@>"#_"^
>>                                                    >  ^      <
>
>0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZv^<>0123456789abcdefghijklmnopqrstuvwxyz
> ```

By googling, we found that `bf` was primarily associated with Befunge programs, a language we didn't know. Using one of our favorite online tool, we ran it and got:

![tio1](tio1.png)

For the second part, a line was missing, and we had to submit it on a web page to get the flag:

> ```
>>232+*""43*52**5+65+:*4::**3:*21+:** "b"$                 v    >
>^"78"v $  $ \ $ \ $<"x32P"    _                           "    v
>^|%24<"SC{key_Y3ByZXNzZXk=}"  ^7        *:+33 +*27*7 *44"key"  <
><>47+:*1+                                     47+:*2+v    "   >^
> ... missing line ...
>>                                        >    34*9g"SC":vv     ^
><                                                    v:,_@>"#_"^
>>                                                    >  ^      <
>
>0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZv^<>0123456789abcdefghijklmnopqrstuvwxyz
> ```

The two codes were very similar, and the corresponding line in the previous snippet was the following:

> ```
>>v                                                       <<    ^<
> ```

In Befunge, "v", ">", "^" and "<" are arrows, giving the path to follow. It means that we had to align arrows to make the code run:

> ```
><>47+:*1+                                     47+:*2+v    "   >^
>v                                                    <        ^<
> ```

By sending the correct answer, we got the flag : **CSC{#d^wv<>e23$&@dDHC}**
