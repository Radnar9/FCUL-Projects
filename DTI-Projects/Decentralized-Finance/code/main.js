const web3 = new Web3(window.ethereum);

// the part is related to the DecentralizedFinance smart contract
const defiContractAddress = "0xcD59950c1bB29c0Aa8A0C9799eCbF3cDaf8Fb542"; // PASTE CONTRACT'S ADDRESS
import { defi_abi } from "./abi_decentralized_finance.js";
const defiContract = new web3.eth.Contract(defi_abi, defiContractAddress);

// the part is related to the the SimpleNFT smart contract
const nftContractAddress = "0x189Af21cB1735E9C610DF6A69349D08174292503"; // PASTE CONTRACT'S ADDRESS
import { nft_abi } from "./abi_nft.js";
const nftContract = new web3.eth.Contract(nft_abi, nftContractAddress);

async function connectMetaMask() {
    if (window.ethereum) {
        try {
            const accounts = await window.ethereum.request({
                method: "eth_requestAccounts",
            });
            console.log("Connected account:", accounts[0]);
            location.reload();
        } catch (error) {
            console.error("Error connecting to MetaMask:", error);
        }
    } else {
        console.error("MetaMask not found. Please install the MetaMask extension.");
    }
}

/* ---------| Contract functions |--------- */
async function buyDex(ethAmount) {
    try {
        const fromAddress = await getConnectedAccount();;
        await defiContract.methods.buyDex().call({
            from: fromAddress,
            value: web3.utils.toWei(ethAmount, "ether"),
        });
        await defiContract.methods.buyDex().send({
            from: fromAddress,
            value: web3.utils.toWei(ethAmount, "ether"),
        });

        printSuccessfulMessage("DEX successfully bought");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}


async function sellDex(dexAmount) {
    try {
        const fromAddress = await getConnectedAccount();;
        const dexToWei = new BigNumber(dexAmount * (10 ** (await getRateEthToDex()))).toFixed();
        await defiContract.methods.sellDex(dexToWei).call({ from: fromAddress });
        await defiContract.methods.sellDex(dexToWei).send({ from: fromAddress });

        printSuccessfulMessage("DEX successfully sold");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function loan(dexAmount, deadlineDate) {
    const deadline = Date.parse(deadlineDate) / 1000;
    try {
        const fromAddress = await getConnectedAccount();;
        const dexToWei = new BigNumber(dexAmount * (10 ** (await getRateEthToDex()))).toFixed()
        await defiContract.methods.loan(dexToWei, deadline).call({ from: fromAddress });
        await defiContract.methods.loan(dexToWei, deadline).send({ from: fromAddress });

        printSuccessfulMessage("Loan created");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function returnLoan(loanId, ethAmount) {
    try {
        const fromAddress = await getConnectedAccount();;
        await defiContract.methods.returnLoan(loanId).call({
            from: fromAddress,
            value: web3.utils.toWei(ethAmount, "ether")
        });
        await defiContract.methods.returnLoan(loanId).send({
            from: fromAddress,
            value: web3.utils.toWei(ethAmount, "ether")
        });

        printSuccessfulMessage("Loan successful returned");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function getEthTotalBalance() {
    try {
        const weiTotalBalance = await defiContract.methods.getEthTotalBalance().call();
        const ethTotalBalance = web3.utils.fromWei(weiTotalBalance, 'ether');

        return ethTotalBalance;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function setRateEthToDex(rate) {
    try {
        if (!(await isOwner())) {
            console.log("User is not owner!");
            return;
        }
        const fromAddress = await getConnectedAccount();
        await defiContract.methods.setRateEthToDex(rate).call({ from: fromAddress, });
        await defiContract.methods.setRateEthToDex(rate).send({ from: fromAddress, });

        printSuccessfulMessage("Rate set with success");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}


async function getDex() {
    try {
        const fromAddress = await getConnectedAccount();
        const dexAmountInWei = await defiContract.methods.getDex().call({ from: fromAddress });
        const rateEthToDex = 10 ** (await getRateEthToDex());
        const dexAmount = dexAmountInWei / rateEthToDex;

        return dexAmount;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function makeLoanRequestByNft(nftId, ethLoanAmount, deadlineDate) {
    const deadline = Date.parse(deadlineDate) / 1000;
    try {
        const fromAddress = await getConnectedAccount();
        await defiContract.methods.makeLoanRequestByNft(
            nftContractAddress,
            nftId,
            web3.utils.toWei(ethLoanAmount, "ether"),
            deadline
        ).call({ from: fromAddress });
        await defiContract.methods.makeLoanRequestByNft(
            nftContractAddress,
            nftId,
            web3.utils.toWei(ethLoanAmount, "ether"),
            deadline
        ).send({ from: fromAddress });

        printSuccessfulMessage("Loan request created");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function cancelLoanRequestByNft(nftId) {
    try {
        const fromAddress = await getConnectedAccount();
        await defiContract.methods.cancelLoanRequestByNft(nftId).call({ from: fromAddress });
        await defiContract.methods.cancelLoanRequestByNft(nftId).send({ from: fromAddress });

        printSuccessfulMessage("Request successful cancelled");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function loanByNft(nftId) {
    try {
        const fromAddress = await getConnectedAccount();
        await defiContract.methods.loanByNft(nftId).call({ from: fromAddress });
        await defiContract.methods.loanByNft(nftId).send({ from: fromAddress });

        printSuccessfulMessage("Successful lend and loan created");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function checkLoan(loanId) {
    try {
        if (!(await isOwner())) {
            console.log("User is not owner!");
            return;
        }
        const fromAddress = await getConnectedAccount();
        await defiContract.methods.checkLoan(loanId).call({ from: fromAddress });
        await defiContract.methods.checkLoan(loanId).send({ from: fromAddress });

        printSuccessfulMessage("Loan successful checked");
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}




/* ---------| Non contract functions |--------- */
async function getRateEthToDex() {
    try {
        const rateEthToDex = await defiContract.methods.rateEthToDex().call();
        const exponentRate = rateEthToDex.toString().length - 1; // - 1 to remove the first number

        return exponentRate;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

// Get all the available NFTs to loan ETH
// Only shows the NFT requests that are not from the owner and don't have a lender yet
async function getAvailableNfts() {
    try {
        const fromAddress = await getConnectedAccount();
        const tokenIdCounter = await nftContract.methods.tokenIdCounter().call({ from: fromAddress });
        const nfts = [];

        for (let tokenId = 1; tokenId <= tokenIdCounter; tokenId++) {
            const loanId = await defiContract.methods.nftLoans(tokenId).call({ from: fromAddress });
            if (loanId == 0) continue;

            const loan = await defiContract.methods.loans(loanId).call({ from: fromAddress });
            if (loan.borrower.toLowerCase() === fromAddress.toLowerCase()) continue;
            if (loan.lender !== "0x0000000000000000000000000000000000000000") continue;

            const tokenURI = await nftContract.methods.tokenURI(tokenId).call();

            loan.id = loanId;
            loan.uri = tokenURI;
            loan.amountEth = web3.utils.fromWei(loan.amountEth, 'ether').toString();
            nfts.push(loan);
        }
        return nfts;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}


async function checkLoanStatus() {
    const checkLoans = async function () {
        try {
            if (!(await isOwner())) {
                console.log("User is not owner!");
                return;
            }
            const fromAddress = await getConnectedAccount();
            await defiContract.methods.checkLoanStatus().send({ from: fromAddress });

            printSuccessfulMessage("All loans status checked");
        } catch (error) {
            verifyAndPrintErrorMessage(error);
        }
    }
    window.setInterval(checkLoans, 10 * 60 * 1000);
}

/**
 * Gets all the loans not yet paid
 */
async function getTotalBorrowedAndNotPaidBackEth() {
    try {
        const fromAddress = await getConnectedAccount();
        const loanIdCounter = await defiContract.methods.loanIdCounter().call({ from: fromAddress });
        const isContractOwner = isOwner();
        const loans = []

        for (let loanId = 1; loanId <= loanIdCounter; loanId++) {
            const loan = await defiContract.methods.loans(loanId).call({ from: fromAddress });
            if (loan.isBasedNft && loan.lender === "0x0000000000000000000000000000000000000000" || loan.deadline == 0){
                continue;
            } 
            if (!(await isContractOwner) && (loan.borrower.toLowerCase() !== fromAddress.toLowerCase() && loan.lender.toLowerCase() !== fromAddress.toLowerCase())) {
                continue;
            }

            loan.id = loanId;
            loan.isCallerOwner = loan.borrower.toLowerCase() === fromAddress.toLowerCase();
            loan.amountEth = web3.utils.fromWei(loan.amountEth, 'ether').toString();
            loans.push(loan)
        }

        return loans
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function listenToLoanCreation() {
    if (!(await isOwner())) {
        console.log("User is not owner!");
        return;
    }

    let options = { filter: { value: [] }, fromBlock: 0 };

    defiContract.events.LoanCreated(options)
        .on('data', res => {
            const event = res.returnValues;
            const elem = document.createElement("tr");
            elem.innerHTML = `
                <td>${event.borrower}</td>
                <td>${web3.utils.fromWei(event.ethAmount, 'ether').toString()} ETH</td>
                <td>${new Date(event.deadline * 1000).toLocaleDateString("en-GB") +
                    " " +
                    new Date(event.deadline * 1000).toLocaleTimeString("en-GB")}
                </td>`;
            const table = document.getElementById("tabela");
            table.appendChild(elem);
        })
        .on('error', error => {
            console.error('Error: ', error);
        });
}

/**
 * Get all token URIs of the sender
 */
async function getAllTokenURIs() {
    try {
        const tokens = [];
        const fromAddress = await getConnectedAccount();
        const tokenIdCounter = await nftContract.methods.tokenIdCounter().call({ from: fromAddress });

        for (let tokenId = 1; tokenId <= tokenIdCounter; tokenId++) {
            // If the sender is not the NFT owner, don't show it up
            if ((await nftContract.methods.ownerOf(tokenId).call()).toLowerCase() != fromAddress) continue;

            const tokenURI = await nftContract.methods.tokenURI(tokenId).call();
            
            const token = {
                "id": tokenId,
                "url": tokenURI
            } 
            tokens.push(token);
        }
        printSuccessfulMessage("Successful lend and loan created");

        return tokens;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

/**
 * Gets all the loan requests that have no lender yet
 */
async function getLoanRequests() {
    try {
        const fromAddress = await getConnectedAccount();
        const loanIdCounter = await defiContract.methods.loanIdCounter().call({ from: fromAddress });
        const requests = []
        for (let loanId = 1; loanId <= loanIdCounter; loanId++) {
            const loan = await defiContract.methods.loans(loanId).call({ from: fromAddress });
            if (loan.borrower.toLowerCase() !== fromAddress.toLowerCase()
                || loan.lender !== "0x0000000000000000000000000000000000000000") continue;
            
            const tokenURI = await nftContract.methods.tokenURI(loan.nftId).call();

            loan.id = loanId;
            loan.uri = tokenURI;
            loan.amountEth = web3.utils.fromWei(loan.amountEth, 'ether').toString();
            requests.push(loan);
        }
        return requests;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

window.connectMetaMask = connectMetaMask;
window.buyDex = buyDex;
window.sellDex = sellDex;
window.getDex = getDex;
window.loan = loan;
window.returnLoan = returnLoan;
window.getEthTotalBalance = getEthTotalBalance;
window.setRateEthToDex = setRateEthToDex;
window.makeLoanRequestByNft = makeLoanRequestByNft;
window.cancelLoanRequestByNft = cancelLoanRequestByNft;
window.loanByNft = loanByNft;
window.checkLoan = checkLoan;
window.getRateEthToDex = getRateEthToDex;
window.getAvailableNfts = getAvailableNfts;
window.listenToLoanCreation = listenToLoanCreation;
window.getTotalBorrowedAndNotPaidBackEth = getTotalBorrowedAndNotPaidBackEth;
window.checkLoanStatus = checkLoanStatus;
window.getAllTokenURIs = getAllTokenURIs;
window.getLoanRequests = getLoanRequests;
window.getConnectedAccount = getConnectedAccount;
window.isOwner = isOwner;


/**
 * Verifies if the error message comes from the require function, if so prints only its message
 * otherwise, prints the raw returned error message
 * @param {@string} error Error message captured in the catch block
 */
function verifyAndPrintErrorMessage(error) {
    const endIndex = error.message.search('{')
    let errorMessage = error.message;
    if (endIndex >= 0) {
        errorMessage = error.message.substring(0, endIndex);
    }
    errorMessage = errorMessage.replace(/^execution reverted: /i, "");
    console.error(errorMessage);

    // Create and display the error message element
    const errorMessageElement = document.createElement('div');
    errorMessageElement.classList.add('error-message');
    errorMessageElement.innerText = errorMessage;
    
    document.body.appendChild(errorMessageElement);
    setTimeout(function() {
        errorMessageElement.remove();
    }, 3000);
}

function printSuccessfulMessage(message) {
    // Create and display the error message element
    const messageElement = document.createElement('div');
    messageElement.classList.add('successful-message');
    messageElement.innerText = message;
    
    document.body.appendChild(messageElement);
    setTimeout(function() {
        messageElement.remove();
    }, 3000);
}

async function isOwner() {
    try {
        const fromAddress = (await window.ethereum.request({ method: "eth_accounts", }))[0];
        const ownerAddress = await defiContract.methods.owner().call();
        return fromAddress.toLowerCase() === ownerAddress.toLowerCase();
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}

async function getConnectedAccount() {
    try {
        const fromAddress = (await window.ethereum.request({ method: "eth_accounts", }))[0];
        return fromAddress;
    } catch (error) {
        verifyAndPrintErrorMessage(error);
    }
}