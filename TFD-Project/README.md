# Project Configurations

The main directory of the project is divided into 3 other directories, each with its own responsibilities:

- `Client`: This directory corresponds to the client-side library that allows clients to connect and access the service, namely, invoke operations.
- `Replica`: This directory contains the Raft algorithm implementation.
- `ReplicaContract`: This directory represents the contract where all the necessary objects and operations for communication are defined.

To run the project, start by installing the `ReplicaContract`. Next, start up the `Replicas`, and finally, execute the `Client` to invoke operations.

### Replica
To run each replica, first **obtain its JAR** through Maven, and then execute the following command to start it up. Since the config file has only 5 IPs, if you don't change it, there can be a maximum of 5 replicas (0 <= `id` <= 4). The config file allows the client and the replicas to know the IP addresses of the other replicas. The log file corresponds to the mechanism of log replication. In case the replica goes down, it can easily recover its state and update its log.

```bash
java -jar Replica-1.0-jar-with-dependencies.jar <id(>= 0)> <configFile(absolute path)> <logFile(absolute path)>
```

### Client
To run the client, start by obtaining its JAR the same way as before, and then execute the following command after inserting the absolute path for the desired config file.

```bash
java -jar Client.jar <configFile(absolute path)>
```

The text has been revised for clarity, consistency, and correctness.