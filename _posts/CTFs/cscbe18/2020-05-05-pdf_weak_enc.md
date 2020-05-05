---
layout: posts
title:  CSCBE18 - PDF weak encryption
date:   2020-05-05
categories: [CTFs, cscbe18]
---

[~$ cd ..](../)

The statement of the challenge tells us that [the PDF file](/assets/res/CTFs/cscbe18/pdf_weak_enc/do_not_use_weak_encryption.pdf) has been encrypted with a 32 bytes random password. The goal is not to guess the password, but
to break the weak PDF encryption. A hint is given: the first byte of the key is 0x30

By looking inside the document using `nano`, we found the following elements:
> ```
>obj 7 0
><<
>/Filter /Standard
>/Length 40
>/O <539ed9bf5f890ee1653f1b0bb03f105369e8b62770ff880b3d1869e52a094a5d>
>/P -4
>/R 2
>/U <d538eec30f3d6571604a6bfcc68cb744230e6daa12482299a0a9816a81cccb4c>
>/V 1 >>
>
>xref
>...
>trailer
><<
>/Root 1 0 R
>/Size 8
>/ID [<8cd030382b6b2076d0a692ee38a74dfd><8cd030382b6b2076d0a692ee38a74dfd>]
>/Encrypt 7 0 R>>
>...
> ```

By reading the [PDF file specification ](https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/pdf_reference_1-7.pdf), section "Encryption", we cas see that:
> ```
> V: (Optional but strongly recommended) A code specifying the algorithm to be used in encrypting and decrypting the document:
>		0 An algorithm that is undocumented and no longer supported, and whose use is strongly discouraged.
>		1 Algorithm 3.1 on page 119 , with an encryption key length of 40 bits; see below.
>		2 (PDF 1.4) Algorithm 3.1, but permitting encryption key lengths greater than 40 bits.
>		3 (PDF 1.4) An unpublished algorithm that permits encryption key lengths ranging from 40 to 128 bits; see  implementation note 22 in Appendix H.
>		4 (PDF 1.5) The security handler defines the use of encryption and decryption in  the document, using the rules specified by the *CF*, *StmF*, and *StrF* entries.
> ```

Here, it was also a 40-bits key encryption using RC4 algorithm, and we already knew that the first byte of the key was 0x30.
We then used the tool [RC4-40-brute-pdf](https://github.com/kholia/RC4-40-brute-pdf), and modified the loop in `RC4-40-brute.c`:

> ```c
>for(i = is; i <= 255; i++) { /* time = 256 * 2.23 * 256 seconds ~= 40.6 hours ~= 1.7 days days */
>	for(j = js; j <= 255; j++) {
>		gettimeofday(&tv, NULL);
>		curtime=tv.tv_sec;
>		printf("%d %d @ ", i, j);
>		strftime(buffer,30,"%m-%d-%Y  %T.",localtime(&curtime));
>		printf("%s%ld\n",buffer,tv.tv_usec);
>		fflush(stdout);
>#pragma omp parallel for
>		for(k = ks; k <= 255; k++) {
>			int l, m;
>			for(l = ls; l <= 255; l++) {
>				unsigned char hashBuf[5];
>				hashBuf[0] = '0';
>				hashBuf[1] = (char)i;
>				hashBuf[2] = (char)j;
>				hashBuf[3] = (char)k;
>				hashBuf[4] = (char)l;
>				try_key(hashBuf);
>			}
>		}
>	}
>}
> ```

We let the program run for a while, and it finally gave us the following key **306462dce4**. However, we had then to decrypt the file...
Didier Stevens gave us the solution in [one of his blog posts](https://blog.didierstevens.com/2017/12/28/cracking-encrypted-pdfs-part-3/). In order to
decrypt, he modified the source of QPDF, and since it's a very nice guy, he published his patch. We only had to recompile and run it:

> ```bash
> qpdf-8.0.2/qpdf-8.0.2/qpdf/build/qpdf --decrypt --password=key:306462dce4 do_not_use_weak_encryption.pdf out.pdf
> ```

and finally, we got the flag **CSCBE{EFD75D1FBC103B39A9D71F8C56754178}**
