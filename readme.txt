Course: CSC583  
Assignment NO.: Final Project
Author: Sixing Lu
Language: Java

=====================================

Collection: arXiv
attach arXiv.py API for downloading paper collection.

zoom1: title
zoom2: author 
zoom3: affiliation
zoom4: publish time
zoom5: summary

each doc separated by <entry></entry>
each title separated by <title></title>
each author and affiliation separated by <author><name></name></author>
each published time separated by <published></published>

workfile: 1000 paper collection
workfile_run: another 1000 paper collection
keywords.txt: keywords used to machine learning
MLcoeff.txt: coefficient from linear machine learning model

=====================================

Basic Search USAGE: 
---------------------------
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -b 'workfile_run' 'keywords.txt' 'Olafur Jonasson' 'Andrei Manolescu'


Machine Learning Search UASGE:
---------------------------
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -m 'workfile_run' 'keywords.txt' 'Olafur Jonasson' 'Andrei Manolescu' 'MLcoeff.txt'


Generate ML training data UASGE: (it takes a little bit long, because encoding)
---------------------------
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -g 'workfile' 'keywords.txt' 'Olafur Jonasson' 'Andrei Manolescu'


BruteForce ML correct answers for all combination UASGE:  (it takes very very long time to run all combinations)
---------------------------
java -Xmx6144m -Xincgc -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -t 'workfile' 'keywords.txt' 'Olafur Jonasson' 'Andrei Manolescu'


Evaluate with 100 combinations UASGE: (combination generate randomly. This also takes long time and memory to run, I use multiple threads)
---------------------------
java -Xmx6144m -Xincgc -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -e 'workfile_run' 'keywords.txt' 'Olafur Jonasson' 'Andrei Manolescu' 'MLcoeff.txt'

