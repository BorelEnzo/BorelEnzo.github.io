# L-Star - Misc

### [~$ cd ..](../)

>l-star
>
>Written by: jespiron
>
>All-star? More like L-star !!!
>
>Note: number of states <= 10.
>
>nc chall2.2019.redpwn.net 6002

I don't know if it was intended, but this challenge was really easy. However, it was still worth 364 as the CTF ended ...

We were given three files: [l_star.c](l_star.c), [nay.txt](nay.txt) and [yay.txt](yay.txt). Here is the content of the C code:

```c
#include <stdio.h>
#include <regex.h>
#include <stdlib.h>

int test(char* buf) {
    FILE* f = fopen("yay.txt", "r");
    char* str = NULL;
    ssize_t read;
    size_t len = 0;
    int result = 0;
    regex_t regex = { 0 };

    regcomp(&regex, buf, REG_EXTENDED);
    while ((read = getline(&str, &len, f)) != -1) {
        if(regexec(&regex, str, 0, NULL, 0) == REG_NOMATCH) {
            result = -1;
            break;
        }
    }
    fclose(f);
    if (result >= 0) {
        f = fopen("nay.txt", "r");
        while ((read = getline(&str, &len, f)) != -1) {
            if(regexec(&regex, str, 0, NULL, 0) == 0) {
                result = -1;
                break;
            }
        }
        fclose(f);
    }
    printf("result: %d", result);

    regfree(&regex);
    free(str);
    return result;
}

int main() {
    setbuf(stdout, NULL);
    setbuf(stdin, NULL);
    setbuf(stderr, NULL);
    puts("get the show on");

    char buf[90];
    fgets(buf, 90, stdin);

    if(test(buf) == 0) {
        FILE* f = fopen("flag.txt", "r");
        char flag[64];
        fgets(flag, 64, f);
        puts(flag);
        fclose(f);
    } else {
        puts("L");
    }

    return 0;
}
```

Quite easy to understand: we have to find a regex that matches all the lines in yay.txt and none of them in nay.txt:

Here are the first lines in yay.txt:

```
aba
abbbaabbaba
abbabbabbaaba
bbabbaaaaaaba
bbaaaaaaaaaba
bbaaaaabbaba
aaaaabbabbaba
aabbbbabbaba
bbbabbbabbaba
aabbbaabbaaba
```

and in nay.txt:

```
aabbbabbbab
bbbbaabbab
bbaabbbaaaaaa
aaabbbbbabb
abbabbaabbbbb
baabbabbbabbb
baabbbbaabbba
bbabbbabbbb
abbbbaabbabb
bbbbaabbbab
```

I then noticed that all lines in yay.txt ended with `aba` and that it was never true in nay.txt. The regex to use was then as simple as `.*aba$`

Let's send it to the service:

```
$ nc chall2.2019.redpwn.net 6002
get the show on
.*aba$
flag{h0w_m4nY_Ls_t1l_a115t4rz}

```

FLAG: **flag{h0w_m4nY_Ls_t1l_a115t4rz}**

EOF
