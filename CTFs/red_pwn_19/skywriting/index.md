# Skywriting - Forensics

### [~$ cd ..](../)

>Skywriting
>
>Written by: blevy
>
>This again.
>
>downloads (the same file, from different sources):
>
>    [google](https://drive.google.com/file/d/1wso-K4b_wOXzxfeppPy41Jhc_3WWRTwk/view)  
>    [cloudflare](https://redpwnctf-b2.gnk.io/file/redpwnctf/skywriting.tar.gz)

_/!\ Links in the description might be broken at the time you read_

The archive contained 20 wav files named `gusty-garden-galaxy-X.wav` where X was the index from 0 to 19, and a README:

```
Look at these 20 wav files, because 20 is my favorite number. I can count to it using my fingers and toes.

You might only need one of them, but don't worry. That's completely intuitive.

The stored data might be around 14700 bytes or so, but that's intuitive too.
```

First thing I did was to find relevant wav:

```
$ md5sum *.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-0.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-10.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-11.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-12.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-13.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-14.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-15.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-16.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-17.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-18.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-19.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-1.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-2.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-3.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-4.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-5.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-6.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-7.wav
26f929e33d1afd24d13876137e49fde6  gusty-garden-galaxy-8.wav
556fbad9e4442523a407c10d58b0aafe  gusty-garden-galaxy-9.wav
```

The [8th file](gusty-garden-galaxy-8.wav) was then the one we had to analyze, as its hash is different.

Audacity was not helpful here, and as it was a wav, I  thought about LSB extraction.

```bash
$ git clone https://github.com/ragibson/Steganography.git
$ cd Steganography
$ python3 setup.py install
$ stegolsb wavsteg -r -i ~/Documents/gusty-garden-galaxy-8.wav -o out.txt -b 14700
```

The last command reveals (switch `-r`) 14'700 (switch `-b`, because of the hint given in the README) least significant bits of the given file (switch `-i`), and writes it in `out.txt` (switch `-o`)

The [output](out.txt) was as follows:

```
xxd out.txt |head
00000000: 4f4d 4720 574f 5720 4e4f 2057 4159 0a49  OMG WOW NO WAY.I
00000010: 5427 5320 5554 4638 2047 4152 4241 4745  T'S UTF8 GARBAGE
00000020: 2041 4d4f 4e47 5354 2054 4845 2052 4547   AMONGST THE REG
00000030: 554c 4152 2047 4152 4241 4745 210a 4954  ULAR GARBAGE!.IT
00000040: 204d 5553 5420 4245 2041 2053 5542 5354   MUST BE A SUBST
00000050: 4954 5554 494f 4e20 4349 5048 4552 2121  ITUTION CIPHER!!
00000060: 2121 210a d8ae ceb2 20ce b8d9 8a20 d8b8  !!!..... .... ..
00000070: ceb2 e285 97d9 8ace b7d8 ae2c 20e2 8593  ..........., ...
00000080: 20e5 9089 ceb9 ceb7 20ce b7ce bbcf 87e4   ....... .......
00000090: bc8a cf87 e285 93ce b7d9 8ad8 b420 ceb9  ............. ..
```

We were not done, as the most difficult part started here...
It was actually not a classical substitution cipher. I started by isolating the ciphertext

```python
f = open("out.txt", "rb")
data = f.read()
f.close()
data = data[6 * 16 + 3:] #based on xxd output
letters = set(data)
```

and replacing each non-printable character by printable ones (the original cipher text doesn't contain any letter). The current character set was :
`set(['\x80', '\x85', '\x87', '\x89', '\x88', '\x8a', '\x90', '\x93', '\xc2', '\x94', '\x97', '\x99', '\x98', '.', '!', ' ', '"', "'", '4', ')', '(', '-', ',', '\xae', '\xb1', '0', '\xb3', '\xb2', '5', '\xb4', '\xb7', '6', '\xb9', '\xb8', '\xbb', ':', '\xbd', '\xbc', '\xbe', '\n', '7', '\xcf', '\xce', '9', '8', '\xd9', '\xd8', ';', '^', '2', '\xe2', '\xe5', '\xe4', '\xe8', '1'])` then I chose letters and a few special characters:

```python
data = data.replace("\x80", "A")
data = data.replace("\x85", "B")
data = data.replace("\x87", "C")
data = data.replace("\x89", "D")
data = data.replace("\x88", "E")
data = data.replace("\x8a", "F")
data = data.replace("\x90", "G")
data = data.replace("\x93", "H")
data = data.replace("\xc2", "I")
data = data.replace("\x94", "J")
data = data.replace("\x97", "K")
data = data.replace("\x99", "L")
data = data.replace("\x98", "M")
data = data.replace("\xae", "N")
data = data.replace("\xb1", "O")
data = data.replace("\xb3", "P")
data = data.replace("\xb2", "Q")
data = data.replace("\xb4", "R")
data = data.replace("\xb7", "S")
data = data.replace("\xb9", "T")
data = data.replace("\xb8", "U")
data = data.replace("\xbb", "V")
data = data.replace("\xbd", "W")
data = data.replace("\xbc", "X")
data = data.replace("\xbe", "Y")
data = data.replace("\xcf", "Z")
data = data.replace("\xce", "?")
data = data.replace("\xd9", "%")
data = data.replace("\xd8", "+")
data = data.replace("\xe2", "&")
data = data.replace("\xe5", "*")
data = data.replace("\xe4", "$")
data = data.replace("\xe8", "@")
```

The [resulting text](out_1.txt) was as follows:

>+N?Q ?U%F +U?Q&BK%F?S+N, &BH *GD?T?S ?S?VZC$XFZC&BH?S%F+R ?T+N +N+U%F +R&BH?X?X&BH?P?V@DY+N@DY +N%F?T+O?S %FIW$XF%FZC&BH%F&BK?P%F+R &BH&BK ?S?Q@DY+T&BH&BK&BM +N+U&BH?S $XFZC?Q?U@DY%F+O. +U?Q*GD%F+T%FZC, &BH +U?Q$XF%F ?T&BK@DY +N%F?T+O?S +N+U?T+N *GD%FZC%F ?S+N@DY+O&BH%F+R ?U@DY &BH+N +R?Q&BK&AL+N +N+U&BH&BK*XA &BH+N *GD?T?S &BK%F?P%F?S?S?TZC&BH@DY@DY ?V&BK?X?T&BHZC, ZA?V?S+N +R&BH?X?X&BH?P?V@DY+N. &BH +N+U&BH&BK*XA %F?T?P+U ?S+N%F$XF &BH&BK +N+U&BH?S ?S?Q@DY?V+N&BH?Q&BK ?P?Q?V@DY+R ?U%F ?P?T+N?T@DY@DY@DY%F+R ?U@DY @DY?Q&BM&BH?P?T@DY ?P@DY?V%F?S, ?T&BK+R &BK?Q+N+U&BH&BK&BM ZC%F&BJ?V&BHZC%F+R $XF?VZC%F +NZC&BH?T@DY-?T&BK+R-%FZCZC?QZC. &BH +N+U&BH&BK*XA +N+U%F +R&BH?X?X&BH?P?V@DY+N@DY +R%F@DY+N?T ?XZC?Q+O +N+U%F ZC%F?S+N ?Q?X $XF?T?P+N?X +O&BH&BM+U+N +U?T+T%F %FIW?T?P%FZC?U?T+N%F+R +N+U%F +R&BH?X?X&BH?P?V@DY+N&BH%F?S +N%F?T+O?S ?X?T?P%F+R: $XF%FZC+U?T$XF?S $XF%F?Q$XF@DY%F *GD%FZC%F %FIW$XF%F?P+N&BH&BK&BM ?T $XFZC?Q?U@DY%F+O ?P@DY?Q?S%FZC +N?Q +N+U%F ZC?Q?V&BK+R 1 +U?TZC+R $XFZC?Q?U@DY%F+O?S. ?T+R+R&BH+N&BH?Q&BK?T@DY@DY@DY, &BH+N&AL?S ?T ?X?T&BHZC@DY@DY &BK?Q&BK-?S+N?T&BK+R?TZC+R ?P+N?X $XFZC?Q?U@DY%F+O. &BH&AL+R ?TZC&BM?V%F +N+U?T+N *GD?QZC*XA?S +N?Q +N+U%F $XFZC?Q?U@DY%F+O&AL?S ?T+R+T?T&BK+N?T&BM%F, ?T&BK+R ?XZC?Q+O *GD+U?T+N &BH&AL+T%F ?S%F%F&BK $XF%F?Q$XF@DY%F *GD+U?Q +U?T+T%F&BK&AL+N +R?Q&BK%F +N?Q?Q +O?T&BK@DY ?P+N?X?S ?S%F%F+O%F+R +N?Q @DY&BH*XA%F &BH+N +O?QZC%F, ?U?V+N +N+U?T+N&AL?S ?T&BK%F?P+R?Q+N?T@DY. &BH?X @DY?Q?V +R&BH?S?T&BMZC%F%F, ?QZC *GD?T&BK+N +N?Q ?S+U?TZC%F ?T&BK@DY+N+U&BH&BK&BM %F@DY?S%F, ?X%F%F@DY ?XZC%F%F +N?Q ?P?Q&BK+N?T?P+N +O%F! +U?Q$XF%F @DY?Q?V %F&BKZA?Q@DY%F+R +N+U%F ?P+N?X ?T?S ?T *GD+U?Q@DY%F! ?T@DY?S?Q, &BH?X @DY?Q?V @DY&BH*XA%F $XFZC?Q?U@DY%F+O?S @DY&BH*XA%F +N+U&BH?S, +O?T*XA%F ?S?VZC%F +N?Q ?P+U%F?P*XA ?Q?V+N $XF?T?P+N?X 2019 &BH&BK +O?T&BK@DY +O?Q&BK+N+U?S&AL +N&BH+O%F.
>    -- &BK&BH?P+U?Q@DY?T?S+O
>
>*XA%F%F$XF &BM?Q&BH&BK&BM.
>
>+R?Q&BK'+N ?U%F %F+T&BH@DY.
>
>+N+U&BH?S ?P+U?T@DY@DY%F&BK&BM%F &BH?S &BH&BK+N?V&BH+N&BH+T%F.
>
>+U%FZC%F &BH?S ?T +N&BH&BK@DY ?S+U?QZC+N%F&BK%F+R ?P@DY?V%F +N?Q +N+U%F +R?Q?P?V+O%F&BK+N @DY?Q?V *GD&BH@DY@DY &BK%F%F+R. *XA%F%F$XF @DY?Q?VZC +U%F?T+R &BH&BK +N+U%F ?P@DY?Q?V+R.
>
>+N&BH&BK@DY?X@DY?T&BM@DY&BH&BK*XA
>
>--------------------------------
>
> **_truncated for the sake of brevity_**

Using a basic online tool that deals only with alphabet, where each letter is replaced by another one was not helpful, because here, each letter is replaced by two or three letters. It took me around a half hour to understand it, and what led me to this assumption is the fact that the string `?S?V?U?S+N&BH+N?V+N&BH?Q&BK` was repeated 16 times in the text (in the second part of the text, not shown here). Indeed, the question mark appeared quite often and it was then unlikely that it was replaced by only one letter. Is then assumed that one letter was replaced by a pair or a triplet, giving me: `?S ?V ?U ?S +N &BH +N ?V +N &BH ?Q &BK`. I split in this way because it made appear some interesting pair or triplets. Thanks to the encryption method, I understood that this string meant `substitution`, and then, breaking the cipher became quite easy:

>?S -> s  
>?V -> u  
>?U -> b  
>+N -> t  
>&BH -> i
>?Q -> o  
>&BK -> n

The beginning of the message was then:

```
to b%F +Uon%Fst, i *GD?Ts suZC$XFZCis%F+R ?Tt t+U%F +Ri?X?Xi?Pu@DYt@DY t%F?T+Os %FIW$XF%FZCi%Fn?P%F+R in so@DY+Tin&BM t+Uis $XFZCob@DY%F+O. +Uo*GD%F+T%FZC, i +Uo$XF%F ?Tn@DY t%F?T+Os t+U?Tt *GD%FZC%F st@DY+Oi%F+R b@DY it +Ron&ALt t+Uin*XA it *GD?Ts n%F?P%Fss?TZCi@DY@DY un?X?TiZC, ZAust +Ri?X?Xi?Pu@DYt. i t+Uin*XA %F?T?P+U st%F$XF in t+Uis so@DYution ?Pou@DY+R b%F ?P?Tt?T@DY@DY@DY%F+R b@DY @DYo&BMi?P?T@DY ?P@DYu%Fs, ?Tn+R not+Uin&BM ZC%F&BJuiZC%F+R $XFuZC%F tZCi?T@DY-?Tn+R-%FZCZCoZC. i t+Uin*XA t+U%F +Ri?X?Xi?Pu@DYt@DY +R%F@DYt?T ?XZCo+O t+U%F ZC%Fst o?X $XF?T?Pt?X +Oi&BM+Ut +U?T+T%F %FIW?T?P%FZCb?Tt%F+R t+U%F +Ri?X?Xi?Pu@DYti%Fs t%F?T+Os ?X?T?P%F+R: $XF%FZC+U?T$XFs $XF%Fo$XF@DY%F *GD%FZC%F %FIW$XF%F?Ptin&BM ?T $XFZCob@DY%F+O ?P@DYos%FZC to t+U%F ZCoun+R 1 +U?TZC+R $XFZCob@DY%F+Os. ?T+R+Rition?T@DY@DY@DY, it&ALs ?T ?X?TiZC@DY@DY non-st?Tn+R?TZC+R ?Pt?X $XFZCob@DY%F+O. i&AL+R ?TZC&BMu%F t+U?Tt *GDoZC*XAs to t+U%F $XFZCob@DY%F+O&ALs ?T+R+T?Tnt?T&BM%F, ?Tn+R ?XZCo+O *GD+U?Tt i&AL+T%F s%F%Fn $XF%Fo$XF@DY%F *GD+Uo +U?T+T%Fn&ALt +Ron%F too +O?Tn@DY ?Pt?Xs s%F%F+O%F+R to @DYi*XA%F it +OoZC%F, but t+U?Tt&ALs ?Tn%F?P+Rot?T@DY. i?X @DYou +Ris?T&BMZC%F%F, oZC *GD?Tnt to s+U?TZC%F ?Tn@DYt+Uin&BM %F@DYs%F, ?X%F%F@DY ?XZC%F%F to ?Pont?T?Pt +O%F! +Uo$XF%F @DYou %FnZAo@DY%F+R t+U%F ?Pt?X ?Ts ?T *GD+Uo@DY%F! ?T@DYso, i?X @DYou @DYi*XA%F $XFZCob@DY%F+Os @DYi*XA%F t+Uis, +O?T*XA%F suZC%F to ?P+U%F?P*XA out $XF?T?Pt?X 2019 in +O?Tn@DY +Oont+Us&AL ti+O%F.
    -- ni?P+Uo@DY?Ts+O

*XA%F%F$XF &BMoin&BM.

+Ron't b%F %F+Ti@DY.

t+Uis ?P+U?T@DY@DY%Fn&BM%F is intuiti+T%F.

+U%FZC%F is ?T tin@DY s+UoZCt%Fn%F+R ?P@DYu%F to t+U%F +Ro?Pu+O%Fnt @DYou *GDi@DY@DY n%F%F+R. *XA%F%F$XF @DYouZC +U%F?T+R in t+U%F ?P@DYou+R.

tin@DY?X@DY?T&BM@DYin*XA

--------------------------------
```

Some words can be easily recognized here, and decrypting the whole ciphertext took only a couple of minutes. I don't know if it was intended or if I made a mistake (I'm pretty sure I did), but "L" and "Y" were encoded with the same triplet **@DY** (I tried twice, and always faced the same issue). Anyway, the plaintext I got was as follows:

```
to be honest, i was surprised at the difficu@DYt@DY teams experienced in so@DYving this prob@DYem. however, i hope an@DY teams that were st@DYmied b@DY it don't think it was necessari@DY@DY unfair, just difficu@DYt. i think each step in this so@DYution cou@DYd be cata@DY@DY@DYed b@DY @DYogica@DY c@DYues, and nothing reqJuired pure tria@DY-and-error. i think the difficu@DYt@DY de@DYta from the rest of pactf might have exacerbated the difficu@DYties teams faced: perhaps peop@DYe were expecting a prob@DYem c@DYoser to the round 1 hard prob@DYems. additiona@DY@DY@DY, it's a fair@DY@DY non-standard ctf prob@DYem. i'd argue that works to the prob@DYem's advantage, and from what i've seen peop@DYe who haven't done too man@DY ctfs seemed to @DYike it more, but that's anecdota@DY. if @DYou disagree, or want to share an@DYthing e@DYse, fee@DY free to contact me! hope @DYou enjo@DYed the ctf as a who@DYe! a@DYso, if @DYou @DYike prob@DYems @DYike this, make sure to check out pactf 2019 in man@DY months' time.
    -- nicho@DYasm

keep going.

don't be evi@DY.

this cha@DY@DYenge is intuitive.

here is a tin@DY shortened c@DYue to the document @DYou wi@DY@DY need. keep @DYour head in the c@DYoud.

tin@DYf@DYag@DYink
```

The last line has to be understood as **tinyflaglink**. Browsing to **tinyurl.com/tinyflaglink** led to **flag{th1s_fl4g_1s_1ntu1t1v3}**

EOF
