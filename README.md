# Socket Battleship
Programs created in the Java programming languages used to simulate a game of Battleship between two players over one network socket.
### Prerequisites

Java compiler or Java IDE, a network connection, a known open port

## Getting Started
  The user first needs to run BattleshipClient.java, passing two command line arguments: a hostname and an open port number. Please see the following example of a valid command line:
```
  java BattleshipClient cs.stanford.edu 100
```
  The above example would establish a game over port 100 using the cs.stanford.edu as a host.

After the host has established a game, run BattleshipServer.java, passing one command line argument: the port number. Please see the following example of a valid command line:
```
  java BattleshipServer  100
```
The above example would attempt to connect to a game over port 100.

After a connection has been established by both programs, both will send a "READY" signal in turns and wait to recieve one.
After "READY" signals have been recieved by both programs, the game will begin by requiring users to place their ships on their boards.

Afterwards, the EchoServer.java program shall make the first move.

## Authors

* **Jared McDonald** - *Initial work* - [Jaredx610](https://github.com/Jaredx610)
