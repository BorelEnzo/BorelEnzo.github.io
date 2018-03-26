## OFB (Crypto, 120 pts)

### [~$ cd ..](../)

> ```
>defund made a simple OFB cipher, if you can even call it that. Here's the source and the encrypted flag.
> ```

Get the [source](encrypt.py)
Get the [cipher text](flag.png.enc)

Since the name of the file containing the flag in "flag.png.enc", we can guess that it was a PNG picture, and therefore we can guess the first bytes of the
plaintext:

> ```bash
>hexdump -C sample.png|head
>00000000  89 50 4e 47 0d 0a 1a 0a  00 00 00 0d 49 48 44 52  |.PNG........IHDR|
>...
> ```

Let's take a look at the source code:

> ```python
>import struct
>
>def lcg(m, a, c, x):
>	return (a*x + c) % m
>
>m = pow(2, 32)
>
>with open('lcg') as f:
>	a = int(f.readline())
>	c = int(f.readline())
>	x = int(f.readline())
>
>d = open('flag.png').read()
>d += '\x00' * (-len(d) % 4)
>d = [d[i:i+4] for i in range(0, len(d), 4)]
>
>e = ''
>for i in range(len(d)):
>	e += struct.pack('>I', x ^ struct.unpack('>I', d[i])[0])
>	x = lcg(m, a, c, x)
>
>with open('flag.png.enc', 'w') as f:
>	f.write(e)
>	f.close()
> ```

We can see that the script will work with 4-bytes blocks, and that each block will be xor'ed with `x`, updated at each round.
Since we know the beginning of the plaintext, we can find the output of `lcg`, and then try to solve a system of 2 equations
in order to find the constants `a` and `c`, and the initial value of `x`.

# Step 1: Find 2 pairs ciphertext-plaintext

At the first round, `a` and `c` don't have any impact on `e` (the cipher text), and therefore, the initial value of `x` is `plain[0]^cipher[0]`.
We then compute the output of the `lcm` for the two first blocks:

> ```bash
>~$ hexdump -C flag.png.enc|head -n 1 
>	00000000  18 9a 6d 45 89 a2 9c 3f  4e b6 9d 78 b3 6f 89 27  |..mE...?N..x.o.'|
>~$ hexdump -C ~/Images/logo_small.png|head -n 1
>	00000000  89 50 4e 47 0d 0a 1a 0a  00 00 00 0d 49 48 44 52  |.PNG........IHDR|
> ```

By xoring 32-bits values, we get `x`, `x1`
* 0x189a6d45 ^ 0x89504e47 = 2445943554
* 0x0d0a1a0a ^ 0x89a29c3f = 2225636917

# Step 2 Solve the equations
 
According to the `lcm` function, we have 2 equations, with `m = pow(2,32) = 4294967296` :

* a * x + c % m =>
* a * 2445943554 + c = 2225636917 % m
* a * 2225636917 + c = 1320590709 % m

We can write it as follows (all computations are done modulo m):

> ```
> a * 2445943554 - 2225636917 = a * 2225636917 - 1320590709
> => a * (2445943554 - 2225636917) = -1320590709 + 2225636917 (mod m)
> => a * 220306637 = 905046208 (mod m)
> ```

invert 220306637 modulo m by multiplying by 3166154757:

> ```
> => a * 220306637 * 3166154757 = 905046208 * 3166154757 (mod m)
> => a = 2865516356764011456 (mod m)
> => a = 3204287424
> ```

And then let's compute `c`:

> ```
> a * 2445943554 + c = 2225636917 (mod m)
> => 3204287424 * 2445943554 + c = 2225636917 (mod m)
> => 7837506169896064896 + c = 2225636917 (mod m)
> => 7837506169896064896 - 2225636917 = -c (mod m)
> => 7837506167670427979 = -c (mod m)
> => c = -2834157899 (mod m)
> => c = 1460809397
> ```

# Step 3 Script it and get the flag

[Full script here](decrypt_ofb.py)

I then rewrote the script by setting appropriate values for `x`, `a` and `c`, and got the flag:

![flag](flag.png)
