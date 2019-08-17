# Super Hash - Crypto

### [~$ cd ..](../)

>Super Hash
>
>Written by: NotDeGhost
>
>Does hashing something multiple times make it more secure? I sure hope so. I've hashed my secret ten times with md5! Hopefully this makes up for the fact that my secret is really short. Wrap the secret in flag{}.
>
>Note: Follow the format of the provided hash exactly
>
>Hash: CD04302CBBD2E0EB259F53FAC7C57EE2

The form of the hash here is indeed crucial. At the beginning, I thought that only the final hash was in hexadecimal (and uppercase), but actually the easiest thing to do was to create a simple loop, computing every time the same thing:

```python
import hashlib
import itertools
import string
def calc(x):
    for i in range(10):
        x = hashlib.md5(x).hexdigest().upper()
    return x

combi = itertools.combinations_with_replacement(string.printable, 1)
for c in combi:
    string = ''.join(x for x in c)
    if calc(string) == "CD04302CBBD2E0EB259F53FAC7C57EE2":
        print string
        break
```

Using `itertools` was here even an overkill as the solution was only `^`

FLAG: **flag{^}**

EOF
