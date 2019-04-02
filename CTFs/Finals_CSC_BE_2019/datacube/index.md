Datacube - Crypto (150 pts)

### [~$ cd ..](../)

Unfortunately, we didn't manage to solve this challenge during the contest, even if we spotted the vulnerability, but the script we wrote didn't gave us the solution :(

>We uncovered this encrypted datacube, can you recover the contents?

The [file]() we are given contains only 2 value, named `N` and  `c`:

>N = 634504ee7770f969cfb8d9065fdbe37c58ca33640a0ee82f6d50b0b6a451a18519b83ff730a4fc00232f7244ebd1ec4f1608613fbfcf4838bfec5a53466898eca14a41ece3db9a79b8a7d7460075b45300a011485508210cc534dcb400eaf9c616de3dd0612db41cd0363a5c49269a17ee04be80b50da93a226949b88baceb7d576473e8a2ab0b6dd46073b2f80980e7b45a8b3421fefdfcbdfa8f8262340844a11bf1681b30405967992f5afb3891ad85289eb79ad7e3e71a0861af8eb1b9ebcc3d6d454a193fd78cf7cf496345e50ba516bf86e02f89c21c443a5b8908670860d2ddc6f  
>c = 14aa1e31e4c5ddcc4da5bf7716da53a532f4bce4ab0cca7489f849981c14edfdc486122c4f74e3d3f0ca375161a8c58b067fdd886e4a3cb29d77a3b2c27580b31ba3b2bba50212df8467655a950257aef8df0591f4a81c82a12216408a5af9a5f988069c5c0b49a7966ea8370dcee275decbc1c081e66ea0c802e0aac8aa33b9ed8eebc55533270c442bbbc81507b40b8a8e36507322ef4ba1371346c0020866791319ac327ae4e87f0af13fbf8e800d4e918d40f6bc7e9534bc60617e3021d40470cb181f1a1da6b755f8cd2291f6249ad0be20024b649b6ba84de31ad4f43823192d048

It looks like an RSA challenge (and I really love RSA challenges), but the public exponent is missing. Because of the name, I guessed that it was `e=3`, making a small exponent attack possible. There was only one ciphertext, we were then unable to perform a Hastad's broadcast attack, but we could try to compute the cubic root of N.

Two scenarios were then possible:

* the plaintext, once raised to the power of 3, is still lower than the modulus. In this case, we only have to compute the cubic root
* the plaintext, once raised to the power of 3, is greater than the modulus, and in this case, we have: `plain = cubic_root(cipher + k * modulus)`, where `k` must be bruteforced.

We then used the following function, computing the cubic root:

```python
def cuberoot(x):
	y, y1 = None, 2
	while y != y1:
		y = y1
		y3 = y ** 3
		d = (2 * y3 + x)
		y1 = (y * (y3 + 2 * x) + d // 2) // d
	if pow(y,3) != x:
		return -1
	else:
		return y
```

If the cubic root is perfect root, `y` is returned, -1 otherwise. We then added our brute-force loop, and waited:

```python
message = 0x14aa1e31e4c5ddcc4da5bf7716da53a532f4bce4ab0cca7489f849981c14edfdc486122c4f74e3d3f0ca375161a8c58b067fdd886e4a3cb29d77a3b2c27580b31ba3b2bba50212df8467655a950257aef8df0591f4a81c82a12216408a5af9a5f988069c5c0b49a7966ea8370dcee275decbc1c081e66ea0c802e0aac8aa33b9ed8eebc55533270c442bbbc81507b40b8a8e36507322ef4ba1371346c0020866791319ac327ae4e87f0af13fbf8e800d4e918d40f6bc7e9534bc60617e3021d40470cb181f1a1da6b755f8cd2291f6249ad0be20024b649b6ba84de31ad4f43823192d048
modulus = 0x634504ee7770f969cfb8d9065fdbe37c58ca33640a0ee82f6d50b0b6a451a18519b83ff730a4fc00232f7244ebd1ec4f1608613fbfcf4838bfec5a53466898eca14a41ece3db9a79b8a7d7460075b45300a011485508210cc534dcb400eaf9c616de3dd0612db41cd0363a5c49269a17ee04be80b50da93a226949b88baceb7d576473e8a2ab0b6dd46073b2f80980e7b45a8b3421fefdfcbdfa8f8262340844a11bf1681b30405967992f5afb3891ad85289eb79ad7e3e71a0861af8eb1b9ebcc3d6d454a193fd78cf7cf496345e50ba516bf86e02f89c21c443a5b8908670860d2ddc6f

k = 0
while True:
    p = cuberoot(message + modulus * k)
    if pow(p, 3, modulus) == message:
        print p
        break
    k += 1
```

After a couple of minutes, the script returned the number `77680526357669271311723739587223093082081422484719902382375874388054436488932279137135879248801939864369376125118100002498798552125977877375214098489309554646267174422088999672791774589`:

giving us, in ASCII:

```python
hex(77680526357669271311723739587223093082081422484719902382375874388054436488932279137135879248801939864369376125118100002498798552125977877375214098489309554646267174422088999672791774589)[2:-1].decode("hex")
	"I believe the flag you're looking for is CSC{wh0_c4r3s_ab0ut_p4dd1ng_anyw4y5}"
```
