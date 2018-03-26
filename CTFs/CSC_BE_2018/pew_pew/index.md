# Pew pew

### [~$ cd ..](../)

The goal was to retrieve a key in the binary. The command `strings` gave us nothing, and then we called our good friend GDB. Old school way, but still efficient!

> ```bash
> pew_pew: ELF 32-bit LSB shared object, Intel 80386, version 1 (SYSV), dynamically linked, interpreter /lib/ld-linux.so.2, for GNU/Linux 3.2.0, BuildID[sha1]=7d8f1bd18e24ccf884a3e5df27b5868cd38d4281, stripped
> ```

The binary is stripped, it would be not so easy.
First thing we did was to dump the code with `objdump -x -D pew_pew` [here](objdump.txt)
We first looked for the entry point:

> ```
>(gdb) break __libc_start_main
>Breakpoint 1 at 0x4e0
>(gdb) run
>Starting program: /home/enzo/Documents/WH/CTF/CSC/pew_pew 
>
>Breakpoint 1, 0xf7e0a190 in __libc_start_main () from /lib32/libc.so.6
>(gdb) disass  __libc_start_main
>Dump of assembler code for function __libc_start_main:
>=> 0xf7e0a190 <+0>:	push   %ebp
>...
>   0xf7e0a282 <+242>:	call   *0x70(%esp)	;just here, before exit
>   0xf7e0a286 <+246>:	add    $0x10,%esp
>   0xf7e0a289 <+249>:	sub    $0xc,%esp
>   0xf7e0a28c <+252>:	push   %eax
>   0xf7e0a28d <+253>:	call   0xf7e20800 <exit>
>   0xf7e0a292 <+258>:	xor    %eax,%eax
>   0xf7e0a294 <+260>:	jmp    0xf7e0a1be <__libc_start_main+46>
>...   
>End of assembler dump.
>(gdb) break *0xf7e0a282
>Breakpoint 2 at 0xf7e0a282
>(gdb) continue
>Continuing.
>
>Breakpoint 2, 0xf7e0a282 in __libc_start_main () from /lib32/libc.so.6
>(gdb) stepi
>0x56555849 in ?? ()
> ```

Okay, nice we are now in a new routine. We can now get the code using the 3 last digits of the address (849) in objdump.txt.
The program reads 128 chars from the stdin, and replaces the carriage return with a null byte. The interesting code starts here:

> ```
>907:	8d 85 48 ff ff ff    	lea    -0xb8(%ebp),%eax
>90d:	50                   	push   %eax
>90e:	e8 4a fd ff ff       	call   65d <atoi@plt+0x15d>
> ```

## Find the right format

We can see that the address of our string ( `$ebp-0xb8` ) is passed as parameter to a custom routine (not the `atoi` actually)
The code located at this address is as follows:

> ```
>65d:	55                   	push   %ebp
>65e:	89 e5                	mov    %esp,%ebp
>660:	53                   	push   %ebx
>661:	83 ec 04             	sub    $0x4,%esp
>664:	e8 8e 03 00 00       	call   9f7 <atoi@plt+0x4f7>
>669:	05 97 19 00 00       	add    $0x1997,%eax
>66e:	83 ec 0c             	sub    $0xc,%esp
>671:	ff 75 08             	pushl  0x8(%ebp)
>674:	89 c3                	mov    %eax,%ebx
>676:	e8 55 fe ff ff       	call   4d0 <strlen@plt>
>67b:	83 c4 10             	add    $0x10,%esp
>67e:	83 f8 18             	cmp    $0x18,%eax
>681:	0f 94 c0             	sete   %al
>684:	0f b6 c0             	movzbl %al,%eax
>687:	8b 5d fc             	mov    -0x4(%ebp),%ebx
>68a:	c9                   	leave  
>68b:	c3                   	ret 
> ```

Okay, nice! Because of the line 0x67e, we can deduce that the routine will return true if `strlen` returns 0x18 (24), so we can guess that our string should be 24 chars long.
Indeed, in the main routine, we have this after the call to 0x65d:

> ```
>913:	83 c4 10             	add    $0x10,%esp
>916:	85 c0                	test   %eax,%eax
>918:	74 16                	je     930 <atoi@plt+0x430>
>91a:	83 ec 0c             	sub    $0xc,%esp
>91d:	8d 85 48 ff ff ff    	lea    -0xb8(%ebp),%eax
>923:	50                   	push   %eax
>924:	e8 63 fd ff ff       	call   68c <atoi@plt+0x18c>
> ```

If the routine returns 0, a jump is done (will print that the key is not correct and exit). However, if the key is 24 chars long (without the carriage return), another routine is called:

