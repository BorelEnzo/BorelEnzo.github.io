# Garfeld (Crypto)

### [~$ cd ..](../)

> You found the marketing campaign for a brand new sitcom. Garfeld!
> It has a secret message engraved on it.
> Do you think you can figure out what they're trying to say?

We are given the following picture:

![garfeld.png](garfeld.png)

Knowing that the flag format was "IceCTF{...}", we thought that it was a monoalphabetic substitution cipher, because of the "IjgJUO".

However, by xoring the string with the number in the top corner, we got: **IceCTF{I_DONT_THINK_GRONSFELD_LIKES_MONDAYS}**

Script:

> ```python
>key = '07271978'
>flag = 'IjgJUO{P_LOUV_AIRUS_GYQUTOLTD_SKRFB_TWNKCFT}'
>j = 0
>res = ''
>for i in xrange(len(flag)):
>	letter = flag[i]
>	if letter.isalpha():
>		newletter = ord(letter)-int(key[j % len(key)])-ord('A')
>		if newletter < 0:
>			newletter = ord('Z')+newletter+1			
>		else:
>			newletter = ord(letter)-int(key[j % len(key)])
>		res += chr(newletter)
>		j += 1
>	else:
>		res += letter
>print res
> ```
