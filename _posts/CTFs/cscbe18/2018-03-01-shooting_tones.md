---
layout: posts
title:  CSCBE18 - Shooting tones
date:   2018-03-01
categories: [CTFs, cscbe18]
---

[~$ cd ..](/ctfs/cscbe18/2018/03/01/index.html)

We are given a [.wav file](/assets/res/CTFs/cscbe18/shooting_tones/shooting_tones.wav), and the goal is to find the hidden message. Steganography is not our area of expertise, but we opened the file with Audacity in order to search some clues.
We spent some hours on this challenge, and finally the answer was very simple ...
The music we can hear is Shooting Stars (Bag Raiders), and we first noticed that there was an inaudible trailing part:
![trail](/assets/res/CTFs/cscbe18/shooting_tones/trail.png)

Wrong path, solution was not there.

We tried several basic tests: spectrogram, change rate, low-pass filter, but found nothing immediately. Finally, we found the solution by setting the rate to 8'000 Hz and displaying the spectrogram:
![morse](/assets/res/CTFs/cscbe18/shooting_tones/morse.png)
We noticed these long and short dashes, the first and the third one being similar. We then thought about morse code, and finally found:

-.-. ... -.-. -... . .-- . .-.. --- ...- . -- --- .-. ... . or, **cscbewelovemorse**
