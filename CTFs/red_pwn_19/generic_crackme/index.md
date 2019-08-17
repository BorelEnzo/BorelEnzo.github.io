# Generic crack me - RE

### [~$ cd ..](../)

>Generic Crackme
>
>Written by: blevy
>
>Note: Enclose the flag with flag{}.

We were given [this binary](generic_crackme), recognized as an ELF 64 bits:

```
$ file generic_crackme
generic_crackme: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, BuildID[sha1]=14b34c581c75edd5cb6b773d9a320f299f23ba7f, for GNU/Linux 3.2.0, stripped
```

No need to run it, static analysis was sufficient. By opening it in radare2 (old-school way, but way better than IDA !), we found:

```
[0x00001060]> aaa
[Cannot find function 'entry0' at 0x00001060 entry0 (aa)
[x] Analyze all flags starting with sym. and entry0 (aa)
[x] Analyze len bytes of instructions for references (aar)
[x] Analyze function calls (aac)
[ ] [*] Use -AA or aaaa to perform additional experimental analysis.
[x] Constructing a function name for fcn.* and sym.func.* functions (aan))
[0x00001060]> afl
0x00001030    2 16   -> 32   sym.imp.puts
0x00001040    2 16   -> 48   sym.imp.__stack_chk_fail
0x00001050    2 16   -> 48   sym.imp.fgets
0x00001090    3 33           sub.__cxa_finalize_216_90
0x00001159    1 15           fcn.00001159
0x00001168   12 183          fcn.00001168
```

The last two routines seemed to be really interesting, so we disassembled them:

```
[0x00001060]> sf fcn.00001159
[0x00001159]> pdf
/ (fcn) fcn.00001159 15
|   fcn.00001159 ();
|           ; var int local_4h @ rbp-0x4
|           ; CALL XREF from 0x00001180 (fcn.00001168)
|           ; CALL XREF from 0x000011a4 (fcn.00001168)
|           ; CALL XREF from 0x000011c5 (fcn.00001168)
|           ; CALL XREF from 0x000011e6 (fcn.00001168)
|           ; CALL XREF from 0x00001207 (fcn.00001168)
|           0x00001159      55             push rbp
|           0x0000115a      4889e5         mov rbp, rsp
|           0x0000115d      897dfc         mov dword [rbp - local_4h], edi
|           0x00001160      8b45fc         mov eax, dword [rbp - local_4h]
|           0x00001163      83c001         add eax, 1
|           0x00001166      5d             pop rbp
\           0x00001167      c3             ret
```

The code here is really to understand: the value in `edi` is moved in `eax`, and `eax` is incremeted by 1. Since it's a 64-bit binary, `edi` contains the first argument passed to the routine, and by convention, `eax` contains the returned value. In other words, this function does:

```c
int foo(int x){
    return x+1;
}
```

The caller is a little bit more complex but also really easy to understand:

