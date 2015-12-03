JFLAGS = -cp
JC = javac
JAR = \
     ".:combinatoricslib-2.1.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar" \
#JAR = .:\* 
      
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $(JAR) $*.java

CLASSES = \
	MapUtil.java \
	MLtrainData.java \
	readXML.java \
	createTest.java \
	BasicSearch.java \
    	BruteForce.java \
	MLsearch.java \
	Evaluate.java \
	MultiThreadEvaluate.java \
	researchLinker.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
