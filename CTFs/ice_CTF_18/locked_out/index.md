# Locked out (Reverse)

### [~$ cd ..](../)

>This is a fancy looking lock, I wonder what would happen if you broke it open?
>
>Note: You can download the binary here

We have an SSH session open on ssh.icec.tf:2222. When we solved this challenge, it was not yet possible to download the binary,
so we first did:

> ```
>[adversary ~]$ ls -l
>total 8
>drwxr-xr-x. 2 root root 4096 Sep  8 01:19 lockedout
>[adversary ~]$ cd lockedout
>[adversary ~/lockedout]$ ls -l
>total 20
>-r--r-----. 1 root drevil   27 Sep  8 01:19 flag.txt
>-rwxr-sr-x. 1 root drevil 5628 Sep  8 01:19 lock
>[adversary ~/lockedout]$ ./lock
>This is a pesky lock.. do you think you can open it?
>Enter key: test
>key failed
> ```

Ok, let's open it in GDB.

### First steps

First, we took a look at routine:

> ```
>gdb-peda$ info functions
>All defined functions:
>
>Non-debugging symbols:
>0x00000590  strcmp@plt
>0x000005a0  printf@plt
>0x000005b0  strcspn@plt
>0x000005c0  free@plt
>0x000005d0  fgets@plt
>0x000005e0  getegid@plt
>0x000005f0  puts@plt
>0x00000600  system@plt
>0x00000610  strlen@plt
>0x00000620  __libc_start_main@plt
>0x00000630  __strdup@plt
>0x00000640  memfrob@plt
>0x00000650  setresgid@plt
>0x00000670  main
> ```

Only one custom routine, it won't be too difficult. Let's break t main, run the program and disassemble the routine:

> ```
>gdb-peda$ disas main
>Dump of assembler code for function main:
>=> 0x56555670 <+0>:	lea    ecx,[esp+0x4]
>   0x56555674 <+4>:	and    esp,0xfffffff0
>   0x56555677 <+7>:	push   DWORD PTR [ecx-0x4]
>   0x5655567a <+10>:	push   ebp
>   0x5655567b <+11>:	mov    ebp,esp
>   0x5655567d <+13>:	push   esi
>   0x5655567e <+14>:	push   ebx
>   0x5655567f <+15>:	call   0x56555770
>   0x56555684 <+20>:	add    ebx,0x197c
>   0x5655568a <+26>:	push   ecx
>   0x5655568b <+27>:	lea    esi,[ebp-0x118]
>   0x56555691 <+33>:	lea    eax,[ebx-0x15d0]
>   0x56555697 <+39>:	sub    esp,0x118
>   0x5655569d <+45>:	push   eax
>   0x5655569e <+46>:	call   0x565555f0 <puts@plt>
>   0x565556a3 <+51>:	lea    eax,[ebx-0x15f5]
>   0x565556a9 <+57>:	mov    DWORD PTR [esp],eax
>   0x565556ac <+60>:	call   0x565555a0 <printf@plt>
>   0x565556b1 <+65>:	mov    eax,DWORD PTR [ebx-0x10]
>   0x565556b7 <+71>:	add    esp,0xc
>   0x565556ba <+74>:	push   DWORD PTR [eax]
>   0x565556bc <+76>:	push   0x100
>   0x565556c1 <+81>:	push   esi
>   0x565556c2 <+82>:	call   0x565555d0 <fgets@plt>
>   0x565556c7 <+87>:	add    esp,0x10
>   0x565556ca <+90>:	test   eax,eax
>   0x565556cc <+92>:	je     0x56555707 <main+151>
>   0x565556ce <+94>:	lea    eax,[ebx-0x15e9]
>   0x565556d4 <+100>:	sub    esp,0x8
>   0x565556d7 <+103>:	push   eax
>   0x565556d8 <+104>:	push   esi
>   0x565556d9 <+105>:	call   0x565555b0 <strcspn@plt>
>   0x565556de <+110>:	mov    DWORD PTR [esp],esi
>   0x565556e1 <+113>:	mov    BYTE PTR [ebp+eax*1-0x118],0x0
>   0x565556e9 <+121>:	call   0x56555920							;Uh uh
>   0x565556ee <+126>:	add    esp,0x10
>   0x565556f1 <+129>:	test   eax,eax
>   0x565556f3 <+131>:	je     0x56555714 <main+164>
>   0x565556f5 <+133>:	lea    eax,[ebx-0x15dd]
>   0x565556fb <+139>:	sub    esp,0xc
>   0x565556fe <+142>:	push   eax
>   0x565556ff <+143>:	call   0x565555f0 <puts@plt>
>   0x56555704 <+148>:	add    esp,0x10
>   0x56555707 <+151>:	lea    esp,[ebp-0xc]
>   0x5655570a <+154>:	xor    eax,eax
>   0x5655570c <+156>:	pop    ecx
>   0x5655570d <+157>:	pop    ebx
>   0x5655570e <+158>:	pop    esi
>   0x5655570f <+159>:	pop    ebp
>   0x56555710 <+160>:	lea    esp,[ecx-0x4]
>   0x56555713 <+163>:	ret    
>   0x56555714 <+164>:	lea    eax,[ebx-0x15e7]
>   0x5655571a <+170>:	sub    esp,0xc
>   0x5655571d <+173>:	push   eax
>   0x5655571e <+174>:	call   0x565555f0 <puts@plt>
>   0x56555723 <+179>:	call   0x565558a0
>   0x56555728 <+184>:	add    esp,0x10
>   0x5655572b <+187>:	jmp    0x56555707 <main+151>
>End of assembler dump.
> ```

