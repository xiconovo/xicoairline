JC = javac
JFLAGS = -g

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -d build -cp build $*.java

all: server client

server: Requests.class Server.class 

client: Requests.class Client.class 

clean:
	$(RM) *.class