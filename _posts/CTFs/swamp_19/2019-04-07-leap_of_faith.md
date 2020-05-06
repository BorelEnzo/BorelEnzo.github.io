---
layout: posts
title:  Swamp CTF 2019 - Leap of Faith (Forensics)
date:   2019-04-07
categories: [CTFs, swamp19]
---

[~$ cd ..](/ctfs/swamp19/2019/04/07/index.html)

>“You have to let it all go, Neo. Fear, doubt, and disbelief. Free your mind (and your stego tools).” - Morpheus, probably
>
>-= Challenge by P4PA_0V3RL0RD =-

Another forensics Challenge, also related to the Matrix trilogy. We were given here the following picture:

![leap_of_faith.jpeg](/assets/res/CTFs/swamp19/leap_of_faith/leap_of_faith.jpeg)

Running `exiftool` told us that we could extract the thumbnail:

```
ExifTool Version Number         : 10.40
File Name                       : leap_of_faith.jpeg
Directory                       : .
File Size                       : 41 kB
File Modification Date/Time     : 2019:04:06 01:47:48-04:00
File Access Date/Time           : 2019:04:08 00:54:22-04:00
File Inode Change Date/Time     : 2019:04:08 00:54:10-04:00
File Permissions                : rw-r--r--
File Type                       : JPEG
File Type Extension             : jpg
MIME Type                       : image/jpeg
Exif Byte Order                 : Little-endian (Intel, II)
X Resolution                    : 72
Y Resolution                    : 72
Resolution Unit                 : inches
Modify Date                     : 2019:04:04 09:22:27
Exif Version                    : 0210
Date/Time Original              : 2019:04:04 09:22:27
Flashpix Version                : 0100
Color Space                     : Uncalibrated
Thumbnail Offset                : 226
Thumbnail Length                : 25217
Image Width                     : 720
Image Height                    : 480
Encoding Process                : Baseline DCT, Huffman coding
Bits Per Sample                 : 8
Color Components                : 3
Y Cb Cr Sub Sampling            : YCbCr4:2:0 (2 2)
Image Size                      : 720x480
Megapixels                      : 0.346
Thumbnail Image                 : (Binary data 25217 bytes, use -b option to extract)
```

We then extracted it by running the following command (commonly used CTF command!):

```bash
~$ exiftool -b -ThumbnailImage leap_of_faith.jpeg > thumb.jpeg
```

and got:

![Morpheus](/assets/res/CTFs/swamp19/leap_of_faith/morpheus.jpeg)

We retried the same command on the new picture and saw that this one contains also an embedded thumbnail:

```
ExifTool Version Number         : 10.40
File Name                       : morpheus.jpeg
Directory                       : .
File Size                       : 25 kB
File Modification Date/Time     : 2019:04:06 01:48:26-04:00
File Access Date/Time           : 2019:04:08 00:58:54-04:00
File Inode Change Date/Time     : 2019:04:08 00:58:38-04:00
File Permissions                : rw-r--r--
File Type                       : JPEG
File Type Extension             : jpg
MIME Type                       : image/jpeg
Exif Byte Order                 : Little-endian (Intel, II)
X Resolution                    : 72
Y Resolution                    : 72
Resolution Unit                 : inches
Modify Date                     : 2019:04:04 09:23:36
Exif Version                    : 0210
Date/Time Original              : 2019:04:04 09:23:36
Flashpix Version                : 0100
Color Space                     : Uncalibrated
Thumbnail Offset                : 226
Thumbnail Length                : 5199
Image Width                     : 334
Image Height                    : 302
Encoding Process                : Baseline DCT, Huffman coding
Bits Per Sample                 : 8
Color Components                : 3
Y Cb Cr Sub Sampling            : YCbCr4:2:0 (2 2)
Image Size                      : 334x302
Megapixels                      : 0.101
Thumbnail Image                 : (Binary data 5199 bytes, use -b option to extract)
```

Let's extract it:

```bash
~$ mv thumb.jpeg morpheus.jpeg
~$ exiftool -b -ThumbnailImage morpheus.jpeg > thumb.jpeg
```

and get our reward:

![thumb](/assets/res/CTFs/swamp19/leap_of_faith/thumb.jpeg)

FLAG: **flag{FR33_Y0UR_M1ND}**

EOF
