# Tulip - Stock market simulation

Tulip is a stock market simulation. It works as a distributed system by using TCP socket communication.

This project was developed in the context of the course *Distributed Systems* delivered to first year MSc in Computer Science students at Paris-Dauphine University (academic year 2017-2018).

## Authors

* [Yanis Guimard](https://github.com/yanisguimard)
* [Thibaud Martinez](https://github.com/thibaudmartinez)

## Built with

* [Apache Maven](https://maven.apache.org/)
* [Jackson](https://github.com/FasterXML/jackson)
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)

## Working

There are three types of agents active in our stock market simulation: 
* the stock exchange
* brokers
* clients

The stock exchange communicates with one or serveral brokers. Similarly, each broker communicates with one or several clients.

The agents exchange various type of information such as purchase/sell orders or market states.

## Architecture

The architecture of the program consist of three layers.

### Sockets

The agents of the system communicate by using TCP sockets.
Message objects are send through sockets using JSON serialization/deserialization.

### The service layer

The service layer relies on socket communication and implements a producer-consumer algorithm to control the flow of data between the agents.

### The application layer

The application layer relies on the service layer. It takes care of the business logic. AppMessage objects are sent between agents using JSON serialization/deserialization and by encapsulating AppMessage objects in Message objects.

## Running the program

### Compiling the Maven project
```
mvn compile
```

### Running the stock exchange
```
mvn exec:java -Dexec.args="--stock-exchange --server-port 5000"
```

### Running the broker
```
mvn exec:java -Dexec.args="--broker --name david --server-port 4000 --host 127.0.0.1 --port 5000"
```

### Running the client
```
mvn exec:java -Dexec.args="--client --name emma --host 127.0.0.1 --port 4000"
```
