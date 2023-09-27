---
layout: posts
title:  Tiny File Manager - There's no place like home
date:   2023-09-27
categories: stuff
---

I remember this carpet, at the entrance of the Computer Science faculty, with this message _There's no place like 127.0.0.1/8_. A joke that would create two categories of people: those who got it, and those who don't. But in reality, is `127.0.0.1` that unique ?

The loopback is actually related to the whole `127.0.0.0/8` range, which is a humongous. In other words, an address like `127.x.x.x` is related to the local machine, and bound to the loopback interface. Although trying to reach a local service with the address `127.0.0.1` and `127.0.0.2` are not exactly the same, we can already see that actually, `127.0.0.1` is not that unique.

They are multiple ways to refer to a web service running on `localhost` using an IP address, for example (but not limited to):
* http://127.1
* http://127.0.1
* http://0x7f.1
* http://0x7f.0.0.0000000001
* http://0177.0.0.1
* http://0x7f000001
* http://017700000001

But in the end (and it actually does matter), IPv4 address are still 32 bits integers, and can be written as a single number. Dots in IPv4 notation are used for convenience, one dot per each 8-bits block, but they can somehow be omitted. It means that one can write it like `(127 << 24) + 1 = 2130706433`, and trying to reach http://2130706433 should be the same as http://127.0.0.1. A dot-free IPv4 address can also be written as hexadecimal (`0x7f000001`) or even octal (`017700000001`).

Web applications that let users fetch content from URLs often tend to forbid requests toward `127.0.0.1` to prevent from restricted content access. [Tiny File Manager](https://github.com/prasathmani/tinyfilemanager) does this, using the following regular expression (screenshot taken the 26.09.2023, time of writing):

<img width="100%" style="margin-left:auto;margin-right:auto;display:block;" alt="TFM regex" src="/assets/res/stuff/tfm_regex.png">

One can see that such regular expression is not efficient to prevent from an access to local resources. The impact is still mitigated by the fact that the URL must start with `http`.

The regex is built as follows:
* 1st alternative: the host is `localhost`
* 2nd alternative: `127` followed by a group made of a dot and a series of number. This group can be found up to two times, and must be followed by the same group. It would therefore match strings like `127.1`, `127.0.1`, `127.0.0.1`, but also `127.0.0.111111111111111` which is clearly not a valid IP address
* 3rd alternative: would match `::1`, the local address for IPv6.

However, `0x7f.0.0.1` or `2130706433` would bypass the regex. Relying of regular expressions to filter user inputs is often insufficient. But I guess that is not the worst to fear when deploying a Tiny File Manager :wink: