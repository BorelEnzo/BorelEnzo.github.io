---
layout: posts
title: Stirring Chowder (35 pts)
date:   2018-10-01
categories: [CTFs, cronosCTF18]
---

[~$ cd ..](/ctfs/cronosctf18/2018/10/01/index.html)

>We be having trouble finding out how to stir the chowder to make it ready,
>ask our pet animal A'tuin if he knows more!


We are an given [an archive](/assets/res/CTFs/cronos_18/stirring_chowder/The Great A'tuin), recognized as a lrzip-compressed file

> ```sh
> % file The\ Great\ A\'tuin
>The Great A'tuin: LRZIP compressed data - version 0.6
> ```

I then wrote a small bash script to break the password:

> ```sh
>#!/bin/bash
>
>arch=$1;
>dict=$2;
>for passphrase in $(cat $dict); do
>	response=$(echo "$passphrase"|lrzip -d $arch -o uncompress 2>/dev/null; rm uncompress)
>	if [[ ! $response =~ "error "* ]]; then
>		printf "Password: '%s'\n" "$passphrase";
>		exit
>	fi
>	echo "$response"
>done
> ```

After a few minutes, the script returns :

> ```sh
> ./brute.sh arch rockyou.txt
>...
>Output filename is: uncompress
>Enter passphrase:
>Decompressing...
>Fatal error - exiting
>Password: 'turtle
> ```

__NB__: make sure to rename the archive, blank spaces are not well-handled

To keep the story short, the extracted file is also a LRZIP archive, which can uncompressed using the same password.

After the second extraction, we obtain zip archive, and have to extract 5 times, with the same password. We then obtain a filesystem to mount, with
embedded file systems. I ran the following command until getting an error:

> ```
># mount turtle /mnt/turtle && cp /mnt/turtle/turtle turtle && umount /mnt/turtle && file turtle
>cp: impossible d'évaluer '/mnt/turtle/turtle': Aucun fichier ou dossier de ce type
> ```

It means that there is no more "turtle" file in the mounted file system.

> ```
># ls /mnt/turtle
> flag.txt
># cat /mnt/turtle/flag.txt
>the flag is the exact number of unpack operations you had to do to get here ;)
> ```

Grrrrr ...

I did the job once again, and found : **12**
