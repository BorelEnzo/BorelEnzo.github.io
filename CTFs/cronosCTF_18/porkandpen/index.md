# Pork Pen and Paper (50 pts)

>Taken directly from the book: "The history of treasure maps"  ...
>The ancient art of treasure map sketching and clue writing is frequently
>accompanied by a bit of pork meat to keep the writer focussed until his
>map was complete.
>There was a small decrease in popularity around 1800 due to difficult
>distribution of treasure maps when the senile pirate "old buzzard"
>started writing his cryptic guidance on live piggies...

### [~$ cd ..](../)

We are given a pigpen ciphertext:

![porkpenpaper](porkpenpaper.png)

First painful task: translate it.

> ```
>GNKL HNIJTL!
>JFWJYS MQQB TQ
>OGQW TDL FJ AUSL
>KNHDLI DLIL NS
>J CFJM TQ
>ILNPAUISL YQU
>CQI YQUI LCCQIT!
>TDL CFJM NS
>HNMHLGILNGVLGTLB
> ```

Because of letters frequencies, and because the main theme of the CTF, I guessed that the second word was "pirate". It gave me:

> ```
>GiKe pirate!
>aFWaYS MQQB tQ
>OGQW tDe Fa AUSe
>KipDer Dere iS
>a CFaM tQ
>reiPAUrSe YQU
>CQr YQUr eCCQrt!
>tDe CFaM iS
>piMpeGreiGVeGteB
> ```

It was actually a good guess, and after some substitions, I got:

> ```
>...
>the flag is
>pigpenreinvented
> ```

FLAG: **pigpenreinvented**
