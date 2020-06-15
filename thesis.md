---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: home
title: MA Thesis - Attacking mobile browsers with extensions
---

In 2020, I wrote a master thesis named _Attacking mobile browsers with extensions_, diving for the first time into the fascinating world of web browsers security. The printed version probably sleeps under thick a layer of dust somewhere on a shelf, but Professor Ramin SADRE, who supervised this work, advised me to make it live longer than a year, by making the content available for everyone. As I think that knowledge is not meant to die, I agreed, and here it is ...

## Abstract

Web browsing on mobile devices is nowadays a common practice. Since browsers can be viewed as pieces of software allowing a remote agent to execute code on someone else’s machine, security measures such as Same Origin Policy or Cross-Origin Resource Sharing are enforced. However, this minimal security level might be affected by third-party software, also known as browsers extensions. The latter are generally meant to improve the browsing experience or to offer customisation, but they can also be a powerful attack vector because of the privileges they are given. At the time of writing, mobile browsers do not all support extensions, hence a lack of research about this specific subject. While extensions security has been broadly studied, mobile devices were often put out of the scope because of this lack of support. The purpose of this thesis is to show that supporting extensions on mobile devices can also be really dangerous, because some weaknesses are inherent to this kind of devices. We present a set of attacks with proofs of concept, and discuss the likelihood as well as the efficiency.

## Acknowledgments

>First and foremost, I would like to thank my supervisor, Professor Ramin Sadre, for his advice, guidance and patience. Bringing fresh ideas and taking an outsider’s eye view, all of this was a considerable added value. Without his support, this thesis would probably not have been a reality. Counting from the first time I came in his office to explain my idea until now, I knew that someone constantly trusted and supported me. During this last year of studies, despite of
all difficulties I had to face, working on this thesis was not an easy task. Working from home, far away from my family and friends, and living under the phantom threat of the COVID-19, combining my academic work and my personal life in a sustainable way was always a challenge.  
>I feel really thankful to all people who offered me their support and inspiration. Many times, I was close to give up, but my family, hundreds of kilometres away, proved me that love was strong enough to overcome all the issues I was facing. To all my close friends, I would like to say my gratitude for what they did, and for have been there for me. I’m also really thankful to Laura D. and Laura M. for their patience, kindness, hope and listening. And finally, I would like to thank Salomé, without whom I would maybe never have written these words.
>
>_An erste Stelle möchte ich meinem Vorgesetzten Professor Ramin Sadre für seinen Rat, seine Anleitung und seine Geduld bedanken. Sowohl Ihre neuen Ideen als auch Ihre Aussensicht waren ein beträchtlicher Mehrwert. Ohne seine Unterstützung wäre diese These wahrscheinlich nicht zustande gekommen. Vom ersten Mal, als ich in sein Büro kam um ihm meine Idee zu erklären, bis heute wusste ich, dass mir immer jemand vertraute und mich unterstützte. In diesem letzten Studienjahr, trotz aller Schwierigkeiten mit denen ich konfrontiert war, war die Realisierung dieser These keine leichte Aufgabe. Von zu Hause auszuarbeiten, weit weg von meiner Familie und meinen Freunden, und unter der Phantombedrohung des COVID-19 zu leben, war immer eine Herausforderung meine akademische Arbeit und mein Privatleben auf nachhaltige Weise zu verbinden.  
>Ich bin wirklich dankbar an alle Menschen, die mir ihre Betreuung und Inspiration angeboten haben. Viele Male war ich kurz vor dem Aufgeben, aber meine Familie, Hunderte von Kilometern entfernt, bewies mir, dass die Liebe stark genug war, um alle Probleme zu bewältigen. Ich möchte allen meinen engen Freunden meinen Dank aussprechen für was sie getan haben, und dafür, dass sie für mich da waren. Ich bin auch Laura D. und Laura M. sehr dankbar für ihre Geduld, Freundlichkeit, Hoffnung und ihr Zuhören. Und schliesslich möchte ich Salomé danken, ohne den ich diese Worte vielleicht nie geschrieben hätte_

Please feel free to download the full content [here](/assets/res/thesis/Thesis_Borel_Enzo.pdf). This article summarises my work, but doesn't get to deep into the details.

Right from the beginning, I wanted to work on web browsers security. The major reference that gave me the first incentive was the _Browser Hacker's Handbook_ by Wade Alcorn. <img width="128" align="right" style="float:right; margin:10px;" alt="Icon kiwi" src="/assets/res/thesis/bhh.jpg">  I quickly realised how powerful and dangerous extensions could be, and started to go deeper by reading more and more about extensions security. I found a plethora of articles, but most of them mentioned the obsolete Add-on SDK and XUL/XPCOM technology.  
The second thing that caught my attention was that mobile devices were often forgotten, mainly because of the lack of support in the mobile version of Google Chrome. In the meantime, Firefox did, and browsers such as [Kiwi Browser](https://kiwibrowser.com/) did support extensions originally developed for desktop Chrome. <img width="96" height="96" align="left" style="float:left; margin:10px;" alt="Icon kiwi" src="/assets/res/thesis/ico_kiwi.png">  
But why Chrome doesn't support extensions ? What could happen if a browser supports them, while they were not meant to run on mobile devices ? Do mobile devices suffer from a larger attack surface ? All of these questions guided my research, and I tried to answer them in my thesis. I learned a lot, had a lot of fun, and will never forget such exciting experience !

[Practical attacks against mobile browsers using extensions](/thesis/2020/05/01/thesis1.html)  

The code of the proofs of concepts is freely available on my Github repository: [https://github.com/BorelEnzo/Extensions-against-mobile-browsers](https://github.com/BorelEnzo/Extensions-against-mobile-browsers)


Happy reading, and stay safe :blush:
