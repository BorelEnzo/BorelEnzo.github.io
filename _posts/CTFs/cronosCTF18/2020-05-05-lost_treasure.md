---
layout: posts
title: Lost Treasure (50 pts)
date:   2020-05-05
categories: [CTFs, cronosCTF18]
---

[~$ cd ..](../)

>Ahoy! We 'ave lost a booty 'n know that thar be a map available t' find th' booty.
>Can ye help us find th' booty at <51.68.252.196:8081>.

We are landed on a flat website:

![site](/assets/res/CTFs/cronos_18/lost_treasure/site.png)

As usual, I checked if there was a robots.txt and found:

>User-agent: BlackPearl  
>Allow: /Isla de Muerta  
>User-agent: Pirate  
>Allow: /TreasureMaps  
>User-agent: *  
>Disallow: /Isla de Muerta  
>Disallow: /TreasureMaps  

I then sent a request with expected User-agent:

```bash
% curl -H "User-agent: Pirate" "http://51.68.252.196:8081/TreasureMaps/"  > TreasureMaps.html
```

and got the [following file](/assets/res/CTFs/cronos_18/lost_treasure/TreasureMaps.html):

```html
<html>
<head><title>Index of /TreasureMaps/</title></head>
<body bgcolor="white">
<h1>Index of /TreasureMaps/</h1><hr><pre><a href="../">../</a>
<a href="README.txt">README.txt</a>                                         19-Sep-2018 13:44                  98
<a href="map1.zip">map1.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map10.zip">map10.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map11.zip">map11.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map12.zip">map12.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map13.zip">map13.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map14.zip">map14.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map15.zip">map15.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map16.zip">map16.zip</a>                                          19-Sep-2018 13:44                1521
<a href="map2.zip">map2.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map3.zip">map3.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map4.zip">map4.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map5.zip">map5.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map6.zip">map6.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map7.zip">map7.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map8.zip">map8.zip</a>                                           19-Sep-2018 13:44                1521
<a href="map9.zip">map9.zip</a>                                           19-Sep-2018 13:44                1521
</pre><hr></body>
</html>
```

Let's begin with the README:

```bash
% curl -H "User-agent: Pirate" "http://51.68.252.196:8081/TreasureMaps/README.txt"             
  All our zip files are secured with passwords. Only someone called "RockYou" knows the passwords!
```

and the archives:

```bash
for i in {1..16}; do curl -H "User-agent: Pirate" "http://51.68.252.196:8081/TreasureMaps/map$i.zip" -o "map$i.zip" ;done
```

I then naively started with a dictionary attack with rockyou.txt using a for loop on each archive, but realized that it could no work:

```bash
% for i in {1..16}; do fcrackzip -v -u -D -p rockyou.txt map$i.zip; done
```

because `fcrakzip` couldn't handle properly the kind of file:

```bash
% file map1.zip
  map12.zip: Zip archive data, at least v5.1 to extract
% unzip map1.zip
  Archive:  map1.zip
    skipping: f864ae468a              need PK compat. v5.1 (can do v4.6)
```

I looked for a tool is order to crack password quickly, but didn't found. I then realized that I didn't have to crack them all:

```bash
% md5sum *                    
b32697dc1e9e8da35b9732a8e2859657  map10.zip
b32697dc1e9e8da35b9732a8e2859657  map11.zip
b32697dc1e9e8da35b9732a8e2859657  map12.zip
fe2f3b7c371400f331aa9e975bfc825a  map13.zip
b32697dc1e9e8da35b9732a8e2859657  map14.zip
b32697dc1e9e8da35b9732a8e2859657  map15.zip
b32697dc1e9e8da35b9732a8e2859657  map16.zip
b32697dc1e9e8da35b9732a8e2859657  map1.zip
b32697dc1e9e8da35b9732a8e2859657  map2.zip
b32697dc1e9e8da35b9732a8e2859657  map3.zip
b32697dc1e9e8da35b9732a8e2859657  map4.zip
b32697dc1e9e8da35b9732a8e2859657  map5.zip
b32697dc1e9e8da35b9732a8e2859657  map6.zip
b32697dc1e9e8da35b9732a8e2859657  map7.zip
b32697dc1e9e8da35b9732a8e2859657  map8.zip
b32697dc1e9e8da35b9732a8e2859657  map9.zip
b828b8825c21ac97afce52afbaac5ca6  README.txt
432e5e794481c58c572905d67c27d189  TreasureMaps.html
```

I then kept only [map12.zip](/assets/res/CTFs/cronos_18/lost_treasure/map12.zip) and [map13.zip](/assets/res/CTFs/cronos_18/lost_treasure/map13.zip) and wrote a (non-optimized) Python script to break the passwords:

