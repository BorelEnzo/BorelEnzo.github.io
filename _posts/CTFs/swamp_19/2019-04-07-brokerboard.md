---
layout: posts
title:  Swamp CTF 2019 - Brokerboard (Web)
date:   2019-04-07
categories: [CTFs, swamp19]
---

[~$ cd ..](/ctfs/swamp19/2019/04/07/index.html)

>It's the year 1997 and the Internet is just heating up! :fire:
>
>In order to get ahead of the curve, SIT Industries® has introduced it's first Internet product: The Link Saver™. SIT Industries® has been very secretive about this product - even going so far to hire Kernel Sanders® to test the security!
>
>However, The Kernel discovered that The Link Saver had a little bit of an SSRF problem that allowed any user to fetch the code for The Link Saver™ from https://localhost/key and host it themselves :grimacing:. Fortunately, with a lil' `parse_url` magic, SIT Industries® PHP wizards have patched this finding from Kernel Sanders® and are keeping the code behind this wonderful site secure!
>
>... or have they? :wink:
>
>chal1.swampctf.com:1244
>
>-= Created by andrewjkerr =-

We faced a really simple website allowing us to submit an URL:

![form0](/assets/res/CTFs/swamp19/brokerboard/form0.png)

The description of the challenge provides a huge hint by highlighting the `parse_url`. We already heard about uXSS CVE-2018-6128 thanks to [this video](https://www.youtube.com/watch?v=0uejy9aCNbI) published on the channel LiveOverflow. By googling "parse_url vulnerability" we landed on [this webpage](https://bugs.php.net/bug.php?id=73192):

![bug](/assets/res/CTFs/swamp19/brokerboard/bug.png)

Submitting the URL `https://localhost/key` returns the message `Ruh roh, we don't allow you to fetch internal URLs!`. However, by submitting the URL `http://chal1.swampctf.com:1244/key#@localhost/key` we got our flag:

![form1](/assets/res/CTFs/swamp19/brokerboard/form1.png)

FLAG: **flag{y0u_cANn0t_TRU5t_php}**

EOF
