package dti;

import dti.models.NFT;
import dti.models.NFTRequest;
import dti.output.OPERATIONS;
import dti.output.Output;
import dti.utils.Pair;

import java.io.Console;
import java.sql.Timestamp;
import java.util.*;

public class InteractiveClient {
    private static boolean exit = false;
    private static DTICrypto crypto;
    private static int clientId;

    private static final Console console = System.console();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: InteractiveClient <client id>");
        }

        clientId = Integer.parseInt(args[0]);
        crypto = new DTICrypto(clientId);
        crypto.myCoins();
        crypto.myNFTs();
        while (!exit) {
            try {
                int mode = modeSelector();
                switch (mode) {
                    case 0:
                        exit = true;
                        crypto.close();
                        break;
                    case 1:
                        coinOperationSelector();
                        break;
                    case 2:
                        nftOperationSelector();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("-> An error occurred, please verify if you're providing the input as expected.\n");
            }
        }
    }

    private static int modeSelector() {
        System.out.println("Select a mode:");
        System.out.println("1 - Manage coins");
        System.out.println("2 - Manage NFTs");
        System.out.println("0 - Terminate this client");

        int mode = Integer.parseInt(console.readLine("> "));
        System.out.println();
        return mode;
    }

    public static void coinOperationSelector() {
        System.out.println("Select a coin operation:");
        System.out.println("1 - My coins");
        System.out.println("2 - Mint");
        System.out.println("3 - Spend");
        System.out.println("9 - Go back");
        System.out.println("0 - Terminate this client");
        String value, receiver, result;

        int cmd = Integer.parseInt(console.readLine("> "));
        System.out.println();
        switch (cmd) {
            case 0:
                exit = true;
                crypto.close();
                break;
            case 1:
                System.out.println("\t----| MY COINS |----\n");
                System.out.println("* Getting the ids and values of your coins...");

                ArrayList<Pair<Long, Float>> res = crypto.myCoins();
                if (res.isEmpty()) {
                    System.out.println("\t- No coins found :(\n");
                    break;
                }
                Output.printValues(res, OPERATIONS.MY_COINS);
                System.out.println();
                break;
            case 2:
                System.out.println("\t----| MINT |----\n");
                if (clientId != 4) {
                    System.err.println("\t*-* You are not allowed to perform the mint operation!");
                    break;
                }
                value = console.readLine("Insert the value of the coin to create: ");
                long createdCoinId = crypto.mint(Float.valueOf(value));
                System.out.println("\t- Created coin with id '" + createdCoinId + "' and value '" + value + "'\n");
                break;
            case 3:
                System.out.println("\t----| SPEND |----\n");
                String coinsInput = console.readLine("Insert the ids of the coins to use separated by ',': ");
                receiver = console.readLine("Insert the id of the receiver: ");
                value = console.readLine("Insert the value to be transferred: ");

                ArrayList<Long> coinsId = convertStringToLongArray(coinsInput);

                long createdCoin = crypto.spend(coinsId, Integer.parseInt(receiver), Float.valueOf(value));
                System.out.println("\t- Successful transfer for client '" + receiver + "'");
                if (createdCoin > 0) System.out.println("\t    -> Coin created with id '" + createdCoin + "'\n");
                break;
            default:
                break;
        }
    }

    public static void nftOperationSelector() {
        System.out.println("Select a NFT operation:");
        System.out.println("1 - My NFTs");
        System.out.println("2 - Check existing NFTs");
        System.out.println("3 - Mint NFT");
        System.out.println("4 - Request NFT transfer");
        System.out.println("5 - Cancel request NFT transfer");
        System.out.println("6 - Requests over my NFTs");
        System.out.println("7 - Process NFT transfer");
        System.out.println("8 - Check my NFT requests");
        System.out.println("9 - Go back");
        System.out.println("0 - Terminate this client");
        String name, uri;

        int cmd = Integer.parseInt(console.readLine("> "));
        System.out.println();

        switch (cmd) {
            case 0:
                exit = true;
                crypto.close();
                break;
            case 1:
                System.out.println("\t----| MY NFTs |----\n");
                System.out.println("* Getting the id, name and URI of your NFTs...");
                ArrayList<NFT> res = crypto.myNFTs();
                if (res.isEmpty()) {
                    System.out.println("\t- No NFTs found :(\n");
                    break;
                }

                Output.printValues(res, OPERATIONS.MY_NFT);
                System.out.println();
                break;
            case 2:
                System.out.println("\t----| EXISTING NFTs |----\n");
                System.out.println("* Getting all existing NFTs except the ones you own...");
                ArrayList<NFT> allNFTs = crypto.existingNFTs();
                if (allNFTs.isEmpty()) {
                    System.out.println("\t- No NFTs found :(\n");
                    break;
                }

                Output.printValues(allNFTs, OPERATIONS.EXISTING_NFT);
                System.out.println();
                break;
            case 3:
                System.out.println("\t----| MINT NFT |----\n");
                name = console.readLine("Insert a name for the NFT: ");
                uri = console.readLine("Insert the URI for the NFT: ");
                long createdNftId = crypto.mintNFT(name, uri);
                if (createdNftId == 0) {
                    System.out.println("\t- The name inserted for the NFT already exists\n");
                    break;
                }
                System.out.format("\t- Created NFT with id '%s', name '%s' and URI '%s'\n\n", createdNftId, name, uri);
                break;
            case 4:
                System.out.println("\t----| REQUEST NFT TRANSFER |----\n");
                String nameNFT = console.readLine("Insert the name of the NFT: ");
                String coinsInput = console.readLine("Insert the ids of the coins to use separated by ',': ");
                String value = console.readLine("Insert the offered value: ");
                String validityStr = console.readLine("Insert the confirmation validity in minutes: ");

                ArrayList<Long> coinsId = convertStringToLongArray(coinsInput);
                long validity = calculateValidity(Long.parseLong(validityStr));
                float offeredValue = Float.parseFloat(value);
                if(crypto.requestNFT(nameNFT, offeredValue, coinsId, validity) == 0) {
                    System.out.println("\t- It was not possible to create the desired request, verify if you have enough coins\n");
                } else {
                    System.out.format("\t- Request created for the NFT '%s', for '%f' and expires on '%s'\n\n",
                            nameNFT, offeredValue, printValidity(validity));
                }
                break;
            case 5:
                System.out.println("\t----| CANCEL REQUEST NFT TRANSFER |----\n");
                String nameNFTRequest = console.readLine("Insert the name of the NFT: ");
                if(crypto.cancelRequestNFT(nameNFTRequest) == 1) {
                    System.out.println("\t- Request cancelled for the NFT '" + nameNFTRequest + "'\n");
                } else {
                    System.out.println("\t- You don't have any pending requests for the provided NFT\n");
                }
                break;
            case 6:
                System.out.println("\t----| REQUESTS OVER MY NFTS |----\n");
                String NFTname = console.readLine("Insert the name of the NFT: ");

                ArrayList<NFTRequest> response = crypto.myNFTRequests(NFTname);
                if (response == null) {
                    System.out.println("\t- You don't own a NFT with the inserted name\n");
                    break;
                }
                if (response.isEmpty()) {
                    System.out.println("\t- No requests found :(\n");
                    break;
                }

                Output.printValues(response, OPERATIONS.REQUESTS_OVER_MY_NFT);
                System.out.println();
                break;
            case 7:
                System.out.println("\t----| PROCESS NFT TRANSFER |----\n");
                String nftName = console.readLine("Insert the name of the NFT: ");
                String buyer = console.readLine("Insert the buyer id: ");
                String accept = console.readLine("Accept transfer? (y/n): ");
                long createdCoinId = crypto.processNFTTransfer(nftName, Integer.parseInt(buyer), Objects.equals(accept, "y"));
                if (createdCoinId == -1) {
                    System.out.println("\t- Doesn't exist a request with the inserted values\n");
                    break;
                } else if (createdCoinId == -2) {
                    System.out.println("\t- The buyer doesn't have enough coins to conclude the transfer\n");
                    break;
                } else if (createdCoinId == 0) {
                    System.out.println("\t- Transfer refused or the validity expired\n");
                    break;
                }
                System.out.println("\t- Successful transfer of NFT '" + nftName + "'");
                System.out.println("\t    -> Coin created with id '" + createdCoinId + "'\n");
                break;
            case 8:
                System.out.println("\t----| CHECK MY NFT REQUESTS |----\n");

                ArrayList<NFTRequest> myRequests = crypto.myRequests();
                if (myRequests == null) {
                    System.out.println("\t- You don't have any ongoing requests\n");
                    break;
                }
                if (myRequests.isEmpty()) {
                    System.out.println("\t- No requests found :(\n");
                    break;
                }
                Output.printValues(myRequests, OPERATIONS.MY_NFT_REQUESTS);
                System.out.println();
                break;
            default:
                break;
        }
    }

    private static ArrayList<Long> convertStringToLongArray(String coinsInput) {
        String[] coinsArray = coinsInput.split(",");
        ArrayList<Long> coinsId = new ArrayList<>();
        new ArrayList<Long>();
        for (String s : coinsArray) {
            coinsId.add(Long.valueOf(s));
        }
        return coinsId;
    }

    private static long calculateValidity(long minutes) {
        return System.currentTimeMillis() + (minutes * 60 * 1000);
    }

    private static String printValidity(long validity) {
        return new Timestamp(validity).toString();
    }
}