```python
import subprocess
f = open('pirate.txt', 'r')
passwords = f.read().splitlines()
f.close()
for word in passwords:
	cmd = '7z e -y map12.zip -p%s' % word
	process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	output, error = process.communicate()
	if "ERROR" not in error:
		print word
		process = subprocess.Popen('wc f864ae468a'.split(), stdout=subprocess.PIPE,stderr=subprocess.PIPE)
		output, error = process.communicate()
		print output
		print '-------------'
```

I made the assumption that they were related to the theme of the piracy.
I then `grep`'d rockyou in this way and run the script:

```bash
% grep rockyou.txt -ie pirate > pirate.txt
% run brute.py
 ohs pirate
 0 0 0 f864ae468a

 -------------
 pirates arr cool
 0 0 0 f864ae468a

 -------------
 pirate girl
 0 0 0 f864ae468a

 -------------
 my pirate
 0 0 0 f864ae468a

 -------------
 iwannabeapirate
 90  209 3677 f864ae468a

 -------------
 i luv pirate forever
 0 0 0 f864ae468a
% 7z e -y map13.zip -piwannabeapirate

 7-Zip [64] 16.02 : Copyright (c) 1999-2016 Igor Pavlov : 2016-05-21
 p7zip Version 16.02 (locale=fr_FR.utf8,Utf16=on,HugeFiles=on,64 bits,4 CPUs Intel(R) Core(TM) i5-4200U CPU @ 1.60GHz (40651),ASM,AES-NI)

 Scanning the drive for archives:
 1 file, 1521 bytes (2 KiB)

 Extracting archive: map13.zip
 --
 Path = map13.zip
 Type = zip
 Physical Size = 1521

 Everything is Ok

 Size:       3677
Compressed: 1521
```

Password found! I then used **"iwannabeapirate"** and extract the file [/assets/res/CTFs/cronos_18/lost_treasure/f864ae468a](/assets/res/CTFs/cronos_18/lost_treasure/f864ae468a):

```php
<?php
if (isset($_POST['submit'])){
	if($_POST["password"] == "todayisagoodday") {
		header("Location: "); //TODO: add treasure location
		die();
	}
	else {
		header("Location: brrrt.html");
		die();
	}
}
?>
...
```

I then sent the following request to get the location of the treasure:

```bash
% curl -d "submit=&password=todayisagoodday" -H "User-agent: BlackPearl" "http://51.68.252.196:8081/Isla de Muerta/index.php"  -v
 *   Trying 51.68.252.196...
 * TCP_NODELAY set
 * Connected to 51.68.252.196 (51.68.252.196) port 8081 (#0)
 > POST /Isla de Muerta/index.php HTTP/1.1
 > Host: 51.68.252.196:8081
 > Accept: */*
 > User-agent: BlackPearl
 > Content-Length: 32
 > Content-Type: application/x-www-form-urlencoded
 >
 * upload completely sent off: 32 out of 32 bytes
 < HTTP/1.1 302 Moved Temporarily
 < Server: nginx/1.12.2
 < Date: Thu, 04 Oct 2018 16:32:14 GMT
 < Content-Type: text/html
 < Transfer-Encoding: chunked
 < Connection: keep-alive
 < X-Powered-By: PHP/5.4.16
 < Location: Treasure-fipeaifaeaFAFAEfa2eaf78af27zaaerdfae87.html
 <
 * Curl_http_done: called premature == 0
 * Connection #0 to host 51.68.252.196 left intact
```

And capture the flag

```bash
 % curl -H "User-agent: BlackPearl" "http://51.68.252.196:8081/Isla de Muerta/Treasure-fipeaifaeaFAFAEfa2eaf78af27zaaerdfae87.html"  -v
 *   Trying 51.68.252.196...
 * TCP_NODELAY set
 * Connected to 51.68.252.196 (51.68.252.196) port 8081 (#0)
 > GET /Isla de Muerta/Treasure-fipeaifaeaFAFAEfa2eaf78af27zaaerdfae87.html HTTP/1.1
 > Host: 51.68.252.196:8081
 > Accept: */*
 > User-agent: BlackPearl
 >
 < HTTP/1.1 200 OK
 < Server: nginx/1.12.2
 < Date: Thu, 04 Oct 2018 16:33:14 GMT
 < Content-Type: text/html
 < Content-Length: 317
 < Last-Modified: Wed, 19 Sep 2018 13:44:20 GMT
 < Connection: keep-alive
 < ETag: "5ba252b4-13d"
 < Accept-Ranges: bytes
 <
 <head>
  <title>Treasure</title>
  <style>
    html, body, .container {
        height: 100%;
    }
    .container {
        color: white;
        display: none;
    }
  </style>
</head>
<body>
  <h1>Treasure</h1>
  <div class="container">
    <p>AllTreasuremapsLeadToATreasure</p>
  </div>
</body>
* Curl_http_done: called premature == 0
* Connection #0 to host 51.68.252.196 left intact
```

FLAG: **AllTreasuremapsLeadToATreasure**
