# Bot communication (Web)

### [~$ cd ..](../)

We were given an IP and a port on which a bot was running, and the goal was to interact with it. _As we wrote this write up the service was unfortunately down, and we didn't keep all exact answers returned by the service_

By trying to reach the service running on `99.80.109.60:8080` using a simple HTTP GET request, we received something like:

>Unknown command: TW96aWxsYS81LjA=

By base64-decoding it, we found that our user agent was taken as a command, hence we tried to guess the commands the bot could understand. We first tried plaintext commands, such as:

> ```sh
>$ curl -H "User-Agent: help" http://99.80.109.60:8080
>	Unkown command: aGVscA== 
>$ curl -H "User-Agent: flag" http://99.80.109.60:8080 
>	Unknown command: ZmxhZw==
>$ curl -H "User-Agent: login" http://99.80.109.60:8080 
>	Unknown command: bG9naW4=
> ```

but we always received an error message with our command, base64 encoded. One of us suggested to send something giving a valid command, once base64-decoded, such as:

> ```sh
>$ curl -H "User-Agent: $(echo help|base64 -d)" http://99.80.109.60:8080 
>	Unknown command: help
> ```

Nope, wrong path. What we actually had to do was to send our commands, base64 encoded, such as:

> ```sh
>$ curl -H "User-Agent: $(echo HELP | base64)" http://99.80.109.60:8080
> ```

and finally the bot returned us a nice message, giving us the valid commands: **HELP**, **ECHO**, **TOKEN**, and **KEY**

HELP and ECHO were not useful, but KEY returned the string **C0FFEE** as a response and TOKEN gave us the following gibberish:

> ```sh
>$ curl -H "User-Agent: $(echo KEY | base64)" http://99.80.109.60:8080
>	C0FFEE
>$ curl -H "User-Agent: $(echo TOKEN | base64)" http://99.80.109.60:8080
>	c=&$'R$u!ptsvq&srq""#|vv"s#{"%rruq#}%Q# vupt~ppwTup "T;
> ```

In fact, there was a null byte at the beginning of the token we could not see. To get the exact answer, we piped the output into `xxd`:

> ```sh
>00000000: 0063 053d 2624 2752 2475 2170 7208 7473  .c.=&$'R$u!pr.ts
>00000010: 7671 2600 7f73 7271 2207 2223 7c76 7600  vq&..srq"."#|vv.
>00000020: 7f22 7323 7b04 2225 7272 7008 7571 237d  ."s#{."%rrp.uq#}
>00000030: 2551 2320 7675 7001 747e 7070 7754 7f75  %Q# vup.t~ppwT.u
>00000040: 7020 2254 3b0a                           p "T;.
> ```

Knowing the the flag format was `CSC{<string>}``` and that the first byte of the key was also a capital C, it was then clear that we had to xor the token and the key:

> ```python
>token = '0063053d262427522475217072087473767126007f737271220722237c7676007f2273237b042225727270087571237d2551232076757001747e707077547f75702022543b'.decode('hex')
>key = 'C0FFEE'
>print ''.join(chr(ord(key[i % len(key)])^ord(token[i])) for i in xrange(len(token)))
>	CSC{cadbb3d5182534e09574a7de93509d6f84dc773837f8faef303128554d935ead}
> ```
