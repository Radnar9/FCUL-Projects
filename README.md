# FCUL Projects

In this repository, I present some of the most important and complete projects developed during my Master's Degree in Computer Science and Engineering at FCUL.

## [TFD Project](TFD-Project/)

The project developed in the "Distributed Fault Tolerance" course, had the objective of providing students with hands-on experience in building fault-tolerant distributed systems, specifically by implementing the Raft consensus algorithm. You can find a detailed description of the protocol in the [Raft paper](https://raft.github.io/raft.pdf).

The project was divided into three phases:

1. **Communication Abstractions:** In this phase, we implemented the communication support for the replicas, which must be able to invoke operations and receive replies from other replicas. The developed library supports `one-to-one` and `one-to-many Quorum` RPC communications through the `gRPC framework`.

2. **Leader Election Algorithm:** The second phase involved implementing the leader election algorithm. Replicas start as *followers* and attempt to become the *leader* until one of them is elected. You can observe a visualization of this protocol [here](https://raft.github.io/).

3. **Log Replication:** The final part of the project focused on implementing log replication to support the replicated execution of requests from an unbounded number of clients.

The developed system can handle the following situations without compromising the safety and liveness of the state machine replication:

- Start without a predefined leader and elect one of the replicas as soon as more than half of them are available.
- Execute requests from a single client.
- Execute requests from multiple clients running simultaneously.
- Continue working despite the failure of a follower replica.
- Continue working despite the failure of a leader replica.
- Recover failed replicas and update their log.
- Handle failure of more than half of the system (the system will stop until a majority is recovered).
- Handle the failure and recovery of all machines without losing state.


## [PSD Project](PSD-Project/)
In the "Data Privacy and Security" course, we developed a secure peer-to-peer (P2P) messaging application with the following characteristics:

- **Decentralization:** The application is designed to be as decentralized as possible.
- **End-to-End Encryption:** Messages are encrypted end-to-end to ensure security, such as privacy, integrity, and authenticity.
- **Reliability Guarantees:** The application provides standard reliability guarantees, including message delivery and ordering.
- **Group Chats:** Users can engage in group chats defined by topics.
- **Long-Term Message Storage:** Messages are stored securely for the long term, and also replicated, ensuring availability.
- **Message Searching:** The application supports privacy-preserving message searching.

To achieve these results, several strategies were employed, including the use of the following techniques:

- BCrypt
- JSON Web Tokens (JWT)
- AES with GCM (Galois/Counter Mode) and no padding
- Shamir's Secret Sharing
- Identity-Based Encryption (used for communication between two users)
- Attribute-Based Encryption (used for group communication)
- Dynamic Searchable Symmetric Encryption

For more detailed information about the implementation, please refer to the [project report](PSD-Project/docs/Report-EN.pdf).


## [TS Project](TS-Project/)
In the "Security Technologies" course, we designed and implemented an eCommerce communication protocol intended to be used by three distinct entities:

- **MBeC (Client):** This entity enables bank customers to generate virtual credit cards, deposit money into their accounts, and conduct online shopping.
- **Bank:** Functioning as the server, the bank is responsible for maintaining customer balances and managing virtual credit cards.
- **Store:** The store entity facilitates customers in carrying out online shopping.

The developed protocol established a secure channel between the Client-Bank and Store-Bank, utilizing an authentication file generated during the Bank's startup. A comparatively less secure channel was established between the Client and the Store. However, the entire communication protocol was designed to safeguard against various types of attacks:

- **Correctness Violations:** If received messages lack the expected structure or adhere to other defined values, the process is terminated.
- **Integrity Violations:** Through the use of symmetric encryption AES with GCM (Galois/Counter Mode) and no padding, the system guarantees data privacy, integrity, and authenticity.
- **Confidentiality Violations/Packet Sniffing:** Confidentiality is ensured not only by employing strong encryption but also by initializing the protocol with a handshake. This handshake employs Elliptic Curve Diffie-Hellman (ECDH), generating a new session key for each communication session. Random parameters are always used in this handshake, making it computationally challenging for adversaries to replicate the parameters.
- **Replay Attacks:** Prevention of such attacks is achieved by generating a new key for every communication session. Additionally, each message contains a timestamp enabling the system to confirm if the message was received within an expected temporal window.
- **Man-in-the-middle Attacks:** Solutions applied for the previously mentioned attacks also serve as countermeasures for man-in-the-middle attacks. These solutions include using ECDH with random values, employing a robust encryption scheme, and including timestamps in each message.
- **Brute Force and DoS Attacks:** To mitigate this vulnerability, a challenge-response mechanism was implemented. Challenges are dispatched to users when the server detects an excessive load of requests. Users are required to expend computational resources to solve the challenge and send back the solution to the server before proceeding with the desired request.


## [DTI Projects](DTI-Projects/)
In the "Intrusion Detection and Tolerance" course, we undertook the development of two main projects:

1. A [**decentralized token infrastructure (DTI)**](DTI-Projects/BFT-SMaRt-DTI) designed to support an NFT (Non-Fungible Token) market. This project leveraged the [BFT-SMaRt replication library](http://bft-smart.github.io/library/) to achieve its goals. 
   - The DTI project encompasses a deterministic wallet-like service managing coins based on the UTXO (Unspent Transaction Output) model, as introduced in Bitcoin. Additionally, it supports the transacting of NFTs using the coins.
   - This endeavor provided an opportunity to directly engage with a cutting-edge BFT (Byzantine Fault Tolerance) system library and apply its features to the desired infrastructure context.

2. The [**Decentralized Finance (DeFi)**](DTI-Projects/Decentralized-Finance) project was aimed at creating an **application that facilitates the swapping of tokens between ETH and a customized DEX** (fungible) token on the `Ethereum blockchain`. This application empowered users to engage in buying and selling DEX. Furthermore, users could utilize their DEX holdings and NFTs as collateral to secure ETH loans. Precisely, users could borrow up to fifty percent of the value of their DEX holdings or NFT tokens in ETH. Upon loan repayment, users retained ownership of the DEX or NFT tokens initially presented as collateral.
   - The development of the application incorporated several technologies:
     - `Smart contracts` authored in the `Solidity` language, adhering to the **ERC721** (for NFTs) and **ERC20** (for fungible tokens) standards.
     - Client-side implementation using HTML, CSS, and JavaScript, facilitated by `Web3.js`.
     - Integration with MetaMask.

For more comprehensive information about the DeFi project, please refer to the [project's directory](DTI-Projects/Decentralized-Finance).

<!-- ## [Final Project / Master's Thesis](Final-Project/) -->