> ```
>68c:	55                   	push   %ebp
>68d:	89 e5                	mov    %esp,%ebp
>68f:	53                   	push   %ebx
>690:	83 ec 14             	sub    $0x14,%esp
>693:	e8 c8 fe ff ff       	call   560 <atoi@plt+0x60>
>698:	81 c3 68 19 00 00    	add    $0x1968,%ebx
>69e:	c7 45 f4 00 00 00 00 	movl   $0x0,-0xc(%ebp)
>6a5:	eb 64                	jmp    70b <atoi@plt+0x20b>
>	6a7:	8b 4d f4             	mov    -0xc(%ebp),%ecx
>	6aa:	ba 67 66 66 66       	mov    $0x66666667,%edx
>	6af:	89 c8                	mov    %ecx,%eax
>	6b1:	f7 ea                	imul   %edx
>	6b3:	d1 fa                	sar    %edx
>	6b5:	89 c8                	mov    %ecx,%eax
>	6b7:	c1 f8 1f             	sar    $0x1f,%eax
>	6ba:	29 c2                	sub    %eax,%edx
>	6bc:	89 d0                	mov    %edx,%eax
>	6be:	c1 e0 02             	shl    $0x2,%eax
>	6c1:	01 d0                	add    %edx,%eax
>	6c3:	29 c1                	sub    %eax,%ecx
>	6c5:	89 ca                	mov    %ecx,%edx
>	6c7:	83 fa 04             	cmp    $0x4,%edx
>	6ca:	75 16                	jne    6e2 <atoi@plt+0x1e2>
>	6cc:	8b 55 f4             	mov    -0xc(%ebp),%edx
>	6cf:	8b 45 08             	mov    0x8(%ebp),%eax
>	6d2:	01 d0                	add    %edx,%eax
>	6d4:	0f b6 00             	movzbl (%eax),%eax
>	6d7:	3c 2d                	cmp    $0x2d,%al
>	6d9:	74 2c                	je     707 <atoi@plt+0x207>
>	6db:	b8 00 00 00 00       	mov    $0x0,%eax
>	6e0:	eb 45                	jmp    727 <atoi@plt+0x227>
>	6e2:	8b 55 f4             	mov    -0xc(%ebp),%edx
>	6e5:	8b 45 08             	mov    0x8(%ebp),%eax
>	6e8:	01 d0                	add    %edx,%eax
>	6ea:	0f b6 00             	movzbl (%eax),%eax
>	6ed:	3c 2f                	cmp    $0x2f,%al
>	6ef:	7e 0f                	jle    700 <atoi@plt+0x200>
>	6f1:	8b 55 f4             	mov    -0xc(%ebp),%edx
>	6f4:	8b 45 08             	mov    0x8(%ebp),%eax
>	6f7:	01 d0                	add    %edx,%eax
>	6f9:	0f b6 00             	movzbl (%eax),%eax
>	6fc:	3c 39                	cmp    $0x39,%al
>	6fe:	7e 07                	jle    707 <atoi@plt+0x207>
>	700:	b8 00 00 00 00       	mov    $0x0,%eax
>	705:	eb 20                	jmp    727 <atoi@plt+0x227>
>	707:	83 45 f4 01          	addl   $0x1,-0xc(%ebp)
>70b:	83 ec 0c             	sub    $0xc,%esp
>70e:	ff 75 08             	pushl  0x8(%ebp)
>711:	e8 ba fd ff ff       	call   4d0 <strlen@plt>
>716:	83 c4 10             	add    $0x10,%esp
>719:	89 c2                	mov    %eax,%edx
>71b:	8b 45 f4             	mov    -0xc(%ebp),%eax
>71e:	39 c2                	cmp    %eax,%edx
>720:	77 85                	ja     6a7 <atoi@plt+0x1a7>
>722:	b8 01 00 00 00       	mov    $0x1,%eax
>727:	8b 5d fc             	mov    -0x4(%ebp),%ebx
>72a:	c9                   	leave  
>72b:	c3                   	ret    
> ```

We guess that is a for loop. The pseudo code is something like this:

> ```python
>i = 0
>if len(key) == 0:
>	exit
>for i in range(len(key)): //0x6d7
>	if i % 4 == 0:
>		if string[i] != '-':
>			exit
> 	else:
>		if !is_number(string[i]) //0x6ed -> 0x6fc
>			exit
> ```

We know then that we should submit something like this: ????-????-????-???? where ? is a number. Indeed, we'll receive the message "Wrong key format" if the format doesn't match.
Let's go back to the main routine, after this first check:

> ```
>94c:	83 ec 08             	sub    $0x8,%esp
>94f:	8d 83 aa ea ff ff    	lea    -0x1556(%ebx),%eax
>955:	50                   	push   %eax
>956:	8d 85 48 ff ff ff    	lea    -0xb8(%ebp),%eax
>95c:	50                   	push   %eax
>95d:	e8 8e fb ff ff       	call   4f0 <strtok@plt>
> ```

