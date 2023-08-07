# Decentralized Finance (DeFi)

## How to Run the Project

**To run this project, follow these steps:**

1. Begin by deploying the smart contracts `nft.sol` and `decentralized_finance.sol`:
   1. You can utilize the [Remix IDE](http://remix.ethereum.org/) directly in your browser.
   2. To acquire some ETH, select a test network within the MetaMask browser extension and obtain tokens for the corresponding network by using a faucet (for example, for the Sepolia testnet, you can use the [sepoliafaucet.com](https://sepoliafaucet.com)).
2. Copy the ABI of each contract to the corresponding `.json` file.
3. Paste the contract addresses into the top section of the `main.js` file as indicated.

**With the contracts successfully deployed, follow these steps to initiate the local server:**

1. Install the latest version of [Python](https://www.python.org/downloads/) on your computer (the project was developed using version 3.14.4).
2. Inside the project directory, execute the following command:
   ```bash
   python -m http.server 8080
   ```
3. Once the local server is running, open your preferred web browser and navigate to `localhost:8080` to begin using the application.

For a comprehensive overview of all the project's functionalities, refer to the [Project Description](DTI-Projects/DeFi/docs/Project-Description).

## Important Notes:

- To add NFTs to the `nft.sol` contract, you can utilize the `InterPlanetary File System (IPFS)`. This decentralized, peer-to-peer protocol is employed for file storage and sharing in blockchain and decentralized applications such as NFT marketplaces and decentralized storage solutions. 
  - Create an account at [web3.storage](https://web3.storage), navigate to [your account](https://web3.storage/account/), and upload an image. Copy the full URL of your uploaded image and paste it into the `mint` function.
- For loan requests involving NFTs, you need to use the `approve` or `setApprovalForAll` functions to grant the decentralized_finance contract permission to transfer NFT ownership.

### Contract Observations:

- Modifier functions are incorporated within all contract functions to ensure their correct usage. For instance, the `buyDex` function includes a modifier to confirm whether the contract has adequate DEX supply. Other modifiers validate conditions like sender NFT ownership in the `makeLoanRequestByNft` function or approval for changing NFT ownership.
- A new map, `nftLoans`, is introduced to enable more efficient retrieval of the associated `loanId` of an NFT without iterating through the entire loans map.
- The variable `lockedDex` is introduced to store DEX amounts loaned that cannot be withdrawn until the deadline expires or the loan is repaid. This ensures that the corresponding DEX amount is available if the borrower repays the `ethAmount` of the loan.
- In cases where the sender's payment (whether in ETH or DEX) leaves a remainder, the contract returns that remaining amount.
- The contract includes a function, `checkLoanStatus`, enabling efficient checking of all loan statuses with a single function call. This minimizes gas costs and improves user experience.
- To ensure accuracy and ease of use, functions that receive a `dexAmount` expect the input in Wei.

### Frontend Observations:

- The `getLoanRequests` function is added to retrieve NFT requests without assigned lenders.
- All implemented functions allow connected accounts to access only their associated data. Users can view their own NFT requests, borrows, etc. The contract owner, however, can view all outstanding unpaid loans.
- Prior to executing transactions that alter the contract's state, a `.call()` request is made to verify if modifier function conditions are met. This process helps users confirm conditions without incurring gas costs. If no errors arise, a transaction is initiated to cover gas fees.
- The contract owner panel only appears in the browser for users who are actually owners of the DeFi contract.
- To facilitate operation, the contract owner must approve the `checkLoanStatus` function every 10 minutes.

### General Observations:

- The loan duration is capped at 7 days.
- Loan amount deductions equate to 5% per day, reaching a maximum of 35% over 7 days.
- The value of one DEX is initially set at 0.001 ETH, or 1000 DEX to 1 ETH. As such, the rateEthToDex is $10^{15}$. When setting the rate in the *setRateEthToDex* function, input only the number of zeros for DEX equivalent to 1 ETH (in this case, 3).