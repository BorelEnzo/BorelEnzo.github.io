# Blind date

### [~$ cd ..](../)

We are given this [file](BlinDate.jpeg), which is supposed to be a JPEG picture.
However, the file is invalid

> ```sh
>$ xxd BlindDate.jpeg
>00000000: e0ff d8ff 464a 1000 0100 4649 6000 0101  ....FJ....FI`...
>00000010: 0000 6000 2200 e1ff 6669 7845 4d4d 0000  ..`."...fixEMM..
>00000020: 0000 2a00 0100 0800 0300 1201 0100 0000  ..*.............
>00000030: 0000 0100 0000 0000 1100 ecff 6b63 7544  ............kcuD
>00000040: 0001 0079 0000 0004 ff00 004b 687e 03e1  ...y.......Kh~..
>00000050: 3a70 7474 736e 2f2f 6f64 612e 632e 6562  :pttsn//oda.c.eb
>00000060: 782f 6d6f 312f 7061 002f 302e 7078 3f3c  x/mo1/pa./0.px?<
>00000070: 656b 6361 6562 2074 3d6e 6967 bfbb ef22  ekcaeb t=nig..."
>00000080: 6469 2022 3557 223d 704d 304d 6968 6543  di "5W"=pM0MiheC
>00000090: 6572 7a48 544e 7a53 636b 7a63 3f22 6439  erzHTNzSckzc?"d9
> ...
>00009630: d9ff 008a 6734 694c 7541 4349 7541 4349  ....g4iLuACIuACI
>00009640: 6741 694c 6734 694c 7541 4349 7541 4349  gAiLg4iLuACIuACI
>00009650: 6741 694c 6734 694c 7541 4349 7541 4349  gAiLg4iLuACIuACI
>00009660: 6741 694c 6741 694c 4e34 694c 6734 6943  gAiLgAiLN4iLg4iC
>00009670: 6741 4349 6741 694c 6734 4349 6741 4349  gACIgAiLg4CIgACI
>00009680: 6741 4349 6741 694c 6741 4349 6734 4349  gACIgAiLgACIg4CI
>00009690: 6741 4349 6741 694c 6734 4349 6734 4349  gACIgAiLg4CIg4CI
>000096a0: 4b30 4149 6741 4349 7534 4349 6741 4349  K0AIgACIu4CIgACI
>000096b0: 6741 4349 6741 4349 7534 4349 6741 4349  gACIgACIu4CIgACI
>000096c0: 6741 4349 6741 694c 7534 4349 6741 4349  gACIgAiLu4CIgACI
>000096d0: 6741 4349 6741 694c 4b50 674c 0014 0403  gACIgAiLKPgL....
>000096e0: 0900 6300 6dad ea4c c3b1 56bc 8300 0000  ..c.m..L..V.....
>000096f0: 9c01 0000 0800 0b00 666c 6167 2e74 7874  ........flag.txt
>00009700: 0199 0700 0100 4145 0308 009f ad41 bd4e  ......AE.....A.N
>00009710: df1c 3741 7ecf d045 22b4 7549 eb2d e068  ..7A~..E".uI.-.h
>00009720: 5c98 2e00 b3c6 4b2c 5338 0f2c b9c0 8389  \.....K,S8.,....
>00009730: 1e34 08b2 6444 8cb9 440b d2cb f6a2 3513  .4..dD..D.....5.
>00009740: 0bd9 d8c4 758e e11e ba06 4a1e 380a 12c3  ....u.....J.8...
>00009750: 1e29 1857 927b 0214 3d43 b107 67da cbe1  .).W.{..=C..g...
>00009760: 4333 47bd cd44 8e58 3a20 74ca c9f2 301d  C3G..D.X: t...0.
>00009770: 13a1 7bdf 1689 f9fd eb75 c74d 8960 d142  ..{......u.M.`.B
>00009780: 1ae3 d5e8 5b07 9a71 eafd 2c3b f05b 504b  ....[..q..,;.[PK
>00009790: 0708 c3b1 56bc 8300 0000 9c01 0000 504b  ....V.........PK
>000097a0: 0102 1f00 1400 0900 6300 6dad ea4c c3b1  ........c.m..L..
>000097b0: 56bc 8300 0000 9c01 0000 0800 2f00 0000  V.........../...
>000097c0: 0000 0000 2000 0000 0000 0000 666c 6167  .... .......flag
>000097d0: 2e74 7874 0a00 2000 0000 0000 0100 1800  .txt.. .........
>000097e0: 099d 00e4 7d18 d401 9589 c762 7d18 d401  ....}......b}...
>000097f0: 1ff0 ae6f 22d3 d301 0199 0700 0100 4145  ...o".........AE
>00009800: 0308 0050 4b05 0600 0000 0001 0001 0065  ...PK..........e
>00009810: 0000 00c4 0000 0000
> ```


We immediately saw tgat each 4-byte were reversed, and wrote a small python
script to recover the file:

> ```python
>file = open('BlindDate.jpeg', 'rb')
>data = file.read()
>file.close()
>output = open('BlindDate_sol.jpeg', 'wb')
>outdata = ''
>for c in xrange(3, len(data), 4):
>	outdata += data[c]
>	outdata += data[c-1]
>	outdata += data[c-2]
>	outdata += data[c-3]
>output.write(outdata)
>output.close()
> ```

We obtained the following picture:

![BlindDate_sol.jpeg](BlindDate.jpeg)

We also noticed the long suspicious string. We guessed to is was a base64-encoded string:

> ```sh
>base64 -d 
>Li4gICAuICAuLiAgLi4gICAuICAuLiAgLi4gICAuICAuLiAgLiAgLi4NCi4gICAgLiAgIC4gICAgICAgLiAgICAgIC4gICAgLiAgIC4gIC4gIA0KICAgIC4uICAgICAgICAgIC4uICAgICAgLiAgIC4uICAgICAgLiAgLgPK
>..   .  ..  ..   .  ..  ..   .  ..  .  ..
>.    .   .       .      .    .   .  .  
>    ..          ..      .   ..      .  .
> ```

We then guessed that it was braille alphabet (which explains the title), and obtained: **f4c3p4lm**

It was not the flag, but we supposed it was a password to use later. We were actually not done with the new
picture. We also noticed that there was a zip archive after the base64-encoded string, because of the magic number PK.
We extracted it with binwalk and got the [file](flag.zip) and recovered the file using:

> ```sh
>zip -F 96DA.zip --out test.zip
> ```

The archive was encrypted, and then will tried the password we just found, but it was unsuccessful ... We spent a few minutes and finally found
that we had to use capital letters: **F4C3P4LM**

The file flag.txt doesn't contain the flag, Instead, we found a weird string:

> ++++++++++[>+>+++>+++++++>++++++++++<<<<-]>>>>++++++++++.+.+++++++++.<---.+++++++++++++++++.--------------.>+++.<+++++++++++++++++.<++++++++++++++++++.>>------.---------.--------.-----.++++++++++++++++++++++++++.<<.>>----.<++++++++.+++.>---------.<<+.>>++.<++.-----.+++++.<+++.>>++++++.<<-.>-----.<+.>.+++.>--------.<<---.>>++.<++.-----.+++++.<+++.>>++++++.<<-.++++++++++++.>>+++++++++.<<<++++++++++++++++++++++.

We knew the esoteric language brainfuck, and searched a way to decode it. The website https://copy.sh/brainfuck/ did the job, and gave us: **noxCTF{W0uld_y0u_bl1nd_d4t3_4_bl1nd_d4t3?}**
