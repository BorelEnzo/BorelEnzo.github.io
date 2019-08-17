# Trinity - Crypto

### [~$ cd ..](../)

>Trinity
>
>Written by: blevy
>
>redpwn claims to have invented an unbreakable "trinity encoding." Security by obscurity!
>
>1202010210201201021011200200021121112010202012010210102012102021000200121200210002021210112111200121200002111200121102000021211120010200212001020020102000212
>
>NB: This challenge does not use the flag{...} format. The flag is only lowercase letters.
>
> Hint: The encoding is so simple, even your grandma knows about it!

The purpose of the hint was to make us understand that it was a well-known old encoding method, and the intuition made me think about morse code, where 0, 1, and 2 could represent dashes, dots and spaces.

Intuitively, I thought that 0 were dots and dashes were 1, and this guess was actually correct:

`- . .-. -. .- .-. -.-- .. ... -- --- .-. . .- .-. -.-. .- -. . -... ..- - .. -... . - -.-- --- ..- - .... --- ..- --. .... - --- ..-. .. - ..-. .. .-. ... - `

Or, once decoded:
`TERNARYISMOREARCANEBUTIBETYOUTHOUGHTOFITFIRST`

EOF
