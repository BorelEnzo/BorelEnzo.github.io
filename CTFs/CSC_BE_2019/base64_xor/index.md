# Base64 + xor = <3 (Crypto)

### [-$ cd ..](../)

Base64 and XOR are often involved in crypto challenges. What if we combined them ?

We were given an encrypted file and the command used to generate it: `base64 -w 0 message.txt | xortool-xor -s <notthekey> -f - > message.enc`

The [ciphertext](message.enc) was quite big, but we had absolutely no idea of what the plaintext could be (we thought about a simple Lorem ipsum). We spent a lot of time on this one
because we were too much focused on the size of the key, which was supposed to be a multiple of 4:

> ```sh
>$ xortool message.enc 
>	The most probable key lengths:
>	   2:   12.3%
>	   4:   13.8%
>	   6:   10.5%
>	   8:   11.5%
>	  10:   8.6%
>	  12:   9.4%
>	  14:   7.1%
>	  16:   7.8%
>	  23:   10.4%
>	  46:   8.7%
>	Key-length can be 4*n
>Most possible char is needed to guess the key!
> ```

We first tried with a length equal to 4 by trying to bruteforce the key with `-b` and `-o` switches, and then lengthes 8, 12, and 16 by trying every character as the most possible one, but it was not successful...
 
At a moment, as the end of the round approached, one of our team mate noted that we didn't even try the 23-bytes key, with a probability arounf 10%. We knew that bruteforceing a 23-bytes key was not possible, and then,
without too much confidence, we tried to bruteforce the most possible char:

> ```sh
>$ for i in {a..z}; do xortool -l 23 -c $i message.enc ; done
>	1 possible key(s) of length 23:
>	`\\sPBCQvrh\x1eAMGNs\x12pU\x14vQB
>	Found 0 plaintexts with 95.0%+ printable characters
>	See files filename-key.csv, filename-char_used-perc_printable.csv
>	1 possible key(s) of length 23:
>	c_pSA@Ruqk\x1dBNDMp\x11sV\x17uRA
>	Found 0 plaintexts with 95.0%+ printable characters
>	See files filename-key.csv, filename-char_used-perc_printable.csv
> ... snipped ...
> ```

Unfortunately, none of the plaintext contained more than 95% of printable characters. However, by using capital letters, the result was way more interesting:

> ```sh
>$ for i in {A..Z}; do xortool -l 23 -c $i message.enc ; done
> ... snipped ...
>	1 possible key(s) of length 23:
>	FzUvdewPTN8gkahU4Vs2Pwd
>	Found 1 plaintexts with 95.0%+ printable characters
>	See files filename-key.csv, filename-char_used-perc_printable.csv
> ... snipped ...
> ```

One key was made only of letters and numbers. We tried it, and finally, got our reward!

> ```sh
>"xortool-xor -s FzUvdewPTN8gkahU4Vs2Pwd -f message.enc  | base64 -d | grep CSC
>	Flag: CSC{I_w0nd3r_how_DEFLATE_wouId_do}
>	base64: invalid input
> ```