The call to `strtok` will split the key using the character '-':

> ```
>Breakpoint 4, 0x5655595d in ?? ()
>(gdb) x/2x $esp
>	0xffffd0b0:	0xffffd140	0x56555aaa
>(gdb) x/s 0xffffd140
>	0xffffd140:	"9876-5432-1098-7654-3210"
>(gdb) x/s 0x56555aaa
>	0x56555aaa:	"-"
> ```

## Part 1

Each block of the key will be checked with a different routine. Their address are not known, since the their are resolved at this line:

> ```
> 98a:	ff d6                	call   *%esi
> ```

Then we break juste before, and use `stepi`:

> ```
>(gdb) break *0x5655598a
	Breakpoint 5 at 0x5655598a
>(gdb) c
>	Continuing.
>
>Breakpoint 5, 0x5655598a in ?? ()
>(gdb) stepi
>0x5655572c in ?? ()
> ```

In 0x72c we can find this code:

> ```
>72c:	55                   	push   %ebp
>72d:	89 e5                	mov    %esp,%ebp
>72f:	e8 c3 02 00 00       	call   9f7 <atoi@plt+0x4f7>
>734:	05 cc 18 00 00       	add    $0x18cc,%eax
>739:	b8 10 27 00 00       	mov    $0x2710,%eax
>73e:	2b 45 08             	sub    0x8(%ebp),%eax
>741:	3d 63 01 00 00       	cmp    $0x163,%eax
>746:	0f 94 c0             	sete   %al
>749:	0f b6 c0             	movzbl %al,%eax
>74c:	5d                   	pop    %ebp
>74d:	c3                   	ret  
> ```

Nice, the first part of the key is compared against a constant:
* eax = 0x2710
* eax -= key
* eax == 0x163 ?

To satisfy the condition, the first part of the key has to be: 0x2710-0x163 = **9645**

## Part 2

To find the next check location, we repeated the same process: reach the call to $esi, skip the first check, and jump with the command `stepi`:

> ```
>74e:	55                   	push   %ebp
>74f:	89 e5                	mov    %esp,%ebp
>751:	e8 a1 02 00 00       	call   9f7 <atoi@plt+0x4f7>
>756:	05 aa 18 00 00       	add    $0x18aa,%eax
>75b:	81 7d 08 7a 16 00 00 	cmpl   $0x167a,0x8(%ebp)
>762:	7e 34                	jle    798 <atoi@plt+0x298>
>764:	81 7d 08 a7 16 00 00 	cmpl   $0x16a7,0x8(%ebp)
>76b:	7f 2b                	jg     798 <atoi@plt+0x298>
>76d:	8b 4d 08             	mov    0x8(%ebp),%ecx
>770:	ba 1f 85 eb 51       	mov    $0x51eb851f,%edx
>775:	89 c8                	mov    %ecx,%eax
>777:	f7 ea                	imul   %edx
>779:	c1 fa 04             	sar    $0x4,%edx
>77c:	89 c8                	mov    %ecx,%eax
>77e:	c1 f8 1f             	sar    $0x1f,%eax
>781:	29 c2                	sub    %eax,%edx
>783:	89 d0                	mov    %edx,%eax
>785:	6b c0 32             	imul   $0x32,%eax,%eax
>788:	29 c1                	sub    %eax,%ecx
>78a:	89 c8                	mov    %ecx,%eax
>78c:	83 f8 23             	cmp    $0x23,%eax
>78f:	75 07                	jne    798 <atoi@plt+0x298>
>791:	b8 01 00 00 00       	mov    $0x1,%eax
>796:	eb 05                	jmp    79d <atoi@plt+0x29d>
>798:	b8 00 00 00 00       	mov    $0x0,%eax
>79d:	5d                   	pop    %ebp
>79e:	c3                   	ret    
> ```

We can guess that the second part should be between 0x167a and 0x16a7 (5754 - 5799). Some computations are done, and the result should be 0x23.
We then wrote this pseudo code:

> ```python
> edx = 0x51eb851f
> edx = ((key * edx) & 0xffff00000000) >> 36
> eax = edx - msb(key)
> eax *= 0x32
> eax = key - eax
> eax == 0x23 ?
> ```

Let our friend Python search the solution for us:

> ```python
>def brute(i):
>	edx = 0x51eb851f
>	edx = ((i * edx) & 0xffff00000000) >> 36
>	eax = i >> 31
>	eax = edx-eax
>	eax = (eax * 0x32) & 0xffffffff
>	eax = i - eax
>	return eax == 0x23
>
>for i in range(0x167a, 0x16a7+1, 1):
>	if brute(i):
>		print i
>		exit(0)
> ```

