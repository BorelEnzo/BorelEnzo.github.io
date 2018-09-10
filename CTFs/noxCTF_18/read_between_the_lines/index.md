# Read between the lines

### [~$ cd ..](../)

We are given a [file](message.code) named message.code. We only had to rename it as "message.zip" to extract the [message](message).

The file starts with a long obfuscated javascript code, wich does nothing but display a dialog box saying "nope". The file also contains a lot of white spaces and tabulations:

> ...
>00004210: 0a0a 2020 0909 0a20 0a0a 0a09 0a0a 2020  ..  ... ......  
>00004220: 0920 2020 2020 2020 2020 0920 2020 0920  .         .   . 
>00004230: 0909 0920 0909 2020 0920 0909 0920 2020  ... ..  . ...   
>00004240: 0a20 0a20 0909 0920 0a20 0a09 2009 2009  . . ... . .. . .
>00004250: 0a09 0a20 2020 2020 090a 0920 2020 0a20  ...     ...   . 
>00004260: 0a09 2020 2020 2020 2020 2009 2020 2009  ..         .   .
>00004270: 2009 0909 2009 0920 2009 2009 0909 2020   ... ..  . ...  
>00004280: 200a 0a20 2009 2009 0a20 0a0a 200a 0a0a   ..  . .. .. ...
>00004290: 090a 0a

We ran this simple script to make it more readable:

> ```python
>file = open('message', 'r')
>lines = file.read().splitlines()
>res = ''
>for line in lines:
		if len(line) == 10:
		res += chr(int(line.replace('-','1').replace('.','0'),2))
>print res
> ```

It gave us: **'}ECAPSETIHWsIkcuFaD{FTCxon'**. We only had to do:

> ```sh
>$ echo '}ECAPSETIHWsIkcuFaD{FTCxon'|rev
>	noxCTF{DaFuckIsWHITESPACE}


