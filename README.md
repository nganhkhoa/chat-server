# Peer to Peer Chat server using TCP/IP

Group assignment....


## How it works

The server listen for connection on port 7789. A client will connect to the server to get list of IP of people in a room. The client will disconnect from the server and connect to the IPs.

The server only keep track of user on the network, the IP of user currently online and room status.

### The protocol

For simplicity, we use a basic method to query information from the server. The request and result will all be json encoded in base64.

The format of the request is:
```json
{
    "task": "tasktype",
    "param": {
        "param1": "value",
        "param2": "value"
    }
}
```

And the format of the result is:
```json
{
    "status": 0,
    "msg": "message return"
}
```

For a list of task type and status code, refer below.

### Connection to server

The server always listen for new connection, when a client connects, the server will create a thread to run the client. Then the client can communicate with the server.

### Connection to peer

// No idea now

### Database

This is a small and quick project, using a database will result in more time wasted. Instead, chosing an appropriate data structure reduces the work.

For user management, a hashmap where key is the username and value is the bytes of class Account.

For room management?

For log?

#### Implementation

The database we use is `MapDB`, a free and easy to use file database. There is only one problem that we cannot read a file that was written while it was open, a null pointer exception happens asap. Solving the problems by making a one instance of `MapDB` only. Avoid Singleton and we go by with passing the instance each time a client connects.

We also have a backup plan, in case we have to change the database. An abstract class of `DatabaseProvider` holds the methods required to successfully use the database, and all database must inherit from.

## Development

This project ultilize the use of Gradle to ease the work of installing an IDE, and configurations.

To get started, just make sure you have installed Gradle (~200MB).

On the root folder, where this file lies, execute:

```sh
./gradlew
```

This is setup gradle to work on this project. Next, build and run the project.

```sh
./gradlew build
./gradlew run
```

The code for the server lies at `src/main/java/`, the class for main is `ChatServer`.

Because we need a client to test, a simple `ChatClient` is put at `Client/`. Because java built-ins does not support json, we use Gson to do the work. To run the chat client, make sure to compile with Gson, and run with Gson.

Get Gson at [Maven](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.5). Download the jar file in this case it is `gson-2.8.5.jar`. Or this [link](http://central.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar) to download the jar file. Save it to `Client/gson-2.8.5.jar`.

```sh
# if you prefer wget:
# wget http://central.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar
javac -cp .:gson-2.8.5.jar ChatClient.java
java -cp .:gson-2.8.5.jar ChatClient
```

## References

### Task type

+ signin
+ signup
+ signout
+ choseroom
+ exit

### Status code

+ 0: exit
+ -1: unknown command
+ 200: ok
+ 403: access denied
+ 404: not found
