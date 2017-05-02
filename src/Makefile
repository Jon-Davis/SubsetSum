CLASSES = $(wildcard *.java)

.PHONY: all classes clean
.SUFFIXES: .java .class

.java.class:
	javac $(JFLAGS) $*.java

all: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class