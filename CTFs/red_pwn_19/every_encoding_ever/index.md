# Every Encoding Ever - Crypto

### [~$ cd ..](../)

>Every Encoding Ever
>
>Written by: NotDeGhost
>
>Who knew encodings could be this annoying?

We were given [this text file](out.txt) with a long ASCII string, and we immediately recognized base64. The string can be decoded several times, as the output was still a base64 encoded string. We repeated these steps several times and sometimes we found UTF-16 encoded strings (one in every two bytes is null). We then obtained the following string, which was neither UTF-16 nor base64:

`\xf9\xe9\x86\x91\x97a\x95\xd1\xa8\xd4\x85\xe3\x94\xd1\xe3\xc6\x97a\x82\xe7\x93a\xe6\x89\x87\xa3\x95\xf5\x97\xc9\xd3\xc6\xf8\xc9\xd6\xc7\x93\xf5\xd7\xe5\xa8\xd4\x95\xa7\x97\xd5\x86\xc4\xf9\xa3\xe7\x89\xf2\xe9\xd3\xf2\xf1\xf5\xe2\x95\xf1\x83\x91\x94\x88\xf6\xe7\xc8\xf4a\x82\xe6\xf0\xa4\xe6\xe3\xf9\xd4\x89\xe4\x97\xf6\xd7\xc9\xf8\xa5\x82\x97\xf1\xf8\xe9\xd6\x88\xc9\x82\x96\x88\x97\xe7\xf0\x86\x87~~`

We didn't know this encoding, then we tried to "bruteforce" them, based on [this](ttps://stackoverflow.com/questions/3824101/how-can-i-programmatically-find-the-list-of-codecs-known-to-python) Stackoverflow answer

```python
import encodings
import os
import pkgutil
x = '\xf9\xe9\x86\x91\x97a\x95\xd1\xa8\xd4\x85\xe3\x94\xd1\xe3\xc6\x97a\x82\xe7\x93a\xe6\x89\x87\xa3\x95\xf5\x97\xc9\xd3\xc6\xf8\xc9\xd6\xc7\x93\xf5\xd7\xe5\xa8\xd4\x95\xa7\x97\xd5\x86\xc4\xf9\xa3\xe7\x89\xf2\xe9\xd3\xf2\xf1\xf5\xe2\x95\xf1\x83\x91\x94\x88\xf6\xe7\xc8\xf4a\x82\xe6\xf0\xa4\xe6\xe3\xf9\xd4\x89\xe4\x97\xf6\xd7\xc9\xf8\xa5\x82\x97\xf1\xf8\xe9\xd6\x88\xc9\x82\x96\x88\x97\xe7\xf0\x86\x87~~'
modnames=set([modname for importer, modname, ispkg in pkgutil.walk_packages(path=[os.path.dirname(encodings.__file__)], prefix='')])
aliases=set(encodings.aliases.aliases.values())
codec_names=modnames.union(aliases)
for codec in codec_names:
    try:
        print x.decode(codec)
        print codec
    except:
        pass
```

The encoding `cp1140` was decoded as a new base64-encoded string, and we noticed that there were alternatively used:

```python
import base64
while "flag" not in x:
    x = x.decode("cp1140")
    x = base64.b64decode(x)
    print x
```

FLAG: **flag{dats_a_lot_of_charsets}**

EOF
