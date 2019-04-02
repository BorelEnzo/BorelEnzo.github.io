## Crack me - Reverse

### [~$ cd ..](../)

> I tried to solve this challenge from a crackme site, but the binary always exits when I start it
:((
Solve it for me!!

We are given a stripped 64-bit [binary](crack_me), which exits immediately as it runs. Radare2 gave us the following routines:

```
[0x000007a0]> aaa
[x] Analyze all flags starting with sym. and entry0 (aa)
[x] Analyze len bytes of instructions for references (aar)
[x] Analyze function calls (aac)
[ ] [*] Use -AA or aaaa to perform additional experimental analysis.
[x] Constructing a function name for fcn.* and sym.func.* functions (aan))
[0x000007a0]> afl
0x000006d0    3 23           sub.__cxa_finalize_232_6d0
0x00000700    2 16   -> 32   sym.imp._exit
0x00000710    2 16   -> 48   sym.imp.write
0x00000720    2 16   -> 48   sym.imp.__stack_chk_fail
0x00000730    2 16   -> 48   sym.imp.close
0x00000740    2 16   -> 48   sym.imp.ptrace
0x00000750    2 16   -> 48   sym.imp.creat
0x00000760    2 16   -> 48   sym.imp.execv
0x00000770    2 16   -> 48   sym.imp.fork
0x00000780    2 16   -> 48   sym.imp.mktemp
0x00000790    1 16           sub.__cxa_finalize_248_790
0x000007a0    1 43           entry0
0x000007d0    4 50   -> 40   sub.__cxa_finalize_216_7d0
0x000008aa    9 137          sub.ptrace_8aa
0x00000933   12 235          main
```
 By disassembling the `main`, we saw that a temporary file was created with a randomly generated name:

```
 [0x000007a0]> sf main
[0x00000933]> pdf
/ (fcn) main 235
|   main ();
|           ; var int local_30h @ rbp-0x30
|           ; var int local_24h @ rbp-0x24
|           ; var int local_1ch @ rbp-0x1c
|           ; var int local_17h @ rbp-0x17
|           ; var int local_fh @ rbp-0xf
|           ; var int local_bh @ rbp-0xb
|           ; var int local_9h @ rbp-0x9
|           ; var int local_8h @ rbp-0x8
|           ; DATA XREF from 0x000007bd (entry0)
|           0x00000933      55             push rbp
|           0x00000934      4889e5         mov rbp, rsp
|           0x00000937      4883ec30       sub rsp, 0x30               ; '0'
|           0x0000093b      897ddc         mov dword [rbp - local_24h], edi
|           0x0000093e      488975d0       mov qword [rbp - local_30h], rsi
|           0x00000942      64488b042528.  mov rax, qword fs:[0x28]    ; [0x28:8]=0x3848 ; '('
|           0x0000094b      488945f8       mov qword [rbp - local_8h], rax
|           0x0000094f      31c0           xor eax, eax
|           0x00000951      e854ffffff     call sub.ptrace_8aa
|           0x00000956      48b82f746d70.  movabs rax, 0x706d742f706d742f
|           0x00000960      488945e9       mov qword [rbp - local_17h], rax
|           0x00000964      c745f1585858.  mov dword [rbp - local_fh], 0x58585858
|           0x0000096b      66c745f55858   mov word [rbp - local_bh], 0x5858
|           0x00000971      c645f700       mov byte [rbp - local_9h], 0
|           0x00000975      488d45e9       lea rax, qword [rbp - local_17h]
|           0x00000979      4889c7         mov rdi, rax
|           0x0000097c      e8fffdffff     call sym.imp.mktemp        ; char *mktemp(char *template);
|           0x00000981      0fb645e9       movzx eax, byte [rbp - local_17h]
|           0x00000985      84c0           test al, al
|       ,=< 0x00000987      7507           jne 0x990
|       |   0x00000989      b800000000     mov eax, 0
|      ,==< 0x0000098e      eb78           jmp 0xa08
|      ||   ; JMP XREF from 0x00000987 (main)
|      |`-> 0x00000990      488d45e9       lea rax, qword [rbp - local_17h]
|      |    0x00000994      beed010000     mov esi, 0x1ed
|      |    0x00000999      4889c7         mov rdi, rax
|      |    0x0000099c      e8affdffff     call sym.imp.creat
|      |    0x000009a1      8945e4         mov dword [rbp - local_1ch], eax
|      |    0x000009a4      837de400       cmp dword [rbp - local_1ch], 0
|      |,=< 0x000009a8      7907           jns 0x9b1
|      ||   0x000009aa      b800000000     mov eax, 0
|     ,===< 0x000009af      eb57           jmp 0xa08
|     |||   ; JMP XREF from 0x000009a8 (main)
|     ||`-> 0x000009b1      8b45e4         mov eax, dword [rbp - local_1ch]
|     ||    0x000009b4      ba10270000     mov edx, 0x2710
|     ||    0x000009b9      488d35600620.  lea rsi, qword 0x00201020   ; 0x201020
|     ||    0x000009c0      89c7           mov edi, eax
|     ||    0x000009c2      e849fdffff     call sym.imp.write         ; ssize_t write(int fd, void *ptr, size_t nbytes);
|     ||    0x000009c7      483d10270000   cmp rax, 0x2710
|     ||,=< 0x000009cd      7407           je 0x9d6
|     |||   0x000009cf      b800000000     mov eax, 0
|    ,====< 0x000009d4      eb32           jmp 0xa08
|    ||||   ; JMP XREF from 0x000009cd (main)
|    |||`-> 0x000009d6      8b45e4         mov eax, dword [rbp - local_1ch]
|    |||    0x000009d9      89c7           mov edi, eax
|    |||    0x000009db      e850fdffff     call sym.imp.close         ; int close(int fildes);
|    |||    0x000009e0      e88bfdffff     call sym.imp.fork
|    |||    0x000009e5      85c0           test eax, eax
|    |||,=< 0x000009e7      7407           je 0x9f0
|    ||||   0x000009e9      b800000000     mov eax, 0
|   ,=====< 0x000009ee      eb18           jmp 0xa08
|   |||||   ; JMP XREF from 0x000009e7 (main)
|   ||||`-> 0x000009f0      488b55d0       mov rdx, qword [rbp - local_30h]
|   ||||    0x000009f4      488d45e9       lea rax, qword [rbp - local_17h]
|   ||||    0x000009f8      4889d6         mov rsi, rdx
|   ||||    0x000009fb      4889c7         mov rdi, rax
|   ||||    0x000009fe      e85dfdffff     call sym.imp.execv
|   ||||    0x00000a03      b800000000     mov eax, 0
|   ||||    ; JMP XREF from 0x000009ee (main)
|   ||||    ; JMP XREF from 0x000009d4 (main)
|   ||||    ; JMP XREF from 0x000009af (main)
|   ||||    ; JMP XREF from 0x0000098e (main)
|   ````--> 0x00000a08      488b4df8       mov rcx, qword [rbp - local_8h]
|           0x00000a0c      6448330c2528.  xor rcx, qword fs:[0x28]
|       ,=< 0x00000a15      7405           je 0xa1c
|       |   0x00000a17      e804fdffff     call sym.imp.__stack_chk_failvoid);
|       |   ; JMP XREF from 0x00000a15 (main)
|       `-> 0x00000a1c      c9             leave
\           0x00000a1d      c3             ret
```

The principle here is quite simple. This binary creates a temporary file, writes something in it and closes the file. Then, it forks and executes the newly created file. We had then to let the program run and take a look at this temporary file. We can also find the content of this extracted file by dumping the .data section of the binary:

```sh
objdump -s -j .data crack_me
```
giving us
```
crack_me:     file format elf64-x86-64

Contents of section .data:
 201000 00000000 00000000 08102000 00000000  .......... .....
 201010 00000000 00000000 00000000 00000000  ................
 201020 7f454c46 02010100 00000000 00000000  .ELF............
 201030 03003e00 01000000 30050000 00000000  ..>.....0.......
 201040 40000000 00000000 28110000 00000000  @.......(.......
 201050 00000000 40003800 09004000 1b001a00  ....@.8...@.....
 201060 06000000 04000000 40000000 00000000  ........@.......
 201070 40000000 00000000 40000000 00000000  @.......@.......
 201080 f8010000 00000000 f8010000 00000000  ................
 201090 08000000 00000000 03000000 04000000  ................
 2010a0 38020000 00000000 38020000 00000000  8.......8.......
 2010b0 38020000 00000000 1c000000 00000000  8...............
 2010c0 1c000000 00000000 01000000 00000000  ................
 2010d0 01000000 05000000 00000000 00000000  ................
 2010e0 00000000 00000000 00000000 00000000  ................
```

We then ran the binary and obtained the new binary, in our case `tmp/tmp58G3MV`, and started to analyze it with r2:

```
[0x00000530]> aaa
[Invalid instruction of 16183 bytes at 0x110 entry0 (aa)
Invalid instruction of 16114 bytes at 0x110
[x] Analyze all flags starting with sym. and entry0 (aa)
[x] Analyze function calls (aac)
[x] Analyze len bytes of instructions for references (aar)
[x] Constructing a function name for fcn.* and sym.func.* functions (aan)
[x] Type matching analysis for all functions (aaft)
[x] Use -AA or aaaa to perform additional experimental analysis.
[0x00000530]> afl
0x00000000    3 97   -> 123  sym.imp.__libc_start_main
0x000004e8    3 23           sub.__gmon_start_4e8
0x00000510    1 6            sym.imp.puts
0x00000520    1 6            sub.__cxa_finalize_520
0x00000530    1 43           entry0
0x00000560    4 50   -> 40   sub._ITM_deregisterTMCloneTable_560
0x000005a0    4 66   -> 57   loc.000005a0
0x000005f0    5 58   -> 51   entry.fini0
0x00000630    5 10   -> 124  entry.init0
0x0000063a   20 396          main
 ```

By disassembling the `main` routine. we found an easy code to reverse, where each character of the flag is compared against a constant value:

 ```
[0x00000530]> sf main
[0x0000063a]> pdf
/ (fcn) main 396
|   main (int argc, char **argv, char **envp);
|           ; var char **local_20h @ rbp-0x20
|           ; var signed int local_14h @ rbp-0x14
|           ; var int local_8h @ rbp-0x8
|           ; arg signed int argc @ rdi
|           ; arg char **argv @ rsi
|           ; DATA XREF from entry0 (0x54d)
|           0x0000063a      55             push rbp
|           0x0000063b      4889e5         mov rbp, rsp
|           0x0000063e      4883ec20       sub rsp, 0x20
|           0x00000642      897dec         mov dword [local_14h], edi  ; argc
|           0x00000645      488975e0       mov qword [local_20h], rsi  ; argv
|           0x00000649      837dec01       cmp dword [local_14h], 1
|       ,=< 0x0000064d      7f16           jg 0x665
|       |   0x0000064f      488d3dfe0100.  lea rdi, qword str.Usage:_crack_me__flag ; 0x854 ; "Usage: crack_me <flag>" ; const char *s
|       |   0x00000656      e8b5feffff     call sym.imp.puts           ; int puts(const char *s)
|       |   0x0000065b      b801000000     mov eax, 1
|      ,==< 0x00000660      e95f010000     jmp 0x7c4
|      ||   ; CODE XREF from main (0x64d)
|      |`-> 0x00000665      488b45e0       mov rax, qword [local_20h]
|      |    0x00000669      488b4008       mov rax, qword [rax + 8]    ; [0x8:8]=0
|      |    0x0000066d      488945f8       mov qword [local_8h], rax
|      |    0x00000671      488b45f8       mov rax, qword [local_8h]
|      |    0x00000675      4883c005       add rax, 5
|      |    0x00000679      0fb600         movzx eax, byte [rax]
|      |    0x0000067c      ba72000000     mov edx, 0x72               ; 'r'
|      |    0x00000681      38d0           cmp al, dl
|      |,=< 0x00000683      0f852a010000   jne 0x7b3
|      ||   0x00000689      488b45f8       mov rax, qword [local_8h]
|      ||   0x0000068d      4883c001       add rax, 1
|      ||   0x00000691      0fb600         movzx eax, byte [rax]
|      ||   0x00000694      ba53000000     mov edx, 0x53               ; 'S'
|      ||   0x00000699      38d0           cmp al, dl
|     ,===< 0x0000069b      0f8512010000   jne 0x7b3
|     |||   0x000006a1      488b45f8       mov rax, qword [local_8h]
|     |||   0x000006a5      4883c006       add rax, 6
|     |||   0x000006a9      0fb600         movzx eax, byte [rax]
|     |||   0x000006ac      ba38000000     mov edx, 0x38               ; '8'
|     |||   0x000006b1      38d0           cmp al, dl
|    ,====< 0x000006b3      0f85fa000000   jne 0x7b3
|    ||||   0x000006b9      488b45f8       mov rax, qword [local_8h]
|    ||||   0x000006bd      4883c003       add rax, 3
|    ||||   0x000006c1      0fb600         movzx eax, byte [rax]
|    ||||   0x000006c4      ba7b000000     mov edx, 0x7b               ; '{'
|    ||||   0x000006c9      38d0           cmp al, dl
|   ,=====< 0x000006cb      0f85e2000000   jne 0x7b3
|   |||||   0x000006d1      488b45f8       mov rax, qword [local_8h]
|   |||||   0x000006d5      4883c004       add rax, 4
|   |||||   0x000006d9      0fb600         movzx eax, byte [rax]
|   |||||   0x000006dc      ba67000000     mov edx, 0x67               ; 'g'
|   |||||   0x000006e1      38d0           cmp al, dl
|  ,======< 0x000006e3      0f85ca000000   jne 0x7b3
|  ||||||   0x000006e9      488b45f8       mov rax, qword [local_8h]
|  ||||||   0x000006ed      4883c002       add rax, 2
|  ||||||   0x000006f1      0fb600         movzx eax, byte [rax]
|  ||||||   0x000006f4      ba52000000     mov edx, 0x52               ; 'R'
|  ||||||   0x000006f9      38d0           cmp al, dl
| ,=======< 0x000006fb      0f85b2000000   jne 0x7b3
| |||||||   0x00000701      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000705      0fb600         movzx eax, byte [rax]
| |||||||   0x00000708      ba43000000     mov edx, 0x43               ; 'C'
| |||||||   0x0000070d      38d0           cmp al, dl
| ========< 0x0000070f      0f859e000000   jne 0x7b3
| |||||||   0x00000715      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000719      4883c007       add rax, 7
| |||||||   0x0000071d      0fb600         movzx eax, byte [rax]
| |||||||   0x00000720      ba5f000000     mov edx, 0x5f               ; '_'
| |||||||   0x00000725      38d0           cmp al, dl
| ========< 0x00000727      0f8586000000   jne 0x7b3
| |||||||   0x0000072d      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000731      4883c008       add rax, 8
| |||||||   0x00000735      0fb600         movzx eax, byte [rax]
| |||||||   0x00000738      ba6a000000     mov edx, 0x6a               ; 'j'
| |||||||   0x0000073d      38d0           cmp al, dl
| ========< 0x0000073f      7572           jne 0x7b3
| |||||||   0x00000741      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000745      4883c009       add rax, 9
| |||||||   0x00000749      0fb600         movzx eax, byte [rax]
| |||||||   0x0000074c      ba30000000     mov edx, 0x30               ; '0'
| |||||||   0x00000751      38d0           cmp al, dl
| ========< 0x00000753      755e           jne 0x7b3
| |||||||   0x00000755      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000759      4883c00a       add rax, 0xa
| |||||||   0x0000075d      0fb600         movzx eax, byte [rax]
| |||||||   0x00000760      ba62000000     mov edx, 0x62               ; 'b'
| |||||||   0x00000765      38d0           cmp al, dl
| ========< 0x00000767      754a           jne 0x7b3
| |||||||   0x00000769      488b45f8       mov rax, qword [local_8h]
| |||||||   0x0000076d      4883c00b       add rax, 0xb
| |||||||   0x00000771      0fb600         movzx eax, byte [rax]
| |||||||   0x00000774      ba21000000     mov edx, 0x21               ; '!'
| |||||||   0x00000779      38d0           cmp al, dl
| ========< 0x0000077b      7536           jne 0x7b3
| |||||||   0x0000077d      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000781      4883c00c       add rax, 0xc
| |||||||   0x00000785      0fb600         movzx eax, byte [rax]
| |||||||   0x00000788      ba7d000000     mov edx, 0x7d               ; '}'
| |||||||   0x0000078d      38d0           cmp al, dl
| ========< 0x0000078f      7522           jne 0x7b3
| |||||||   0x00000791      488b45f8       mov rax, qword [local_8h]
| |||||||   0x00000795      4883c00d       add rax, 0xd
| |||||||   0x00000799      0fb600         movzx eax, byte [rax]
| |||||||   0x0000079c      ba00000000     mov edx, 0
| |||||||   0x000007a1      38d0           cmp al, dl
| ========< 0x000007a3      750e           jne 0x7b3
| |||||||   0x000007a5      488d3dbf0000.  lea rdi, qword str.You_got_it ; 0x86b ; "You got it!" ; const char *s
| |||||||   0x000007ac      e85ffdffff     call sym.imp.puts           ; int puts(const char *s)
| ========< 0x000007b1      eb0c           jmp 0x7bf
| |||||||   ; XREFS: CODE 0x00000683  CODE 0x0000069b  CODE 0x000006b3  CODE 0x000006cb  CODE 0x000006e3  CODE 0x0000070f  
| |||||||   ; XREFS: CODE 0x00000727  CODE 0x0000073f  CODE 0x00000753  CODE 0x00000767  CODE 0x0000077b  CODE 0x0000078f  
| |||||||   ; XREFS: CODE 0x000007a3  
| `````-`-> 0x000007b3      488d3dbd0000.  lea rdi, qword str.Try_again_: ; 0x877 ; "Try again :(" ; const char *s
|      |    0x000007ba      e851fdffff     call sym.imp.puts           ; int puts(const char *s)
|      |    ; CODE XREF from main (0x7b1)
| --------> 0x000007bf      b800000000     mov eax, 0
|      |    ; CODE XREF from main (0x660)
|      `--> 0x000007c4      c9             leave
\           0x000007c5      c3             ret
```

We were then able to recover the flag by putting each character at the right place. The principle is as follows:
* the address of the input is stored in `eax` by moving `qword [local_8h]`
* an offset is added, being the position of the character to check
* a constant is put in `edx`
* and a comparison is made

The steps are the following:

* flag[5] == 'r'
* flag[1] == 'S'
* flag[6] == '8'
* flag[3] == '{'
* flag[4] == 'g'
* flag[2] == 'R'
* flag[0] == 'C'
* flag[7] == '\_'
* flag[8] == 'j'
* flag[9] == '0'
* flag[10] == 'b'
* flag[11] == '!'
* flag[12] == '}'

The flag is therefore **CSR{gr8_j0b!}**
