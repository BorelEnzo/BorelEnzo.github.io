---
layout: posts
title:  CSCBE19 - Alien Object (Misc)
date:   2019-03-01
categories: [CTFs, cscbe19]
---

[~$ cd ..](/ctfs/cscbe19/2019/03/01/index.html)

Author: [alect096](https://alect096.github.io/)

Here is the image that the creator of the challenge gave us.

![alt text](/assets/res/CTFs/cscbe19/alien_object/cosmos.jpg)

We executed `exiftool` on the image in the following way:

> ```sh
> exiftool cosmos.jpg
>
>   ExifTool Version Number         : 11.30
>   File Name                       : cosmos.jpg
>   Directory                       : .
>   File Size                       : 8.5 MB
>   File Modification Date/Time     : 2019:03:15 14:40:52+01:00
>   File Access Date/Time           : 2019:03:15 14:48:06+01:00
>   File Inode Change Date/Time     : 2019:03:15 14:45:17+01:00
>   File Permissions                : rw-r--r--
>   File Type                       : JPEG
>   File Type Extension             : jpg
>   MIME Type                       : image/jpeg
>   JFIF Version                    : 1.01
>   DCT Encode Version              : 100
>   APP14 Flags 0                   : (none)
>   APP14 Flags 1                   : (none)
>   Color Transform                 : YCbCr
>   Exif Byte Order                 : Big-endian (Motorola, MM)
>   Compression                     : LZW
>   Photometric Interpretation      : RGB
>   Camera Model Name               : UEsDBBQAAAAIALiOMk4NBnn9agAAAGgAAAABAAAAZnPPz09R8MpPUlSIzC9VSMsvzUtRKMlIVUjLSUxXVPBILUpVyCxRyCy24uXi5XIODqoO8TBWdvQxNA6t80/yMvYP0Q03CY4vLk1Odi22SAvNMYyMTzPIK3XRNczTDTFJ1XM2KPb1D1asBQBQSwECHwAUAAAACAC4jjJODQZ5/WoAAABoAAAAAQAkAAAAAAAAACAAAAAAAAAAZgoAIAAAAAAAAQAYAG4+r2JOr9QBHHgxf02v1AEceDF/Ta/UAVBLBQYAAAAAAQABAFMAAACJAAAAAAA=
>   ...
> ```

Thanks to this command, we found that the camera model name was a base64 so we decoded it

> ```sh
> echo "UEsDBBQAAAAIALiOMk4NBnn9agAAAGgAAAABAAAAZnPPz09R8MpPUlSIzC9VSMsvzUtRKMlIVUjLSUxXVPBILUpVyCxRyCy24uXi5XIODqoO8TBWdvQxNA6t80/yMvYP0Q03CY4vLk1Odi22SAvNMYyMTzPIK3XRNczTDTFJ1XM2KPb1D1asBQBQSwECHwAUAAAACAC4jjJODQZ5/WoAAABoAAAAAQAkAAAAAAAAACAAAAAAAAAAZgoAIAAAAAAAAQAYAG4+r2JOr9QBHHgxf02v1AEceDF/Ta/UAVBLBQYAAAAAAQABAFMAAACJAAAAAAA=" | base64 -D > out.zip
> ```


We now have a [zip file](/assets/res/CTFs/cscbe19/alien_object/out.zip)

> ```sh
> unzip out.zip
>
>   Archive:  out.zip
>   inflating: f    
> ```

We now have a [file f](/assets/res/CTFs/cscbe19/alien_object/f)

> ```sh
> cat f
>
>     Good Job! You found the flag! Here it is:
>     CSR{TH3#AL13U~ObJ3OT-W4S_succEs8fUl1Y_f0nuD-1n-T4e.C0sMOS!}
> ```
