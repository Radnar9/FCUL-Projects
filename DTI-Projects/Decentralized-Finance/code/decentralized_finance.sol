// SPDX-License-Identifier: MIT
pragma solidity 0.8.18;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

contract DecentralizedFinance is ERC20 {
    using Counters for Counters.Counter;

    address public owner;
    uint256 private maxLoanDuration;
    uint256 public rateEthToDex;
    uint256 private ethTotalBalance;
    uint256 private loanDeduction;  // Percentage of the deduction per day
    uint256 public lockedDex;       // DEX obtained through loan creations that can't be spent until payment or deadline

    struct Loan {
        uint256 deadline;
        uint256 amountEth;
        address lender;
        address borrower;
        bool isBasedNft;
        IERC721 nftContract;
        uint256 nftId;
    }

    Counters.Counter public loanIdCounter;
    mapping(uint256 => Loan) public loans;        // loanId -> Loan
    mapping(uint256 => uint256) public nftLoans;  // nftId -> loanId
    
    event LoanCreated(address indexed borrower, uint256 ethAmount, uint256 deadline);

    constructor() ERC20("DEX", "DEX") {
        rateEthToDex = 1 ether / (10 ** 3);                               // Default: 1 eth = 1000 dex || 1 dex = 0.001 eth
        _mint(address(this), (10 ** 30) * rateEthToDex);                // Saves the total suply in Wei
        maxLoanDuration = 7 days;
        loanDeduction = 5;                                              // 5% per day
        owner = msg.sender;
    }

    modifier isOwner {
        require(msg.sender == owner, "DeFi: Only the owner of the contract is allowed");
        _;
    }

    modifier positiveValue {
        require(msg.value > 0, "DeFi: The value is lower or equal to zero");
        _;
    }

    modifier enoughDexToEthAmount(uint256 dexAmount) {
        require(balanceOf(msg.sender) >= dexAmount, "DeFi: Not enough DEX balance");
        require(address(this).balance >= dexAmount, "DeFi: Not enough ETH balance");
        _;
    }

    modifier hasDexSupply() {
        require(balanceOf(address(this)) > 0 &&  msg.value <= balanceOf(address(this)) - lockedDex, "DeFi: There is no DEX balance available");
        _;
    }

    modifier validDeadline(uint256 deadline) {
        require(deadline - block.timestamp <= maxLoanDuration, "DeFi: The deadline is higher than the allowed duration");
        _;
    }

    modifier positiveAmount(uint256 amount) {
        require(amount > 0, "DeFi: The amount is lower or equal to zero");
        _;
    }

    modifier hasDexAmount(uint256 dexAmount) {
        require(balanceOf(msg.sender) >= dexAmount, "DeFi: The sender's balance is lower than the DEX amount inserted");
        _;
    }

    modifier nonExistentNftRequest(uint256 nftId) {
        require(nftLoans[nftId] == 0, "DeFi: A request with the provided NFT already exists");
        _;
    }

    modifier existsNftRequest(uint256 nftId) {
        require(nftLoans[nftId] != 0, "DeFi: A request with the provided NFT does not exist");
        _;
    }

    modifier isNftRequestBorrower(uint256 nftId) {
        require(loans[nftLoans[nftId]].borrower == msg.sender, "DeFi: The sender is not the borrower of the NFT request");
        _;
    }

    modifier isNotLend(uint256 nftId) {
        require(loans[nftLoans[nftId]].lender == address(0), "DeFi: The NFT is already lend");
        _;
    }

    modifier existsLoan(uint256 loanId) {
        require(loans[loanId].deadline > 0, "DeFi: The loan does not exist");
        _;
    }

    modifier ownsLoan(uint256 loanId) {
        require(loans[loanId].deadline > 0, "DeFi: The loan does not exist");
        require(loans[loanId].borrower == msg.sender, "DeFi: The sender does not own the loan");
        _;
    }

    modifier isContractApproved(IERC721 nftContract, uint256 nftId) {
        require(
            IERC721(nftContract).getApproved(nftId) == address(this) || IERC721(nftContract).isApprovedForAll(msg.sender, address(this)), 
            "DeFi: The contract address needs to be approved to be able to change the ownership of NFT"
        );
        _;
    }

    modifier isNFTOwner(IERC721 nftContract, uint256 nftId) {
        require(IERC721(nftContract).ownerOf(nftId) == msg.sender, "DeFi: The sender is not the owner of the NFT");
        _;
    }

    modifier senderIsDifferentThanBorrower(uint256 nftId) {
        require(loans[nftLoans[nftId]].borrower != msg.sender, "DeFi: The sender is the borrower of the loan");
        _;
    }

    function buyDex() external payable positiveValue hasDexSupply {
        uint256 dexToBuy = msg.value;
        uint256 availableDex = balanceOf(address(this));

        if (dexToBuy <= availableDex) {
            _transfer(address(this), msg.sender, dexToBuy);
            return;
        } else if (availableDex > 0) {
            _transfer(address(this), msg.sender, availableDex);
            payable(msg.sender).transfer(dexToBuy - availableDex);
        }
    }

    // Receives the DEX amount in Wei
    function sellDex(uint256 dexAmount) 
        external
        positiveAmount(dexAmount)
        enoughDexToEthAmount(dexAmount)
    {
        _transfer(msg.sender, address(this), dexAmount);
        payable(msg.sender).transfer(dexAmount);
    }

    // Obtain the deadline in days, calculate the initial value by deducting 5% per day based on the deadline
    // Then, creates the desired Loan
    function loan(uint256 dexAmount, uint256 _deadline)
        external 
        positiveAmount(dexAmount) 
        validDeadline(_deadline) 
        hasDexAmount(dexAmount)
    {
        uint256 deadlineDays = (_deadline - block.timestamp) / (60 * 60 * 24); 
        uint256 initialValue = (dexAmount * (100 - deadlineDays * loanDeduction)) / 100; // +5%/day with a maximum of 7*5%/day
        initialValue = initialValue / 2;

        // Deduct the DEX amount from the user account and lock it in the variable
        _transfer(msg.sender, address(this), dexAmount);
        lockedDex += initialValue * 2;

        // Send half of the calculated initial value to the user's account
        payable(msg.sender).transfer(initialValue);

        loanIdCounter.increment();
        Loan memory createdLoan = Loan({
            deadline: _deadline,
            amountEth: initialValue,
            lender: address(this),
            borrower: msg.sender,
            isBasedNft: false,
            nftContract: IERC721(address(0)),
            nftId: 0
        });

        loans[loanIdCounter.current()] = createdLoan;
        emit LoanCreated(msg.sender, initialValue, _deadline);
    }


    function returnLoan(uint256 loanId) external payable positiveValue ownsLoan(loanId) {
        Loan memory currentLoan = loans[loanId];
        if (currentLoan.deadline < block.timestamp) {
            // In case the deadline expired, send back the received ETH
            payable(msg.sender).transfer(msg.value);

            punishBorrower(loanId);
            return;
        }

        uint256 remainder = msg.value > currentLoan.amountEth ? msg.value - currentLoan.amountEth : 0;
        if (currentLoan.isBasedNft) {
            require(currentLoan.amountEth <= msg.value, "DeFi: Partial repayments on NFT-based loans are not allowed");

            lockedDex -= currentLoan.amountEth;
            _transfer(address(this), currentLoan.lender, currentLoan.amountEth);
            IERC721(currentLoan.nftContract).transferFrom(address(this), currentLoan.borrower, currentLoan.nftId);
            if (remainder != 0) payable(msg.sender).transfer(remainder);

            delete loans[loanId];
            delete nftLoans[currentLoan.nftId];
            return;
        }

        lockedDex -= currentLoan.amountEth * 2;
        uint256 dexToGiveBack = msg.value >= currentLoan.amountEth ? currentLoan.amountEth * 2 : msg.value * 2;
        _transfer(address(this), msg.sender, dexToGiveBack);

        if (remainder != 0) payable(msg.sender).transfer(remainder);
        delete loans[loanId];
    }

    function getEthTotalBalance() public view returns (uint256) {
        return address(this).balance;
    }

    function setRateEthToDex(uint256 rate) external isOwner {
        rateEthToDex = 1 ether / (10 ** rate);
    }

    function getDex() public view returns (uint256) {
        return balanceOf(msg.sender);
    }

    function makeLoanRequestByNft(IERC721 nftContract, uint256 nftId, uint256 loanAmount, uint256 deadline)
        external
        positiveAmount(loanAmount)
        validDeadline(deadline)
        isNFTOwner(nftContract, nftId)
        isContractApproved(nftContract, nftId)
        nonExistentNftRequest(nftId)
    {
        Loan memory nftLoan = Loan({
            deadline: deadline,
            amountEth: loanAmount,
            lender: address(0),
            borrower: msg.sender,
            isBasedNft: true,
            nftContract: nftContract,
            nftId: nftId
        });

        IERC721(nftContract).transferFrom(msg.sender, address(this), nftId);

        loanIdCounter.increment();
        loans[loanIdCounter.current()] = nftLoan;
        nftLoans[nftId] = loanIdCounter.current();
    }


    function cancelLoanRequestByNft(uint256 nftId)
        external
        existsNftRequest(nftId)
        isNftRequestBorrower(nftId)
        isNotLend(nftId)
    {
        uint256 loanId = nftLoans[nftId];
        Loan memory currentLoan = loans[loanId];
        IERC721(currentLoan.nftContract).transferFrom(address(this), msg.sender, nftId);
        
        delete loans[loanId];
        delete nftLoans[nftId];
    }


    function loanByNft(uint256 nftId)
        external
        existsNftRequest(nftId)
        hasDexAmount(loans[nftLoans[nftId]].amountEth)
        senderIsDifferentThanBorrower(nftId)
        isNotLend(nftId)
    {   
        uint256 loanId = nftLoans[nftId];
        Loan memory currentLoan = loans[loanId];

        if (currentLoan.deadline < block.timestamp) { 
            punishBorrower(loanId);
            return;
        }
        
        currentLoan.lender = msg.sender;
        loans[loanIdCounter.current()] = currentLoan;
        
        lockedDex += currentLoan.amountEth;
        _transfer(msg.sender, address(this), currentLoan.amountEth);
        payable(currentLoan.borrower).transfer(currentLoan.amountEth);

        emit LoanCreated(currentLoan.borrower, currentLoan.amountEth, currentLoan.deadline);
    }

    function checkLoan(uint256 loanId) public isOwner existsLoan(loanId) {
        Loan memory currentLoan = loans[loanId];
        if (currentLoan.deadline >= block.timestamp) return;
            
        punishBorrower(loanId);
    }
 
    // Function responsible for verify all the existent loan status, checking if they already expired 
    function checkLoanStatus() external isOwner {
        for (uint256 loanId = 1; loanId <= loanIdCounter.current(); loanId++) {
            checkLoan(loanId);   
        }
    }
    
    function punishBorrower(uint256 loanId) internal {
        Loan memory currentLoan = loans[loanId];
        if (currentLoan.isBasedNft && currentLoan.lender != address(0)) {
            IERC721(currentLoan.nftContract).transferFrom(address(this), currentLoan.lender, currentLoan.nftId);
            lockedDex -= currentLoan.amountEth;
            delete nftLoans[currentLoan.nftId];
        } else if (currentLoan.isBasedNft && currentLoan.lender == address(0)) {
            IERC721(currentLoan.nftContract).transferFrom(address(this), currentLoan.borrower, currentLoan.nftId);
            delete nftLoans[currentLoan.nftId];
        } else {
            lockedDex -= currentLoan.amountEth * 2;
        }
        delete loans[loanId];
    }
}