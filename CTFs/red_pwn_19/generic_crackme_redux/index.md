# Generic Crackme Redux - RE

### [~$ cd ..](../)

>Generic Crackme Redux
>
>Written by: blevy
>
>Note: Enclose the flag with flag{}.

We were given [ŧhis binary](generic_crackme_redux), recognized as a ELF 64-bit:

```
$ file generic_crackme_redux
generic_crackme_redux: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, BuildID[sha1]=c336bbc034850b821149fdb3267edcb80589a47b, for GNU/Linux 3.2.0, stripped
```

As usual, we started with radare2:

```
[0x00001070]> aaa
[Cannot find function 'entry0' at 0x00001070 entry0 (aa)
[x] Analyze all flags starting with sym. and entry0 (aa)
[x] Analyze len bytes of instructions for references (aar)
[x] Analyze function calls (aac)
[ ] [*] Use -AA or aaaa to perform additional experimental analysis.
[x] Constructing a function name for fcn.* and sym.func.* functions (aan))
[0x00001070]> afl
0x00001030    2 16   -> 32   sym.imp.puts
0x00001040    2 16   -> 48   sym.imp.__stack_chk_fail
0x00001050    2 16   -> 48   sym.imp.printf
0x00001060    2 16   -> 48   sym.imp.__isoc99_scanf
0x000010a0    3 33           sub.__cxa_finalize_216_a0
0x00001169    1 29           fcn.00001169
[0x00001070]>
```

Only one interesting routine here, so let's jump at `0x00001169`:

```
[0x00001070]> sf fcn.00001169
[0x00001169]> pdf
/ (fcn) fcn.00001169 29
|   fcn.00001169 ();
|           ; var int local_4h @ rbp-0x4
|           ; CALL XREF from 0x000011cb (unk)
|           0x00001169      55             push rbp
|           0x0000116a      4889e5         mov rbp, rsp
|           0x0000116d      897dfc         mov dword [rbp - local_4h], edi
|           0x00001170      8b55fc         mov edx, dword [rbp - local_4h]
|           0x00001173      89d0           mov eax, edx
|           0x00001175      c1e002         shl eax, 2
|           0x00001178      01d0           add eax, edx
|           0x0000117a      01c0           add eax, eax
|           0x0000117c      3d92c20a00     cmp eax, 0xac292
|           0x00001181      0f94c0         sete al
|           0x00001184      5d             pop rbp
\           0x00001185      c3             ret
```

Not really difficult to understand! Starting from the end, we know that `eax` is equal to `0x56149` at `0x0000117a`, as it is added to itself and compared against `0xac292` at the next line. Dividing the constant value by two gives then `0x56149`

Besides this, we know that `eax` was equal to `edx` at `0x00001173`. The value in `eax` was then shifted to the left by 2, which means that it was multiplied by 4. Then `edx` was added to the new value, which means that the new value is 5 times the original one. If we divide `0x56149` by 5, we get `0x11375` or `70517`

FLAG: **flag{70517}**

EOF
