# Angry Pirate Parrot (65 pts)

>Polly the pirate parrot is guarding a chest full o' coins,
>can you get her some cookies, so we get past her? 
>She can be found on 51.68.252.196:8082

### [~$ cd ..](../)

The challenge was actually quite easy, even if the reward was 65 points.

By sending a request to the given service, we can get:

> ```sh
> % curl 51.68.252.196:8082 
>["wjRjt09","tcFkpmW","Fg0wcFp","t6ZOO6f","mbZHSwz"]
> ```

I searchd how to deal with this for a few minutes, and found that it was the next part of the URL:

> ```
> % curl 51.68.252.196:8082/wjRjt09
>["ezDQZ01","w7gx9gI","JGXbfKw","SPt2BnB","5AxzKJm"]
> ```

I wrote a first script to browse through all of these URLs, but some of them lead to nowhere:

> ```
> % curl 51.68.252.196:8082/wjRjt09/ezDQZ01/WMEkfGk/vhrCDjb/cCmPilq
> This path leads nowhere, return to a previous path.
> ```

Many trials-and-errors were needed to write the right script. In fact, fetching some URLs give cookies, and these cookies have to be sent back to the service.
Once we have the two required cookies, and URL leads to the flag.

Script:

> ```python
>import requests
>import urllib
>
>def fetch(suburl):
>	print '-------------------------------'
>	global baseurl
>	global cookie
>	url = baseurl+suburl
>	print url
>	r = requests.get(url, cookies=cookie)
>	if "This path leads nowhere" in r.text:
>		return
>	print r.text
>	x = r.text.replace('[','').replace(']', '').replace('"','')
>	suburls = x.split(',')
>	if len(suburls) != 5:
>		if "cookie" in r.text or "COOKIE" in r.text:
>			print r.cookies
>			cookie[r.cookies.keys()[0]] = urllib.unquote(r.cookies[r.cookies.keys()[0]]).decode('utf8')
>	for sub in suburls:
>		fetch(suburl+"/"+sub)
>
>baseurl = "http://51.68.252.196:8082"
>cookie = {}
>fetch('')
> ```

I redirected the output to a file, and as the script ends:
> ```sh
>% cat fetch.txt|grep -i flag  
> CRAAAH! your flag is: idontlikecrackers
> ```
