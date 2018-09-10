# Behind her eyes

### [~$ cd ..](../)

Actually, I do not remember the exact name and the statement of the challenge, but it was about two Roman people who sent messages. The goal was to extract a hidden picture.
The [text](data.txt) we had was obviously not written with roman characters, and then we guessed that it was about encoding, and more specifically Latin1, because of the statement.
(We tried to translate the russian text, and it was only a Lorem ipsum. Wrong path)

We wrote a first Python script to decode the text,

> ```python
>file = open('data.txt', 'rb')
>text = file.read()
>file1 = open('out.txt', 'wb')
>for i in range(len(text)):
>	try:
>		file1.write(text[i].decode('ISO-8859-1'))
>	except UnicodeEncodeError:
>		pass
>file.close()
>file1.close()
> ```

and saw that there was only white spaces, "o"s, and "p"s. Since the goal was to extract a picture, and since we had only two characters (except blank spaces), we guessed that
we should interpret it as binary data. We then modified our script (okay, it's quite dirty, but does the job):

> ```python
>file = open('data.txt', 'rb')
>text = file.read()
>file1 = open('out.txt', 'wb')
>number = ""
>for i in range(len(text)):
>	try:
>		c = text[i].decode('ISO-8859-1')
>		if c == "p":
>			number += "1"
>		elif c == "o":
>			number += "0"
>	except UnicodeEncodeError:
>		pass
>file1.write(hex(int(number,2))[2:-1].decode('hex'))
>file.close()
>file1.close()
> ```

We then typed this in our shell:

> ```bash
> % file out.txt 
>out.txt: SVG Scalable Vector Graphics image
> ```

We got the [following picture](out.svg).

Nice, but we were not done, because there was no visible flag! We opened the file with a text editor and saw that there was 2 embedded pictures, the first one with the barcode, and the second one with the girl.
Even if the barcode has been added above the first picture, it is not useful (it's only the ISBN13 of the book "Behind here eyes").
Actually, the solution was mush simpler: we only had to remove the picture with the girl, and we got:
![flag](flag.svg)
