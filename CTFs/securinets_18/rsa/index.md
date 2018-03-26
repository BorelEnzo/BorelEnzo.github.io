## RSA Worst joke (Crypto)

### [~$ cd ..](../)

I love cryptography and RSA challenges, and this one was indeed a big joke.

The statement tells us that someone has setup a [public key](public.pem) was a big prime modulus. We have to decrypt the [message](flag.enc)

Wait, what ? A prime modulus ? o_O

Let's verify:

> ```bash
>~$ openssl rsa -noout -text -inform PEM -in public.pem -pubin 
>Public-Key: (2049 bit)
>Modulus:
>    01:21:3b:05:7d:6f:de:bc:45:27:b5:b7:42:1a:c1:
>    e9:3c:cf:fe:a7:6c:4f:08:74:99:1c:f3:fa:cf:0e:
>    ab:9e:f6:46:a2:18:39:82:0b:4d:65:3a:91:6b:68:
>    f2:58:e9:d3:f7:c2:ba:05:4e:4a:f0:5f:3e:4d:61:
>    b3:de:77:2d:2a:d2:33:8b:0e:ae:6e:a0:8f:20:76:
>    e4:e4:b0:8a:b2:09:20:fa:68:85:e6:c4:4d:ec:1b:
>    ee:28:a8:76:53:c7:4b:cb:9e:9f:12:94:de:8a:48:
>    bd:61:46:52:a3:d6:59:df:ce:7b:89:44:61:0f:25:
>    bf:af:93:6e:9a:54:16:7c:4d:22:7d:16:d3:2e:65:
>    ea:45:8b:69:d6:0d:ec:d7:fa:03:4c:1b:3b:d8:62:
>    71:71:64:e7:78:5e:b0:6d:cc:5b:88:ba:a2:62:e4:
>    31:20:e5:46:65:c0:cb:cb:3e:ad:51:b0:a0:08:19:
>    b4:e9:1d:48:72:d3:fb:e7:72:4e:03:ab:71:bc:af:
>    8b:b8:4c:74:de:c9:6a:ad:fc:b1:86:53:a8:f0:53:
>    93:d6:66:06:99:23:bc:7b:9b:31:36:3d:6d:6d:9b:
>    45:9f:46:db:5b:af:96:f8:40:4a:af:1a:83:1f:0d:
>    b8:aa:d7:d9:3a:42:56:e8:15:6b:2b:70:75:7a:01:
>    20:93
>Exponent: 65537 (0x10001)
> ```

And, yes, it's true, n is really a prime number. The factorisation will be quite easy...

Let's our friend Python do the job:

> ```python
>import base64
>from Crypto.Util import number
>
>file_cipher = open('flag.enc', 'r')
>cipher = base64.b64decode(file_cipher.read())
>file_cipher.close()
>n = 0x01213b057d6fdebc4527b5b7421ac1e93ccffea76c4f0874991cf3facf0eab9ef646a21839820b4d653a916b68f258e9d3f7c2ba054e4af05f3e4d61b3de772d2ad2338b0eae6ea08f2076e4e4b08ab20920fa6885e6c44dec1bee28a87653c74bcb9e9f1294de8a48bd614652a3d659dfce7b8944610f25bfaf936e9a54167c4d227d16d32e65ea458b69d60decd7fa034c1b3bd862717164e7785eb06dcc5b88baa262e43120e54665c0cbcb3ead51b0a00819b4e91d4872d3fbe7724e03ab71bcaf8bb84c74dec96aadfcb18653a8f05393d666069923bc7b9b31363d6d6d9b459f46db5baf96f8404aaf1a831f0db8aad7d93a4256e8156b2b70757a012093
>phi = n-1
>d = number.inverse(65537, phi)
>c = int(cipher.encode('hex'),16)
>print hex(pow(c, d, n))[2:-1].decode('hex')
>The empire secret system has been exposed ! The top secret flag is : Flag{S1nGL3_PR1m3_M0duLUs_ATT4cK_TaK3d_D0wn_RSA_T0_A_Sym3tr1c_ALg0r1thm}
> ```
