## Batou (Misc)

### [~$ cd ..](../)

>We manage to collect a dump from Batou's computer.
>Try to find info/notes that can help us
>Author: d3vnu11 

We are given an archive (batou.tar.gz) containing a memory dump (actually embedded in another archive).

Let's open it in volatility (be patient)

> ```sh
> $ volatility -f batou imageinfo
>Volatility Foundation Volatility Framework 2.6
>INFO    : volatility.debug    : Determining profile based on KDBG search...
>          Suggested Profile(s) : Win7SP1x64, Win7SP0x64, Win2008R2SP0x64, Win2008R2SP1x64_23418, Win2008R2SP1x64, Win7SP1x64_23418
>                     AS Layer1 : WindowsAMD64PagedMemory (Kernel AS)
>                     AS Layer2 : VMWareAddressSpace (Unnamed AS)
>                     AS Layer3 : FileAddressSpace (/home/enzo/Téléchargements/sectf/batou_chall/batou)
>                      PAE type : No PAE
>                           DTB : 0x187000L
>                          KDBG : 0xf800028480a0L
>          Number of Processors : 2
>     Image Type (Service Pack) : 1
>                KPCR for CPU 0 : 0xfffff80002849d00L
>                KPCR for CPU 1 : 0xfffff880009ea000L
>             KUSER_SHARED_DATA : 0xfffff78000000000L
>           Image date and time : 2018-09-11 04:17:17 UTC+0000
>     Image local date and time : 2018-09-10 21:17:17 -0700 
> ```

According to the statement, we have to look for infos or notes, so let's scan files:

> ```sh
> $ volatility -f batou --profile=Win7SP1x64 filescan > files.txt
> ```

Here is [files.txt](files.txt)

By searching the word "note" in Geany, we found the following list:

> ```
>files.txt:44: 0x000000003dc3d3d0     16      0 R--rw- \Device\HarddiskVolume2\ProgramData\Microsoft\Windows\Start Menu\Programs\Notepad++.lnk
>files.txt:61: 0x000000003dc44f20      9      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\mimeTools\mimeTools.dll
>files.txt:62: 0x000000003dc47780     17      1 RW-r-- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Microsoft\Sticky Notes\StickyNotes.snt
>files.txt:84: 0x000000003dc5fbc0      5      0 R--r-d \Device\HarddiskVolume2\Windows\System32\notepad.exe
>files.txt:90: 0x000000003dc62070     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Accessories\Notepad.lnk
>files.txt:106: 0x000000003dc7bf20     16      0 R--r-d \Device\HarddiskVolume2\Users\Batou\Searches\Sticky Notes (Windows Sticky Notes).searchconnector-ms
>files.txt:113: 0x000000003dc8fbc0     16      0 R--r-d \Device\HarddiskVolume2\Windows\System32\en-US\notepad.exe.mui
>files.txt:114: 0x000000003dc8fdd0     16      0 R--r-d \Device\HarddiskVolume2\Program Files (x86)\Notepad++\SciLexer.dll
>files.txt:115: 0x000000003dc8ff20     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\config.xml
>files.txt:149: 0x000000003de6df20     16      0 R--rwd \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\plugins\config\DSpellCheck.ini
>files.txt:573: 0x000000003e1ffa10     13      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\langs.xml
>files.txt:574: 0x000000003e1ffcb0      8      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\DSpellCheck\DSpellCheck.dll
>files.txt:637: 0x000000003e27da50     15      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\SciLexer.dll
>files.txt:936: 0x000000003e3fb3b0     16      0 R--rw- \Device\HarddiskVolume2\ProgramData\Microsoft\Windows\Start Menu\Programs\Accessories\Sticky Notes.lnk
>files.txt:1054: 0x000000003e50fd60     15      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\notepad++.exe
>files.txt:1139: 0x000000003e55f070     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\shortcuts.xml
>files.txt:1140: 0x000000003e55f300     15      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\stylers.xml
>files.txt:1451: 0x000000003f38e6b0      1      1 R--r-d \Device\HarddiskVolume2\Windows\System32\en-US\notepad.exe.mui
>files.txt:1508: 0x000000003fe9c930     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\backup\new 2@2018-09-10_203737
>files.txt:1510: 0x000000003fead410     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\backup\new 1@2018-09-10_202915
>files.txt:1511: 0x000000003fec75a0     16      0 R--rwd \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\plugins\config\converter.ini
>files.txt:1512: 0x000000003fec76f0      9      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\NppConverter\NppConverter.dll
>files.txt:1513: 0x000000003fecaf20     10      0 R--rw- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\Config\Hunspell\en_US.dic
>files.txt:1514: 0x000000003fecc770     16      0 R--rw- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\Config\Hunspell\en_US.aff
>files.txt:1515: 0x000000003fecc8c0     12      0 R--r-- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\plugins\NppExport\NppExport.dll
>files.txt:1516: 0x000000003fece070     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\contextMenu.xml
>files.txt:1517: 0x000000003fece220     16      0 R--rw- \Device\HarddiskVolume2\Program Files (x86)\Notepad++\change.log
>files.txt:1519: 0x000000003feced10     16      0 R--rw- \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\session.xml
>files.txt:1526: 0x000000003ffa21c0     13      0 R--r-d \Device\HarddiskVolume2\Program Files (x86)\Notepad++\notepad++.exe
> ```

