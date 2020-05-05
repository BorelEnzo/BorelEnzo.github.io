---
layout: posts
title:  Finals CSCBE19 - Failing For XOR
date:   2019-03-02
categories: [CTFs, finalscscbe19]
---

[~$ cd ..](/ctfs/finalscscbe19/2019/03/02/index.html)

Author: [renaud11232](https://renaud11232.github.io/ctf/CSCBE2019/Finals/failing_for_xor/)

>Some idiot encrypted the flag with a 6 byte key. I'm sure you can spot the problem.
>
>dec028e43de8e61ef717accc00d178eacc18af25aecc28ed31cde75be2

For this challenge we were given a hexadecimal representation of some ciphered text.
The challenge description said it was XORed with a 6 bytes key but something went wrong went decrypting. Here is the cipher text :

```
dec028e43de8e61ef717accc00d178eacc18af25aecc28ed31cde75be2
```

We knew the flag format was gonna be `CSC{<something>}`, so we could easily compute the first 4 bytes of the key. So we started writing a python script that XORed the first 4 bytes of the cipher text with `CSC{`, then tried to brute force the 2 other bytes but without success.

Then we remembered that there is something wrong with the challenge description, so we used `xortool` to check the possible key length :

```
xortool -x cipher
The most probable key lengths:
   1:   31.3%
   5:   40.7%
  10:   21.4%
  20:   6.7%
Key-length can be 5*n
Most possible char is needed to guess the key!
```

So in fact we retried the same script but adapting it for a 5 bytes key. That gave us the following ouput.

```
...
CSC{úuuuhÐ1_kN¿w_s0â3_CröPt0}
CSC{õuuuhß1_kN°w_s0í3_CrùPt0}
CSC{ôuuuhÞ1_kN±w_s0ì3_CrøPt0}
CSC{÷uuuhÝ1_kN²w_s0ï3_CrûPt0}
CSC{öuuuhÜ1_kN³w_s0î3_CrúPt0}
CSC{ñuuuhÛ1_kN´w_s0é3_CrýPt0}
CSC{ðuuuhÚ1_kNµw_s0è3_CrüPt0}
CSC{óuuuhÙ1_kN¶w_s0ë3_CrÿPt0}
CSC{òuuuhØ1_kN·w_s0ê3_CrþPt0}
CSC{íuuuhÇ1_kN¨w_s0õ3_CráPt0}
CSC{ìuuuhÆ1_kN©w_s0ô3_CràPt0}
CSC{ïuuuhÅ1_kNªw_s0÷3_CrãPt0}
CSC{îuuuhÄ1_kN«w_s0ö3_CrâPt0}
CSC{éuuuhÃ1_kN¬w_s0ñ3_CråPt0}
CSC{èuuuhÂ1_kN­w_s0ð3_CräPt0}
CSC{ëuuuhÁ1_kN®w_s0ó3_CrçPt0}
CSC{êuuuhÀ1_kN¯w_s0ò3_CræPt0}
CSC{åuuuhÏ1_kN w_s0ý3_CréPt0}
CSC{äuuuhÎ1_kN¡w_s0ü3_CrèPt0}
CSC{çuuuhÍ1_kN¢w_s0ÿ3_CrëPt0}
CSC{æuuuhÌ1_kN£w_s0þ3_CrêPt0}
CSC{áuuuhË1_kN¤w_s0ù3_CríPt0}
...
```
Looking at this it looked like the last word of the flag was going to be `CryPt0`, so we tweaked our script one last time :

```python
import base64
import string

cipher = base64.b16decode("dec028e43de8e61ef717accc00d178eacc18af25aecc28ed31cde75be2".upper())
partial_flag = "CSC{"

key_base = []

for i in range(len(partial_flag)):
    key_byte = ord(partial_flag[i]) ^ cipher[i]
    key_base.append(key_byte)

possible_keys = []
for b in range(256):
        key = list(key_base)
        key.append(b)
        possible_keys.append(key)

for key in possible_keys:
    key = key * 5 + key[:4]
    clear = []
    for i in range(len(key)):
        clear.append(chr(key[i] ^ cipher[i]))
    clear = "".join(clear)
    if clear.endswith("CryPt0}"):
        print(clear)

```

And here is the output :

```
CSC{uuuuh_1_kN0w_s0m3_CryPt0}
```

DONE
