# Lost in the forest (Forensics)

### [~$ cd ..](../)

> You've rooted a notable hacker's system and you're sure that
> he has hidden something juicy on there. Can you find his secret?

We are given [this zip archive](fs.zip), wich contains the following files:

> ```
>$ tree
>├── bin
>│   ├── cat
>│   ├── clear
>│   ├── ls
>│   ├── man
>│   ├── mkdir
>│   ├── mv
>│   ├── rm
>│   ├── rmdir
>│   └── touch
>├── boot
>│   └── syslinux
>│       └── syslinux.cfg
>├── dev
>├── etc
>│   └── hostname
>├── home
>│   └── hkr
>│       ├── Desktop
>│       │   └── clue.png
>│       ├── Documents
>│       ├── Downloads
>│       ├── hzpxbsklqvboyou
>│       ├── Music
>│       ├── Pictures
>│       │   ├── ngsn2dhjtM1sulnzno1_500.jpg
>│       │   ├── ngw04qs2ah1r2r2hco1_500.jpg
>│       │   ├── ngw06omoVx1r2r2hco1_500.jpg
>│       │   ├── ngw14eClcm1r2r2hco1_500.jpg
> ... some pictures
>│       │   ├── nrfh6lK0LK1t0ge0qo1_500.jpg
>│       │   ├── nrgufoLBGp1uxhpyfo1_1280.jpg
>│       │   └── nrjp4inobM1uvfeuho1_500.jpg
>│       └── Videos
>├── lib
>├── media
>├── mnt
>├── opt
>├── proc
>├── root
>├── run
>├── sbin
>├── srv
>│   ├── ftp
>│   └── http
>├── sys
>├── tmp
>├── usr
>│   ├── bin
>│   ├── etc
>│   ├── include
>│   ├── lib
>│   ├── lib32
>│   ├── lib64
>│   ├── local
>│   ├── sbin
>│   ├── share
>│   └── src
>└── var
> ...
>
>51 directories, 108 files
> ```

A lot of directories were empty, all relevant files were in /home/hkr.

Here is the "clue", but we didn't understand how it could help us.

![clue.png](clue.png)

We took a look at the file named [hzpxbsklqvboyou](hzpxbsklqvboyou), which contained a base64-encoded string:

> ```sh
>base64 -d hzpxbsklqvboyou |xxd
>00000000: f0d1 d8db 9998 b611 9fb3 99dd c31d 9993  ................
>00000010: 595c 6851 9ce2 959d 5591 50a0 9993 f0d1  Y\hQ....U.P.....
>00000020: d8db 9998 b611 9fb3 99dd c31d 9993 595c  ..............Y\
>00000030: 6851 9ce2 959d 5591 50a0 9993 f0d1 d8db  hQ....U.P.......
>00000040: 9998 b611 9fb3 99dd c31d 9993 595c 6851  ............Y\hQ
>00000050: 9ce2 959d 5591 50a0 9993 f0d1 d8db 9998  ....U.P.........
>00000060: b611 9fb3 99dd c31d 9993 595c 6851 9ce2  ..........Y\hQ..
>00000070: 959d 5591 50a0 9993 f0d1 d8db 9998 b611  ..U.P...........
>00000080: 9fb3 99dd c31d 9993 595c 6851 9ce2 959d  ........Y\hQ....
>00000090: 5591 50a0 9993                           U.P...
> ```

We looked for more clued, when a team mate noticed that there was an interesting thing in the [bash_history](bash_history)

> ```
> $ ls -a
>.              .bash_logout   Desktop    hzpxbsklqvboyou  Pictures  Videos
>..             .bash_profile  Documents  Music            .profile
>.bash_history  .bashrc        Downloads  out.test         .ssh
> $ nano -l .bash_history
>	...
>	168 cd Downloads
>	169 ./tool.py ../secret > ../hzpxbsklqvboyou
> ```

However, we didn't find tool.py in Downloads/ and we had no way to decrypt hzpxbsklqvboyou ...

Another line in .bash_history gave us the solution:
> ```sh
>...
>	96 wget https://gist.githubusercontent.com/Glitch-is/bc49ee73e5413f3081e5bcf5c1537e78/raw/c1f735f7eb36a20cb46b9841916d73017b5e46a3/eRkjLlksZp
> ...
> ```

We downloaded the script:

> ```
>#!/usr/bin/python3
>import sys
>import base64
>
>def encode(filename):
>    with open(filename, "r") as f:
>        s = f.readline().strip()
>        return base64.b64encode((''.join([chr(ord(s[x])+([5,-1,3,-3,2,15,-6,3,9,1,-3,-5,3,-15] * 3)[x]) for x in range(len(s))])).encode('utf-8')).decode('utf-8')[::-1]*5
>
>if __name__ == "__main__":
>    print(encode(sys.argv[1]))
> ```

and tried to reverse it, and it was actually quite easy:

* a shit is applied with a hard-coded key
* a string is base64'ed
* \[ ::-1 \] reverses the string
* the string repeated 5 times

Hence, we did: 
> ```sh
>echo '8NHY25mYthGfs5ndwx2Zk1lcaFGc4pWdVZFQoJmT'|rev|base64 -d
>	Nbh@VUujxpaZr]dglpv~l|hmbnv`s|
> ```

to recover the base64'ed string, and applied the shift, but with a "-":

> ```python
>cipher = "Nbh@VUujxpaZr]dglpv~l|hmbnv`s|"
>''.join([chr(ord(cipher[x])-([5,-1,3,-3,2,15,-6,3,9,1,-3,-5,3,-15] * 3)[x]) for x in range(len(cipher))])
>	'IceCTF{good_ol_history_lesson}
> ```