```
[0x00001159]> sf fcn.00001168
[0x00001168]> pdf
/ (fcn) fcn.00001168 183
|   fcn.00001168 ();
|           ; var int local_8h @ rbp-0x8
|           ; CALL XREF from 0x00001261 (unk)
|           0x00001168      55             push rbp
|           0x00001169      4889e5         mov rbp, rsp
|           0x0000116c      4883ec08       sub rsp, 8
|           0x00001170      48897df8       mov qword [rbp - local_8h], rdi
|           0x00001174      488b45f8       mov rax, qword [rbp - local_8h]
|           0x00001178      0fb600         movzx eax, byte [rax]
|           0x0000117b      0fbec0         movsx eax, al
|           0x0000117e      89c7           mov edi, eax
|           0x00001180      e8d4ffffff     call fcn.00001159
|           0x00001185      83f865         cmp eax, 0x65               ; 'e' ; 'e'
|       ,=< 0x00001188      740a           je 0x1194
|       |   0x0000118a      b800000000     mov eax, 0
|      ,==< 0x0000118f      e989000000     jmp 0x121d
|      ||   ; JMP XREF from 0x00001188 (fcn.00001168)
|      |`-> 0x00001194      488b45f8       mov rax, qword [rbp - local_8h]
|      |    0x00001198      4883c001       add rax, 1
|      |    0x0000119c      0fb600         movzx eax, byte [rax]
|      |    0x0000119f      0fbec0         movsx eax, al
|      |    0x000011a2      89c7           mov edi, eax
|      |    0x000011a4      e8b0ffffff     call fcn.00001159
|      |    0x000011a9      83f870         cmp eax, 0x70               ; 'p' ; 'p'
|      |,=< 0x000011ac      7407           je 0x11b5
|      ||   0x000011ae      b800000000     mov eax, 0
|     ,===< 0x000011b3      eb68           jmp 0x121d
|     |||   ; JMP XREF from 0x000011ac (fcn.00001168)
|     ||`-> 0x000011b5      488b45f8       mov rax, qword [rbp - local_8h]
|     ||    0x000011b9      4883c002       add rax, 2
|     ||    0x000011bd      0fb600         movzx eax, byte [rax]
|     ||    0x000011c0      0fbec0         movsx eax, al
|     ||    0x000011c3      89c7           mov edi, eax
|     ||    0x000011c5      e88fffffff     call fcn.00001159
|     ||    0x000011ca      83f868         cmp eax, 0x68               ; 'h' ; 'h'
|     ||,=< 0x000011cd      7407           je 0x11d6
|     |||   0x000011cf      b800000000     mov eax, 0
|    ,====< 0x000011d4      eb47           jmp 0x121d
|    ||||   ; JMP XREF from 0x000011cd (fcn.00001168)
|    |||`-> 0x000011d6      488b45f8       mov rax, qword [rbp - local_8h]
|    |||    0x000011da      4883c003       add rax, 3
|    |||    0x000011de      0fb600         movzx eax, byte [rax]
|    |||    0x000011e1      0fbec0         movsx eax, al
|    |||    0x000011e4      89c7           mov edi, eax
|    |||    0x000011e6      e86effffff     call fcn.00001159
|    |||    0x000011eb      83f868         cmp eax, 0x68               ; 'h' ; 'h'
|    |||,=< 0x000011ee      7407           je 0x11f7
|    ||||   0x000011f0      b800000000     mov eax, 0
|   ,=====< 0x000011f5      eb26           jmp 0x121d
|   |||||   ; JMP XREF from 0x000011ee (fcn.00001168)
|   ||||`-> 0x000011f7      488b45f8       mov rax, qword [rbp - local_8h]
|   ||||    0x000011fb      4883c004       add rax, 4
|   ||||    0x000011ff      0fb600         movzx eax, byte [rax]
|   ||||    0x00001202      0fbec0         movsx eax, al
|   ||||    0x00001205      89c7           mov edi, eax
|   ||||    0x00001207      e84dffffff     call fcn.00001159
|   ||||    0x0000120c      83f87a         cmp eax, 0x7a               ; 'z' ; 'z'
|   ||||,=< 0x0000120f      7407           je 0x1218
|   |||||   0x00001211      b800000000     mov eax, 0
|  ,======< 0x00001216      eb05           jmp 0x121d
|  ||||||   ; JMP XREF from 0x0000120f (fcn.00001168)
|  |||||`-> 0x00001218      b801000000     mov eax, 1
|  |||||    ; JMP XREF from 0x0000118f (fcn.00001168)
|  |||||    ; JMP XREF from 0x000011b3 (fcn.00001168)
|  |||||    ; JMP XREF from 0x000011d4 (fcn.00001168)
|  |||||    ; JMP XREF from 0x000011f5 (fcn.00001168)
|  |||||    ; JMP XREF from 0x00001216 (fcn.00001168)
|  `````--> 0x0000121d      c9             leave
\           0x0000121e      c3             ret
```

One can see that the previous routine is called multiple times and the returned value is compared against a constant
one. Let's take the first test as an example:

```
0fb600         movzx eax, byte [rax]
0fbec0         movsx eax, al ; put input[0] in eax
89c7           mov edi, eax ; copy it in edi
e8d4ffffff     call fcn.00001159 ; call the incremeting routine
83f865         cmp eax, 0x65               ; compare it against 'e'
740a           je 0x1194    ; jump to next test
b800000000     mov eax, 0   ; set 0 as returned value
e989000000     jmp 0x121d   ; jump to the end of the routine

```

By taking a quick look at the characters between quotes, one can see that the expected returned values are `ephhz`. Subtracting 1 to each of them gave the flag **flag{doggy}**

EOF
