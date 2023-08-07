# BFT-SMaRt Decentralized Token Infrastructure (DTI)

The objective of this project was to build a decentralized token infrastructure to support an NFT
market using the [BFT-SMaRt replication library](http://bft-smart.github.io/library/).

## Quick start

To build and run our project, it is necessary to perform some steps that will be presented below. After downloading the zip file 
or cloning the repository, the following command must be executed in the **main directory** of the project in order to build it and
install all the required dependencies:

```shell
./gradlew installDist
```

Next, the following command must be executed to change to the directory where the servers can be run:

```shell
cd build/install/library
```

Once the above step is completed, three other terminals must be opened in the same directory, and in each of these, 
one of the following four commands must be executed, since the system requires four instances of the server to be 
running:

```shell
./smartrun.sh dti.DTICryptoServer 0
./smartrun.sh dti.DTICryptoServer 1
./smartrun.sh dti.DTICryptoServer 2
./smartrun.sh dti.DTICryptoServer 3
```

Finally, to interact with our system, at least one client must be running. To do this, the following command must be 
executed in a terminal in the same directory mentioned in the commands above:

```shell
./smartrun.sh dti.InteractiveClient <clientId >= 4>
```

Just like when executing the servers, an identifier must also be passed as an argument, which in this case refers to 
the client identifier. It is important to note that **only the client who has the identifier 4 can perform the `MINT(value)`**
operation that allows the creation of system coins.

_Notes:_ To perform the previous commands in the _Windows Command Prompt_ you will need to use `gradlew installDist` and
`smartrun.cmd` instead of the ones mentioned above.

### Optimizations

- The servers have as data structures four hash maps built in a way to allow us to make additions, removals and searches
in **constant time**.
- The input provided when referring to a NFT is its **unique** name, instead of the id.
- The client always receives the information of whether the operation perform was successful or not, and in case of a 
negative answer, in the returned response was added some negative values in order to inform the client with 
the more precision of the error that might have happened.
- Since it was not possible to know which NFTs received requests from other clients, was added to the return object of 
the operation `MY_NFTS()` a field `requests` that contains the number of pending requests for that NFT.
- In order to improve the user experience, **two additional operations were added**. The first called 
`EXISTING_NFTS()`, which returns all the existent NFTs, except the ones the client owns and the second named `MY_REQUESTS()`,
which returns all the pending requests made by the client. These operations were added because before was not possible to know
which NFTs were available to make transfer request, neither which requests made by a client were still pending.
- To improve the user experience while observing the results obtained, for the operations that can return more than
one tuple, **was built a beautiful table** in order to more easily observe the results returned by these operations. 

### Limitations

- Only the client with the identifier 4 can execute the `MINT(value)` coin operation, only for simplicity reasons.