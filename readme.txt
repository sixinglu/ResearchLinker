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

=====================================

Basic Search USAGE: 
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -b 'workfile' 'keywords' 'R. P. Hardikar' 'S. Mazumdar'

Machine Learning Search UASGE:
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -m 'workfile' 'keywords' 'R. P. Hardikar' 'S. Mazumdar'

Generate ML training data UASGE:
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -g 'workfile' 'keywords' 'R. P. Hardikar' 'S. Mazumdar'

BruteForce ML correct answers for all combination UASGE:  (it takes very long time to run)
java —Xmx6144m -Xincgc cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -t 'workfile' 'keywords' 'R. P. Hardikar' 'S. Mazumdar'

Evaluate with 100 combinations UASGE: (‘authorA' 'authorB' does not matter, combination generate randomly. This also takes long time and memory to run, I use multiple threads)
java —Xmx6144m -Xincgc -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -e 'workfile' 'keywords' 'R. P. Hardikar' 'S. Mazumdar'

