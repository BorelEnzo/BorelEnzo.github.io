## Not random

### [~$ cd ..](../)

We are given the [source code](notrandom.py) of a service. The principle is as follows: the service gives us the
md5 hash of the sum of 2 random numbers. One of these numbers has been generated with the module `secrets` and is 10 bits long,
whereas the second one is 32 bits long, but has been generated with the module `random`.

We have to send a number to the service, and if it is equal to the 10-bits secret number, we are given some points.
At the beginning we have 1'000 points. If the answer is wrong, we loose 500 points, and if we are right, we earn only 100 points... To get the flag,
we have to reach a score of 10'000 points.

Bruteforcing the hash could be feasible, but there is a smarter way to complete the task. Actually, the service expects us to send a number, but if we send a letter,
it prints the 2 random numbers, and proposes a new hash. It means that we can collect a serie of (pseudo)random values, and maybe recover the internal state of the
pseudo-random generator. Unfortunately, it's not as simple as C...

We then started to search how to do this, and found [this tool](https://github.com/eboda/mersenne-twister-recover). It worked perfectly!

We simply modified the routine in [MTRecover.py](MTRecover.py):

> ```python
>def recover(n):
>    mtb = MT19937Recover()
>    r2 = mtb.go(n)
>    return r2
> ``` 

and wrote our script which first collects more than 600 pseudo-random values, and then bruteforce each 10-bits number knowing the hash and send
it to the service.

(the script was not so simple to write, because we had to be patient...)

Finally, we got the flag: **INSA{Why_w0ulD_U_Us3_b4s1c_r4nd0m}**
[See full python code](solve.py)

