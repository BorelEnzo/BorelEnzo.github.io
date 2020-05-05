---
layout: posts
title:  Finals CSCBE19 - Ancient Writings #3 - Misc (210 pts)
date:   2019-03-02
categories: [CTFs, finalscscbe19]
---

[~$ cd ..](/ctfs/finalscscbe19/2019/03/02/index.html)

Author: [renaud11232](https://renaud11232.github.io/ctf/CSCBE2019/Finals/ancient_writings_3/) aka Befunge Master

>Two weeks have passed since the passing of the beloved Dr XX, and Mr. Blanche, the
cleaning man, has cleared out the garden shed where the Dr would muse about the Life of
Brian.
A printout was found, it has the following written on it in big letters.
PATCHING NEEDED
As you've proven yourself to be an expert in Medieval programming during the qualifiers;
can you help Mr. Blanche ?
Unfortunately, again one line is missing. Can you find this line ?
>
>52.210.193.145:4242

The goal was the same as in [Ancient Writings #2](https://renaud11232.github.io/ctf/CSCBE2019/Qualifiers/ancient_writings_2/), we needed to rewrite the missing line from a `Befunge` program:

```
>232+*""43*52**5+65+:*4::**3:*21+:** "b"$                 v    >
^"78"v $  $ \ $ \ $<"x32P"    _                           "    v
^|%24<"SC{key_Y3ByZXNzZXk=}"  ^7        *:+33 +*27*7 *44"key"  <
<>"PATCHING"47+:*1+7v"NEEDED"*:+33 +*27*7 *44 47+:*2+v    "   >^
```

```
>                                        >   <34*9g"CC":vv     ^
<                                                    v:,_@>"#_"^
>                                                    >  ^      <

0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZv^<>0123456789abcdefghijklmnopqrstuvwxyz
```

This one was way harder than the previous ones as the "path" the code execution takes was not the same at all, and the stack was filled with garbage on purpose.

Some parts of the code were still very similar. Most importantly, the part that displays the characters was the same (The loop with the `,` operator).

So, we had to come with a way to :

1. Get the code to the display loop
2. Remove the garbage from the stack
3. Add the missing output part into the stack

This meant we had to use the `$` operator, which pops the last element on the stack, the `""` to delimit character values to push on the stack and arrows to guide the code.

We finally managed to write the following line :

```
v                   >$$$$$$$$$$"z{CSCC"              v        ^<
```

Once submitted this gave us the flag.

DONE
