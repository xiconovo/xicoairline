JC = javac
JFLAGS = -g

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

all: server client

server: Requests.class Server.class 

client: Requests.class Client.class 

clean:
	$(RM) *.class