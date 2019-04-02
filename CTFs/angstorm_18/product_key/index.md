# Product Key (Reverse Engineering, 200pts)

### [~$ cd ..](../)

We are given a [binary](activate), which checks a license key, according to an email and a name:

> ```
>artemis.tosini@example.com
>Artemis Tosini
> ```

I first found 4 user-defined routine:

> ```
>(gdb) info functions
>All defined functions:
>
>Non-debugging symbols:
>...
>0x0000000000400806  swapArr
>0x00000000004008e9  sumChars
>0x0000000000400932  verify_key
>0x0000000000400ff8  main
>...
> ```

[All dumps here](dumps.txt)

## Routine sumChars
 
Let's begin by the routine `sumChars`:

> ```
>(gdb) disas sumChars
>Dump of assembler code for function sumChars:
>   0x00000000004008e9 <+0>:	push   %rbp
>   0x00000000004008ea <+1>:	mov    %rsp,%rbp
>   0x00000000004008ed <+4>:	mov    %rdi,-0x18(%rbp)
>   0x00000000004008f1 <+8>:	mov    %esi,-0x1c(%rbp)
>   0x00000000004008f4 <+11>:	mov    %edx,-0x20(%rbp)
>   0x00000000004008f7 <+14>:	mov    %ecx,-0x24(%rbp)
>   0x00000000004008fa <+17>:	movl   $0x0,-0x8(%rbp)
>   0x0000000000400901 <+24>:	mov    -0x1c(%rbp),%eax
>   0x0000000000400904 <+27>:	mov    %eax,-0x4(%rbp)
>   0x0000000000400907 <+30>:	jmp    0x400925 <sumChars+60>
>	|   0x0000000000400909 <+32>:	mov    -0x4(%rbp),%eax
>	|	0x000000000040090c <+35>:	movslq %eax,%rdx
>	|   0x000000000040090f <+38>:	mov    -0x18(%rbp),%rax
>	|   0x0000000000400913 <+42>:	add    %rdx,%rax
>	|   0x0000000000400916 <+45>:	movzbl (%rax),%eax
>	|   0x0000000000400919 <+48>:	movsbl %al,%eax
>	|   0x000000000040091c <+51>:	add    %eax,-0x8(%rbp)
>	|   0x000000000040091f <+54>:	mov    -0x24(%rbp),%eax
>	|   0x0000000000400922 <+57>:	add    %eax,-0x4(%rbp)
>	|   0x0000000000400925 <+60>:	mov    -0x4(%rbp),%eax
>	|   0x0000000000400928 <+63>:	cmp    -0x20(%rbp),%eax	
>	|   0x000000000040092b <+66>:	jl     0x400909 <sumChars+32>
>   0x000000000040092d <+68>:	mov    -0x8(%rbp),%eax
>   0x0000000000400930 <+71>:	pop    %rbp
>   0x0000000000400931 <+72>:	retq   
>End of assembler dump.
> ```

I knew that it was a kind of loop, where
* the upper limit is stored at `$rbp-0x20` (passed as parameter in `$edx`, line <+11>)
* the iterator is incremented by the value stored at `$rbp-0x24`, also passed as parameter in `$ecx`, line <+14>
* the lower limit is stored at `$rbp-0x1c` (passed as parameter in `$esi`, and put in `$eax` before the loop, line <+24>
The string to iterate is then passed is `$rdi`, and stored at `$rbp-0x18`.
The loops makes the sum of some characters, according to the iterator, and returns the result. In Python, it gives:

> ```python
>def sumchars(start, string, lim, inc):
>	i = start
>	res = 0
>	while i < lim:
>		res += ord(string[i])
>		i+=inc
>	return res
> ```

Next step is then to find the expected ouput.

## Find the expected output

The program computes a kind of checksum for each 4-digit number (stored at `$rbp-0x30` ), and compares them against constant values (at `$rbp-0x70`):

> ```
>0x0000000000400f9e <+1644>:	jmp    0x400fca <verify_key+1688>
>|	0x0000000000400fa0 <+1646>:	mov    -0xa4(%rbp),%eax
>|	0x0000000000400fa6 <+1652>:	cltq   
>|	0x0000000000400fa8 <+1654>:	mov    -0x70(%rbp,%rax,4),%edx
>|	0x0000000000400fac <+1658>:	mov    -0xa4(%rbp),%eax
>|	0x0000000000400fb2 <+1664>:	cltq   
>|	0x0000000000400fb4 <+1666>:	mov    -0x30(%rbp,%rax,4),%eax
>|	0x0000000000400fb8 <+1670>:	cmp    %eax,%edx
>|	0x0000000000400fba <+1672>:	je     0x400fc3 <verify_key+1681>
>|	0x0000000000400fbc <+1674>:	movb   $0x0,-0xc9(%rbp)
>|	0x0000000000400fc3 <+1681>:	addl   $0x1,-0xa4(%rbp)
>0x0000000000400fca <+1688>:	cmpl   $0x5,-0xa4(%rbp)
>0x0000000000400fd1 <+1695>:	jle    0x400fa0 <verify_key+1646>
> ```

These constant values are the following:

> ```
>0x7fffffffe070:	0x000007f8	0x00001780	0x00000b94	0x000001f8
>0x7fffffffe080:	0x00000b4b	0x000011f8
> ```

I had then to reverser the process to find the right input.

## Script

I will not get lost into details, since the process is quite long. One import thing is that the program will put a padding after the email and the name, always the same:

> ```python
>mail = "dwq`hlv+qjvlklE`}dhui`+fjhr9\033n<\026"
>name = "N}{jbf|/[`|faf-\x00+\x0d|Vhk.|\x18\x11bT\x0bij^"
> ```

Then, checksums for each 4-digit number of the key are computed and put in an array. The array is swapped, always in the same way (no need to reverse `swapArr`), 
I only put a breakpoint before the swapping and one after, and reversed the process:

> ```python
>def unswap(array):
>	res = []
>	res.append(array[4])
>	res.append(array[0])
>	res.append(array[1])
>	res.append(array[5])
>	res.append(array[2])
>	res.append(array[3])
>	return res
> ```

And finally, two checksum are computed and new values are compared against the constant values.
I wrote a [script](sumchars.py), and the output was:

**[3914, 6104, 4611, 1711, 1243, 4699]**

