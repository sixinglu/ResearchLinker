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
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -b 'workfile' 'keywords' 'authorA' 'authorB'

Machine Learning Search UASGE:
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -m 'workfile' 'keywords' 'authorA' 'authorB'

Generate ML training data:
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -g 'workfile' 'keywords' 'authorA' 'authorB'

BruteForce ML correct answers for all combination:  (it takes very long time to run)
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -t 'workfile' 'keywords' 'authorA' 'authorB'

Evaluate with 100 combinations: (‘authorA' 'authorB' does not matter, combination generate randomly)
java -cp .:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar researchLinker -e 'workfile' 'keywords' 'H. Howe' 'S. Mazumdar'