The answer appears: **5785**

## Step 3

> ```
>79f:	55                   	push   %ebp
>7a0:	89 e5                	mov    %esp,%ebp
>7a2:	e8 50 02 00 00       	call   9f7 <atoi@plt+0x4f7>
>7a7:	05 59 18 00 00       	add    $0x1859,%eax
>7ac:	81 7d 08 f0 0c 00 00 	cmpl   $0xcf0,0x8(%ebp)
>7b3:	0f 94 c0             	sete   %al
>7b6:	0f b6 c0             	movzbl %al,%eax
>7b9:	5d                   	pop    %ebp
>7ba:	c3                   	ret
> ```

Pretty straightforward! The third part is then **3312** (0xcf0)

## Step 4

> ```
>7bb:	55                   	push   %ebp
>7bc:	89 e5                	mov    %esp,%ebp
>7be:	83 ec 10             	sub    $0x10,%esp
>7c1:	e8 31 02 00 00       	call   9f7 <atoi@plt+0x4f7>
>7c6:	05 3a 18 00 00       	add    $0x183a,%eax
>7cb:	c7 45 f0 02 00 00 00 	movl   $0x2,-0x10(%ebp)
>7d2:	c7 45 f4 11 00 00 00 	movl   $0x11,-0xc(%ebp)
>7d9:	c7 45 f8 07 01 00 00 	movl   $0x107,-0x8(%ebp)
>7e0:	c7 45 fc 00 00 00 00 	movl   $0x0,-0x4(%ebp)
>7e7:	eb 2e                	jmp    817 <atoi@plt+0x317>
>7e9:	8b 45 fc             	mov    -0x4(%ebp),%eax
>	7ec:	8b 4c 85 f0          	mov    -0x10(%ebp,%eax,4),%ecx
>	7f0:	8b 45 08             	mov    0x8(%ebp),%eax
>	7f3:	99                   	cltd   
>	7f4:	f7 f9                	idiv   %ecx
>	7f6:	89 d0                	mov    %edx,%eax
>	7f8:	85 c0                	test   %eax,%eax
>	7fa:	74 07                	je     803 <atoi@plt+0x303>
>		7fc:	b8 00 00 00 00       	mov    $0x0,%eax
>		801:	eb 24                	jmp    827 <atoi@plt+0x327>
>	803:	8b 45 fc             	mov    -0x4(%ebp),%eax
>	806:	8b 4c 85 f0          	mov    -0x10(%ebp,%eax,4),%ecx
>	80a:	8b 45 08             	mov    0x8(%ebp),%eax
>	80d:	99                   	cltd   
>	80e:	f7 f9                	idiv   %ecx
>	810:	89 45 08             	mov    %eax,0x8(%ebp)
>	813:	83 45 fc 01          	addl   $0x1,-0x4(%ebp)
>	817:	83 7d fc 02          	cmpl   $0x2,-0x4(%ebp)
>81b:	7e cc                	jle    7e9 <atoi@plt+0x2e9>
>81d:	83 7d 08 01          	cmpl   $0x1,0x8(%ebp)
>821:	0f 94 c0             	sete   %al
>824:	0f b6 c0             	movzbl %al,%eax
>827:	c9                   	leave  
>828:	c3                   	ret
> ```

Once again, we have a for loop, and at each round, the key is divided by the number located at $ebp+(4*i)-16. If the remainder is not 0, the routine returns false.
It's therefore quite simple to guess the correct value, since $ebp-8, $ebp-12 and $ebp-16 are known: 0x2, 0x11 and 0x107. The answer is therefore the product: **8942**.

## Last step

> ```
>829:	55                   	push   %ebp
>82a:	89 e5                	mov    %esp,%ebp
>82c:	e8 c6 01 00 00       	call   9f7 <atoi@plt+0x4f7>
>831:	05 cf 17 00 00       	add    $0x17cf,%eax
>836:	8b 45 08             	mov    0x8(%ebp),%eax
>839:	c1 e0 08             	shl    $0x8,%eax
>83c:	3d 00 8d 1c 00       	cmp    $0x1c8d00,%eax
>841:	0f 94 c0             	sete   %al
>844:	0f b6 c0             	movzbl %al,%eax
>847:	5d                   	pop    %ebp
>848:	c3   					ret
>```

The value is then shifted by 8 bits, and the result is compared against the constant 0x1c8d00. The correct value is then 0x1c8d00 >> 8 = **7309**.
If we run the once again with the key, we get:

> ```
>Good job! The flag is CSCBE{9645-5785-3312-8942-7309}
> ```