Files located in \Device\HarddiskVolume2\Users\Batou\AppData\Roaming\Notepad++\ seem to be very interesting .... To extract them, I did:

> ```
> $ volatility -f batou --profile=Win7SP1x64 dumpfiles -D out_files/ -Q 0x000000003fe9c930
> $ volatility -f batou --profile=Win7SP1x64 dumpfiles -D out_files/ -Q 0x000000003fead410
> $ volatility -f batou --profile=Win7SP1x64 dumpfiles -D out_files/ -Q 0x000000003feced10
> ```

__The option -Q takes the offset of the file as value.__

One of them contains some interesting numbers:

> ```sh
> $ xxd out_files/file.None.0xfffffa8000da35c0.dat|head
>00000000: 0d0a 3533 2034 350d 0a34 3320 3534 2037  ..53 45..43 54 7
>00000010: 6220 0d0a 3334 2036 6320 3663 2035 6620  b ..34 6c 6c 5f 
>00000020: 3739 2036 6620 3735 2037 3220 3566 2034  79 6f 75 72 5f 4
>00000030: 6520 3330 2037 3420 3333 2037 3320 3566  e 30 74 33 73 5f
>00000040: 2033 3420 3732 2033 3320 3566 2036 3220   34 72 33 5f 62 
>00000050: 3333 2036 6320 3330 2036 6520 3637 2035  33 6c 30 6e 67 5
>00000060: 6620 3734 2033 3020 3566 2037 3520 3335  f 74 30 5f 75 35
>00000070: 0d0a 3764 0000 0000 0000 0000 0000 0000  ..7d............
>00000080: 0000 0000 0000 0000 0000 0000 0000 0000  ................
>00000090: 0000 0000 0000 0000 0000 0000 0000 0000  ................
> ```

I recognized the curly brackets (0x7b and 0x7d), and did:

> ```python
> >>> x = [0x53,0x45,0x43,0x54,0x7b,0x34,0x6c,0x6c,0x5f,0x79,0x6f,0x75,0x72,0x5f,0x4e,0x30,0x74,0x33,0x73,0x5f,0x34,0x72,0x33,0x5f,0x62,0x33,0x6c,0x30,0x6e,0x67,0x5f,0x74,0x30,0x5f,0x75,0x35,0x7d]
> >>> "".join(chr(i) for i in x)
>	'SECT{4ll_your_N0t3s_4r3_b3l0ng_t0_u5}'
> ```
