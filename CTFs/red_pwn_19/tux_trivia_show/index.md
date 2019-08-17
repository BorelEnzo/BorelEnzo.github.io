# Tux Trivia Show - Misc

### [~$ cd ..](../)

>Tux Trivia Show
>
>Written by: Tux
>
>Win Tux Trivia Show!
>
>nc chall.2019.redpwn.net 6001

The running service was asking us for the capital city of a bunch of countries (as well as US states), and we only had a few seconds to send the answer back. Too fast and too boring, we had to script it.

By googling, we found the appropriate CSV files:
```bash
$ wget https://raw.githubusercontent.com/icyrockcom/country-capitals/master/data/country-list.csv
$ wget https://raw.githubusercontent.com/jasperdebie/VisInfo/master/us-state-capitals.csv
```

And python did the job:

```python
import csv
from pwn import *

dict_capitals = {}
def readcsv(filename):
    with open(filename, 'r') as csvFile:
        reader = csv.reader(csvFile)
        for row in reader:
            dict_capitals[row[0]] = row[1]
    csvFile.close()

readcsv("us-state-capitals.csv")
readcsv("country-list.csv")
p = remote("chall.2019.redpwn.net", 6001)
print p.readline()
while True:
    resp = p.read(1024)
    print resp
    state = resp[resp.rfind("capital of")+11:-2]
    if state not in dict_capitals:
        print state # state/country not found
        break
    else:
        p.sendline(dict_capitals[state])
        print p.readline() # read response
        print p.readline() # read next question
p.close()
```

Unfortunately, there was a lot of trial and errors because the service sometimes closed the connection, or because the CSV didn't contain exactly the expected name. Not a hard challenge, but quite frustrating...  
After a couple of hours, we finally got our flag: **flag{TUX_tr1v1A_sh0w+m3st3r3d_:D}**

EOF
