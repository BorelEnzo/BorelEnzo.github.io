---
layout: posts
title: The fate of Steven Seagull (50 pts)
date:   2020-05-05
categories: [CTFs, cronosCTF18]
---

[~$ cd ..](ctfs/cronosctf18/2020/05/05/index.html)

>We noticed Steven be surfin' the web quite a lot in silence, we thinks he's been hidin' a stash of the good old booty.
>Of course we be hanging him or make him walk the plank if it isn't gender neutral!
>Find out if he's snooping on women and let us know if he needs to feed the fishes.

We are given a picture of Steven Seagal, dressed as a pirate.

![steven-seagull](/assets/res/CTFs/cronos_18/steven-seagull/steven-seagull.jpg)

I actually spent many hours on the first part of this challenge, because the trick to extract the hidden data of this picture was totally unexpected, and I found the solution by making a copy/paste mistake ...

I started by trying a dictionnary attack with `steghide` and rockyou.txt, but found nothing. I also tried stegsolve, same result. It made me think to the band [Steve'n'Seagulls](https://stevenseagulls.com/), then I tried to find a password related to them. Unsuccessful as well.
And finally, as I tried to copy and paste another wrong password, I accidentally put in the clipboard a leading carriage return, and was so surprised to see in my shell:

> ```sh
>% steghide extract -sf steven/steven-seagull.jpg
>Entrez la passphrase:
>Ecriture des données extraites dans "nostash".
> ```

The passphrase was actually empty ... The rest of the challenge was then quite straightforward.

> ```sh
>% file nostash
>nostash: Squashfs filesystem, little endian, version 4.0, 382 bytes, 2 inodes, blocksize: 131072 bytes, created: Mon Sep 10 15:27:55 2018
> ```

Let's mount it:

> ```sh
># mkdir /mnt/steven
># mount nostash /mnt/steven
># ls /mnt/steven
># cat /mnt/steven/PornStash.txt
>In case i forgot where me stash was:
>
>https://www.dropbox.com/s/3nezkun9p4vjlbg/StevenSeagullWare.mkv?dl=0
>
>Hope me mateys don't find out i'm not taking care of the ship!
> ```

Okay, let's download the video:

> ```sh
>% wget "https://www.dropbox.com/s/3nezkun9p4vjlbg/StevenSeagullWare.mkv?dl=0"
>% mv StevenSeagullWare.mkv\?dl=0 steven.mkv
>% file steven.mkv
>steven.mkv: 7-zip archive data, version 0.4
>% 7z e steven.mkv
>
>7-Zip [64] 16.02 : Copyright (c) 1999-2016 Igor Pavlov : 2016-05-21
>p7zip Version 16.02 (locale=fr_FR.utf8,Utf16=on,HugeFiles=on,64 bits,4 CPUs Intel(R) Core(TM) i5-4200U CPU @ 1.60GHz (40651),ASM,AES-NI)
>
>Scanning the drive for archives:
>1 file, 26510586 bytes (26 MiB)
>
>Extracting archive: steven.mkv
>--
>Path = steven.mkv
>Type = 7z
>Physical Size = 26510586
>Headers Size = 146
>Method = LZMA2:25
>Solid = -
>Blocks = 1
>
>Everything is Ok
>
>Size:       26535475
>Compressed: 26510586
> ```

The .mkv was actually an archive, giving me a real mp4 video. No need to analyze deeper, I only had to use my old friend `strings`:

> ```sh
>% strings StevenSeagullWare.mp4|grep flag
><flag>the early bird guts the worm</flag>
> ```