At <main+121> an anonymous routine is called, so let's print instructions:

> ```
>gdb-peda$ x/50i 0x56555920
>   0x56555920:	push   edi
>   0x56555921:	push   esi	
>   0x56555922:	push   ebx
>   0x56555923:	call   0x56555770
>   0x56555928:	add    ebx,0x16d8
>   0x5655592e:	sub    esp,0xc
>   0x56555931:	mov    esi,DWORD PTR [ebx+0x48]	
>   0x56555937:	push   esi
>   0x56555938:	call   0x56555610 <strlen@plt>
>   0x5655593d:	mov    DWORD PTR [esp],esi
>   0x56555940:	mov    edi,eax
>   0x56555942:	call   0x56555630 <__strdup@plt>
>   0x56555947:	mov    esi,eax
>   0x56555949:	pop    eax
>   0x5655594a:	pop    edx
>   0x5655594b:	push   edi
>   0x5655594c:	push   esi
>   0x5655594d:	call   0x56555640 <memfrob@plt>
>   0x56555952:	pop    ecx
>   0x56555953:	pop    edi
>   0x56555954:	push   esi
>   0x56555955:	push   DWORD PTR [esp+0x1c]
>   0x56555959:	call   0x56555590 <strcmp@plt>					<- key compared here
>   0x5655595e:	mov    DWORD PTR [esp],esi
>   0x56555961:	mov    edi,eax
>   0x56555963:	call   0x565555c0 <free@plt>
>   0x56555968:	add    esp,0x10
>   0x5655596b:	mov    eax,edi
>   0x5655596d:	pop    ebx
>   0x5655596e:	pop    esi
>   0x5655596f:	pop    edi
>   0x56555970:	ret    
>   ...
> ```

We can find a call to `strcmp`, probably comparing our string and the key, so we put a breakpoint at 0x56555959.

> ```
>gdb-peda$ continue
>[----------------------------------registers-----------------------------------]
>EAX: 0x56558818 ("aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8")
>EBX: 0x56557000 --> 0x1ef4 
>ECX: 0x56558818 ("aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8")
>EDX: 0x56558840 --> 0x0 
>ESI: 0x56558818 ("aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8")
>EDI: 0x28 ('(')
>EBP: 0xffffd2b8 --> 0x0 
>ESP: 0xffffd170 --> 0xffffd1a0 ("ABCD")
>EIP: 0x56555959 (call   0x56555590 <strcmp@plt>)
>EFLAGS: 0x246 (carry PARITY adjust ZERO sign trap INTERRUPT direction overflow)
>[-------------------------------------code-------------------------------------]
>   0x56555953:	pop    edi
>   0x56555954:	push   esi
>   0x56555955:	push   DWORD PTR [esp+0x1c]
>=> 0x56555959:	call   0x56555590 <strcmp@plt>
>   0x5655595e:	mov    DWORD PTR [esp],esi
>   0x56555961:	mov    edi,eax
>   0x56555963:	call   0x565555c0 <free@plt>
>   0x56555968:	add    esp,0x10
>Guessed arguments:
>arg[0]: 0xffffd1a0 ("ABCD")
>arg[1]: 0x56558818 ("aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8")
>[------------------------------------stack-------------------------------------]
>0000| 0xffffd170 --> 0xffffd1a0 ("ABCD")
>0004| 0xffffd174 --> 0x56558818 ("aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8")
>0008| 0xffffd178 --> 0xf7f31e69 (add    ebx,0x74197)
>0012| 0xffffd17c ("(YUV")
>0016| 0xffffd180 --> 0x56557000 --> 0x1ef4 
>0020| 0xffffd184 --> 0xffffd1a0 ("ABCD")
>0024| 0xffffd188 --> 0xf7fa6000 --> 0x1b2db0 
>0028| 0xffffd18c --> 0x565556ee (<main+126>:	add    esp,0x10)
> ```

