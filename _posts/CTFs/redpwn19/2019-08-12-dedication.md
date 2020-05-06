---
layout: posts
title:  Red Pwn 2019 - Dedication (Forensics)
date:   2019-08-12
categories: [CTFs, redpwn19]
---

[~$ cd ..](/ctfs/redpwn19/2019/08/12/index.html)
>Dedication
>
>Written by: Tux
>
>Only for the dedicated.

I have to admit, I didn't like this challenge, even if it was worth it. Indeed, I spent A LOT of time on this one, and as the end of the CTF was approaching, I was not sure that I could finish in time ...

We were a given a zip archive with two files: a [textual file](/assets/res/CTFs/redpwn19/dedication/jjofpbwvgk.png) with `png` as extension and another [zip](/assets/res/CTFs/redpwn19/dedication/jjofpbwvgk.zip) with password-protected files. The textual file contained 600 lines, and each one was made of a series of 400 tuples, representing pixels value (that was not clearly indicated, but easy to guess)

The first part of the challenge was then to build the image based on the given pixels:

```python
from PIL import Image
img = Image.new('RGB', (600, 400))
f = open("jjofpbwvgk.png")
lines = f.read().splitlines()
f.close()
for i in xrange(600):
    pixels = lines[i][:-1].split(' ')
    for j in xrange(400):
        rgb = pixels[j][1:-1].split(',')
        color = (int(rgb[0]), int(rgb[1]), int(rgb[2]))
        img.putpixel((i,j),color)
img.save("jjofpbwvgk.bmp")
```

The output was as follows:

![jjofpbwvgk.bmp](/assets/res/CTFs/redpwn19/dedication/jjofpbwvgk.bmp)

It was the password to use on the password-protected archive, and it was then always the same story: a textual file describing a picture and another password-protected archive ...

Doing it by hand was surely not practicable as there were 999 embedded archives (we didn't know it at the beginning). The "easy" way to do it was to use an OCR library to automatically find the passwords, but unfortunately, guessed passwords were often wrong, leading to crashes.

The script was as follows:

```python
from PIL import Image
import pytesseract
from zipfile import ZipFile
import os
import shutil

path = '.'

next = "jvmeokeipf.png" #name of the next archive. Change it after a crash
while True:
    img = Image.new('RGB', (600, 400))
    f = open(next)
    lines = f.read().splitlines()
    f.close()
    for i in xrange(600):
        pixels = lines[i][:-1].split(' ')
        for j in xrange(400):
                rgb = pixels[j][1:-1].split(',')
                color = (int(rgb[0]), int(rgb[1]), int(rgb[2]))
                img.putpixel((i,j),color)
    new_img = next.replace(".png", ".bmp")
    img.save(new_img)
    text = pytesseract.image_to_string(img)

    zip_name = next.replace(".png", ".zip")
    #some quick fixes on passwords
    if "vv" in text or "VV" in text:
        text = text.replace("vv", "w")
    if "H" in text:
        text = text.replace("H", "ll")
    text = text.replace(" ", "")
    text = text.lower()
    print text
    with ZipFile(zip_name) as zf:
        files = zf.filelist
        for f in files:
            if f.filename[-1] != "/":
                if "|" in text:
                    try:
                        text = text.replace("|", "l")
                        source = zf.open(f, pwd=text)
                    except:
                        text = text.replace("|", "i")
                        source = zf.open(f, pwd=text)
                source = zf.open(f, pwd=text)
                #extract file in current directory
                fname = f.filename[f.filename.find("/")+1:]
                target = open(fname, "wb")
                with source, target:
                    shutil.copyfileobj(source, target)
    next = fname.replace(".zip", ".png")
    print next
```

After more than 4 hours, a file named `flag.png` FINALLY spawned:

![flag.png](/assets/res/CTFs/redpwn19/dedication/flag.png)

EOF
