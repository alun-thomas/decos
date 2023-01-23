
JBIN=/usr/bin
JAVAC = $(JBIN)/javac -Xlint:-deprecation -Xlint:-fallthrough -Xlint:-unchecked -O

.SUFFIXES: .java, .class
.PRECIOUS: %.class
CLASSES := $(shell pwd )

JAVAFILES := $(shell find . -name "*.java" -print | grep -v MISC | grep -v misc)
ALLOBJS := $(shell find . -name "*.class" -print)
ALLOBJS1 := $(shell find . -maxdepth 1 -name "*.class" -print)
OBJS = $(JAVAFILES:%.java=%.class)
MAINS := $(shell echo *.java)
COBJS = $(MAINS:%.java=%.class)

all	: $(COBJS) $(OBJS) 

get:
	cp /home/alun/src/java/jpsgcs/DecosDemo.java .

getall:
	jar -xvf /home/alun/src/java/packaging/decos.jar 

clean	:
	/bin/rm -f $(ALLOBJS1)

sanitary:
	rm -f $(ALLOBJS)
	/bin/rm -f `find . -name "*.class"`

.java.class:
	$(JAVAC) -classpath $(CLASSES) $<

%.class: %.java
	$(JAVAC) -classpath $(CLASSES) $<