We saw that the two strings passed as parameter are "ABCD" (our string) and the constant "aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8". So let's cat the flag!

> ```sh
>[adversary ~/lockedout]$ ./lock
>This is a pesky lock.. do you think you can open it?
>Enter key: aXat9r45UtyMjw4i5Wh8swVWmEg3vAbWZaijTWP8
>unlocked!
>sh-4.4$ id
>uid=1000(adversary) gid=1337(drevil) groups=1337(drevil)
>sh-4.4$ cat flag.txt
>IceCTF{you_m3ddling_k1ds}
> ```

Here is the binary:

f0VMRgEBAQAAAAAAAAAAAAMAAwABAAAALQcAADQAAAB0EQAAAAAAADQAIAAJACgAHQAcAAYAAAA0
AAAANAAAADQAAAAgAQAAIAEAAAUAAAAEAAAAAwAAAFQBAABUAQAAVAEAABMAAAATAAAABAAAAAEA
AAABAAAAAAAAAAAAAAAAAAAAlAwAAJQMAAAFAAAAABAAAAEAAADoDgAA6B4AAOgeAABkAQAAaAEA
AAYAAAAAEAAAAgAAAPQOAAD0HgAA9B4AAPAAAADwAAAABgAAAAQAAAAEAAAAaAEAAGgBAABoAQAA
RAAAAEQAAAAEAAAABAAAAFDldGSUCgAAlAoAAJQKAABMAAAATAAAAAQAAAAEAAAAUeV0ZAAAAAAA
AAAAAAAAAAAAAAAAAAAABgAAABAAAABS5XRk6A4AAOgeAADoHgAAGAEAABgBAAAEAAAAAQAAAC9s
aWIvbGQtbGludXguc28uMgAABAAAABAAAAABAAAAR05VAAAAAAACAAAABgAAACAAAAAEAAAAFAAA
AAMAAABHTlUAmxjhlJFe9HMrIAVWztVfqRqpk2ACAAAAFAAAAAEAAAAFAAAAACQAKBQAAAAVAAAA
a3+afK1L48AAAAAAAAAAAAAAAAAAAAAAegAAAAAAAAAAAAAAEgAAAJgAAAAAAAAAAAAAACAAAAA2
AAAAAAAAAAAAAAASAAAASgAAAAAAAAAAAAAAEgAAAJMAAAAAAAAAAAAAABIAAAA9AAAAAAAAAAAA
AAASAAAAYwAAAAAAAAAAAAAAEgAAAGsAAAAAAAAAAAAAACIAAAArAAAAAAAAAAAAAAASAAAAXAAA
AAAAAAAAAAAAEgAAALQAAAAAAAAAAAAAACAAAABDAAAAAAAAAAAAAAASAAAAgQAAAAAAAAAAAAAA
EgAAADAAAAAAAAAAAAAAABEAAAAiAAAAAAAAAAAAAAASAAAAGgAAAAAAAAAAAAAAEgAAAMMAAAAA
AAAAAAAAACAAAABSAAAAAAAAAAAAAAASAAAA1wAAAAAAAAAAAAAAIAAAAI4AAABwBgAAvQAAABIA
DgALAAAA/AkAAAQAAAARABAAAGxpYmMuc28uNgBfSU9fc3RkaW5fdXNlZABtZW1mcm9iAF9fc3Ry
ZHVwAHB1dHMAc3RkaW4AcHJpbnRmAGZnZXRzAHN0cmxlbgBzdHJjc3BuAHNldHJlc2dpZABzeXN0
ZW0AZ2V0ZWdpZABfX2N4YV9maW5hbGl6ZQBzdHJjbXAAX19saWJjX3N0YXJ0X21haW4AZnJlZQBf
SVRNX2RlcmVnaXN0ZXJUTUNsb25lVGFibGUAX19nbW9uX3N0YXJ0X18AX0p2X1JlZ2lzdGVyQ2xh
c3NlcwBfSVRNX3JlZ2lzdGVyVE1DbG9uZVRhYmxlAEdMSUJDXzIuMS4zAEdMSUJDXzIuMAAAAAAC
AAAAAgACAAIAAgACAAMAAgACAAAAAgACAAIAAgACAAAAAgAAAAEAAQABAAIAAQAAABAAAAAAAAAA
cx9pCQAAAwDxAAAAEAAAABBpaQ0AAAIA/QAAAAAAAADoHgAACAAAAOweAAAIAAAA9B8AAAgAAABE
IAAACAAAAEggAAAIAAAA5B8AAAYCAADoHwAABggAAOwfAAAGCwAA8B8AAAYOAAD4HwAABhEAAPwf
AAAGEwAADCAAAAcBAAAQIAAABwMAABQgAAAHBAAAGCAAAAcFAAAcIAAABwYAACAgAAAHBwAAJCAA
AAcJAAAoIAAABwoAACwgAAAHDAAAMCAAAAcNAAA0IAAABw8AADggAAAHEAAAPCAAAAcSAABTg+wI
6BMCAACBw6MaAACLg+z///+FwHQF6PYAAACDxAhbwwAAAAAAAAAAAP+zBAAAAP+jCAAAAAAAAAD/
owwAAABoAAAAAOng/////6MQAAAAaAgAAADp0P////+jFAAAAGgQAAAA6cD/////oxgAAABoGAAA
AOmw/////6McAAAAaCAAAADpoP////+jIAAAAGgoAAAA6ZD/////oyQAAABoMAAAAOmA/////6Mo
AAAAaDgAAADpcP////+jLAAAAGhAAAAA6WD/////ozAAAABoSAAAAOlQ/////6M0AAAAaFAAAADp
QP////+jOAAAAGhYAAAA6TD/////ozwAAABoYAAAAOkg/////6Po////ZpD/o+z///9mkI1MJASD
5PD/cfxVieVWU+jsAAAAgcN8GQAAUY216P7//42DMOr//4HsGAEAAFDoTf///42DC+r//4kEJOjv
/v//i4Pw////g8QM/zBoAAEAAFboCf///4PEEIXAdDmNgxfq//+D7AhQVujS/v//iTQkxoQF6P7/
/wDoMgIAAIPEEIXAdB+NgyPq//+D7AxQ6Oz+//+DxBCNZfQxwFlbXl2NYfzDjYMZ6v//g+wMUOjN
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
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYAgA
ABAIAAAAAAAAAQAAAAEAAAAMAAAAVAUAAA0AAADkCQAAGQAAAOgeAAAbAAAABAAAABoAAADsHgAA
HAAAAAQAAAD1/v9vrAEAAAUAAAAwAwAABgAAANABAAAKAAAABwEAAAsAAAAQAAAAFQAAAAAAAAAD
AAAAACAAAAIAAABoAAAAFAAAABEAAAAXAAAA7AQAABEAAACUBAAAEgAAAFgAAAATAAAACAAAAPv/
/28AAAAI/v//b2QEAAD///9vAQAAAPD//284BAAA+v//bwUAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHAGAAAAAAAAAAAAAPQeAAAAAAAA
AAAAAJYFAACmBQAAtgUAAMYFAADWBQAA5gUAAPYFAAAGBgAAFgYAACYGAAA2BgAARgYAAFYGAAAA
AAAARCAAAGgKAABHQ0M6IChEZWJpYW4gNi4zLjAtMTgrZGViOXUxKSA2LjMuMCAyMDE3MDUxNgAA
LnNoc3RydGFiAC5pbnRlcnAALm5vdGUuQUJJLXRhZwAubm90ZS5nbnUuYnVpbGQtaWQALmdudS5o
YXNoAC5keW5zeW0ALmR5bnN0cgAuZ251LnZlcnNpb24ALmdudS52ZXJzaW9uX3IALnJlbC5keW4A
LnJlbC5wbHQALmluaXQALnBsdC5nb3QALnRleHQALmZpbmkALnJvZGF0YQAuZWhfZnJhbWVfaGRy
AC5laF9mcmFtZQAuaW5pdF9hcnJheQAuZmluaV9hcnJheQAuamNyAC5keW5hbWljAC5nb3QucGx0
AC5kYXRhAC5ic3MALmNvbW1lbnQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAALAAAAAQAAAAIAAABUAQAAVAEAABMAAAAAAAAAAAAAAAEAAAAAAAAAEwAAAAcAAAACAAAA
aAEAAGgBAAAgAAAAAAAAAAAAAAAEAAAAAAAAACEAAAAHAAAAAgAAAIgBAACIAQAAJAAAAAAAAAAA
AAAABAAAAAAAAAA0AAAA9v//bwIAAACsAQAArAEAACQAAAAFAAAAAAAAAAQAAAAEAAAAPgAAAAsA
AAACAAAA0AEAANABAABgAQAABgAAAAEAAAAEAAAAEAAAAEYAAAADAAAAAgAAADADAAAwAwAABwEA
AAAAAAAAAAAAAQAAAAAAAABOAAAA////bwIAAAA4BAAAOAQAACwAAAAFAAAAAAAAAAIAAAACAAAA
WwAAAP7//28CAAAAZAQAAGQEAAAwAAAABgAAAAEAAAAEAAAAAAAAAGoAAAAJAAAAAgAAAJQEAACU
BAAAWAAAAAUAAAAAAAAABAAAAAgAAABzAAAACQAAAEIAAADsBAAA7AQAAGgAAAAFAAAAGAAAAAQA
AAAIAAAAfAAAAAEAAAAGAAAAVAUAAFQFAAAjAAAAAAAAAAAAAAAEAAAAAAAAAHcAAAABAAAABgAA
AIAFAACABQAA4AAAAAAAAAAAAAAAEAAAAAQAAACCAAAAAQAAAAYAAABgBgAAYAYAABAAAAAAAAAA
AAAAAAgAAAAAAAAAiwAAAAEAAAAGAAAAcAYAAHAGAAByAwAAAAAAAAAAAAAQAAAAAAAAAJEAAAAB
AAAABgAAAOQJAADkCQAAFAAAAAAAAAAAAAAABAAAAAAAAACXAAAAAQAAAAIAAAD4CQAA+AkAAJwA
AAAAAAAAAAAAAAQAAAAAAAAAnwAAAAEAAAACAAAAlAoAAJQKAABMAAAAAAAAAAAAAAAEAAAAAAAA
AK0AAAABAAAAAgAAAOAKAADgCgAAtAEAAAAAAAAAAAAABAAAAAAAAAC3AAAADgAAAAMAAADoHgAA
6A4AAAQAAAAAAAAAAAAAAAQAAAAEAAAAwwAAAA8AAAADAAAA7B4AAOwOAAAEAAAAAAAAAAAAAAAE
AAAABAAAAM8AAAABAAAAAwAAAPAeAADwDgAABAAAAAAAAAAAAAAABAAAAAAAAADUAAAABgAAAAMA
AAD0HgAA9A4AAPAAAAAGAAAAAAAAAAQAAAAIAAAAhgAAAAEAAAADAAAA5B8AAOQPAAAcAAAAAAAA
AAAAAAAEAAAABAAAAN0AAAABAAAAAwAAAAAgAAAAEAAAQAAAAAAAAAAAAAAABAAAAAQAAADmAAAA
AQAAAAMAAABAIAAAQBAAAAwAAAAAAAAAAAAAAAQAAAAAAAAA7AAAAAgAAAADAAAATCAAAEwQAAAE
AAAAAAAAAAAAAAABAAAAAAAAAPEAAAABAAAAMAAAAAAAAABMEAAALQAAAAAAAAAAAAAAAQAAAAEA
AAABAAAAAwAAAAAAAAAAAAAAeRAAAPoAAAAAAAAAAAAAAAEAAAAAAAAA