package dti;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import dti.dto.DTICryptoRequest;
import dti.dto.DTICryptoRequestType;
import dti.dto.DTICryptoResponse;
import dti.models.NFT;
import dti.models.NFTRequest;
import dti.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DTICryptoServer extends DefaultSingleRecoverable {
    private final ServiceReplica replica;
    private final Logger logger = LoggerFactory.getLogger("bftsmart");

    private long NFTIDCounter = 0;
    private long coinIDCounter = 0;
    /**
     * NFT name -> NFT object
     */
    private HashMap<String, NFT> existingNFT = new HashMap<>();
    /**
     * Client id -> NFTs it owns
     */
    private HashMap<Integer, ArrayList<NFT>> NFTOwners = new HashMap<>();
    /**
     * Client id -> Coins it owns (coin id -> coin value)
     */
    private HashMap<Integer, HashMap<Long, Float>> coinOwners = new HashMap<>();
    /**
     * NFT name -> Requests it has (client id -> request made)
     */
    private HashMap<String, HashMap<Integer, NFTRequest>> NFTRequests = new HashMap<>();

    // The constructor passes the id of the server to the super class
    public DTICryptoServer(int id) {
        replica = new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java DTICryptoServer <server id 0-3>");
            System.exit(-1);
        }
        new DTICryptoServer(Integer.parseInt(args[0]));
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            DTICryptoResponse response = new DTICryptoResponse();
            DTICryptoRequest request = DTICryptoRequest.fromBytes(command);
            DTICryptoRequestType cmd = request.getParams().getType();

            HashMap<Integer, NFTRequest> reqs;
            HashMap<Long, Float> coins;
            String name, uri, nftName;
            ArrayList<NFT> nfts;
            long validity;
            float value;
            int userId;
            NFT nft;

            logger.info("Ordered execution of a {} request from {}", cmd, msgCtx.getSender());

            switch (cmd) {
                case MY_COINS:
                    if (!coinOwners.containsKey(request.getParams().getUserId())) {
                        System.out.println("* Created wallet for user " + request.getParams().getUserId());
                        coinOwners.put(request.getParams().getUserId(), new HashMap<>());
                    }
                    coins = coinOwners.get(request.getParams().getUserId());

                    ArrayList<Pair<Long, Float>> listCoins = new ArrayList<>();
                    coins.forEach((a, b) -> listCoins.add(new Pair<>(a, b)));
                    response.setCoins(listCoins);
                    return DTICryptoResponse.toBytes(response);
                case MINT_COIN:
                    userId = request.getParams().getUserId();
                    coins = coinOwners.get(userId);
                    if (coins == null) coins = new HashMap<>();
                    coins.put(++coinIDCounter, request.getParams().getValue());
                    coinOwners.put(userId, coins);

                    response.setId(coinIDCounter);
                    return DTICryptoResponse.toBytes(response);
                case SPEND:
                    int sender = request.getParams().getUserId();
                    value = request.getParams().getValue();
                    int receiver = request.getParams().getReceiver();
                    ArrayList<Long> coinsIds = request.getParams().getCoinsIds();

                    if (coinOwners.containsKey(sender) && coinOwners.containsKey(receiver)) {
                        float sum = 0;
                        HashMap<Long, Float> wallet = new HashMap<>(coinOwners.get(sender));

                        for (Long coinId : coinsIds) {
                            if (wallet.containsKey(coinId)) {
                                sum += wallet.get(coinId);
                                wallet.remove(coinId);
                            }
                        }

                        if (sum >= value) {
                            coinOwners.get(receiver).put(++coinIDCounter, value);
                            if (sum - value == 0) {
                                coinOwners.put(sender, wallet);
                            }

                            if (sum - value > 0) {
                                wallet.put(++coinIDCounter, sum - value);
                                coinOwners.put(sender, wallet);
                                response.setId(coinIDCounter);
                            }
                        }
                    }
                    return DTICryptoResponse.toBytes(response);

                case MY_NFTS:
                    userId = request.getParams().getUserId();
                    nfts = NFTOwners.get(userId);
                    if (nfts == null) {
                        nfts = new ArrayList<>();
                        NFTOwners.put(userId, nfts);
                    }
                    response.setNfts(nfts);
                    return DTICryptoResponse.toBytes(response);
                case CHECK_EXISTING_NFTS:
                    userId = request.getParams().getUserId();
                    nfts = NFTOwners.get(userId);
                    if (nfts == null) {
                        nfts = new ArrayList<>();
                        NFTOwners.put(userId, nfts);
                    }

                    ArrayList<NFT> existingNFTs = new ArrayList<>(existingNFT.values());
                    existingNFTs.removeAll(nfts);
                    response.setNfts(existingNFTs);
                    return DTICryptoResponse.toBytes(response);
                case MINT_NFT:
                    userId = request.getParams().getUserId();
                    name = request.getParams().getName();
                    uri = request.getParams().getURI();
                    if (existingNFT.containsKey(name)) return DTICryptoResponse.toBytes(response);

                    nft = new NFT(++NFTIDCounter, userId, name, uri);
                    existingNFT.put(name, nft);
                    nfts = NFTOwners.get(userId);
                    if (nfts == null) nfts = new ArrayList<>();
                    nfts.add(nft);
                    NFTOwners.put(userId, nfts);
                    NFTRequests.put(name, new HashMap<>());

                    response.setId(NFTIDCounter);
                    return DTICryptoResponse.toBytes(response);

                case REQUEST_NFT:
                    userId = request.getParams().getUserId();
                    nftName = request.getParams().getName();
                    value = request.getParams().getValue();
                    validity = request.getParams().getValidity();
                    nft = existingNFT.get(nftName);
                    ArrayList<Long> coinIds = request.getParams().getCoinsIds();

                    if (nft != null && nft.getOwner() != userId
                            && !NFTRequests.get(nftName).containsKey(userId)
                            && coinsSum(userId, coinIds) >= value
                    ) {
                        NFTRequests.get(nftName).put(
                                userId,
                                new NFTRequest(userId, nftName, value, coinIds, validity)
                        );
                        nft.incRequests();
                        response.setId(nft.getId());
                    }
                    return DTICryptoResponse.toBytes(response);

                case CANCEL_REQUEST:
                    userId = request.getParams().getUserId();
                    nftName = request.getParams().getName();
                    reqs = NFTRequests.get(nftName);
                    if (reqs == null || !reqs.containsKey(userId)) {
                        return DTICryptoResponse.toBytes(response);
                    }
                    existingNFT.get(nftName).decRequests();
                    reqs.remove(userId);
                    response.setId(1);
                    return DTICryptoResponse.toBytes(response);

                case CURRENT_NFT_REQUESTS:
                    userId = request.getParams().getUserId();
                    String nftRequested = request.getParams().getName();
                    if (NFTOwners.containsKey(userId) && existingNFT.containsKey(nftRequested)) {
                        if (NFTOwners.get(userId).contains(existingNFT.get(nftRequested))) {
                            // if the user is the owner of the NFT
                            response.setNftRequests(new ArrayList<>(NFTRequests.get(nftRequested).values()));
                        }
                    }
                    return DTICryptoResponse.toBytes(response);

                case PROCESS_NFT_TRANSFER:
                    userId = request.getParams().getUserId();
                    nftName = request.getParams().getName();
                    int buyer = request.getParams().getBuyer();
                    boolean accept = request.getParams().getAccept();

                    if (!NFTRequests.containsKey(nftName) || !NFTRequests.get(nftName).containsKey(buyer)) {
                        response.setId(-1);
                        return DTICryptoResponse.toBytes(response);
                    }

                    NFTRequest req = NFTRequests.get(nftName).get(buyer);
                    if (!accept || req.getValidity() < msgCtx.getTimestamp()) {
                        NFTRequests.get(nftName).remove(buyer);
                        return DTICryptoResponse.toBytes(response);
                    }


                    HashMap<Long, Float> buyerCoins = new HashMap<>(coinOwners.get(buyer));
                    float sum = 0;
                    for (Long coinId : req.getWallet()) {
                        if (buyerCoins.get(coinId) != null) {
                            sum += buyerCoins.get(coinId);
                            buyerCoins.remove(coinId);
                        }
                    }
                    if (sum < req.getOfferedValue()) {
                        response.setId(-2);
                        return DTICryptoResponse.toBytes(response);
                    }
                    if (sum - req.getOfferedValue() > 0) {
                        buyerCoins.put(++coinIDCounter, sum - req.getOfferedValue());
                    }
                    HashMap<Long, Float> issuerCoins = coinOwners.get(userId);
                    issuerCoins.put(++coinIDCounter, req.getOfferedValue());
                    coinOwners.put(userId, issuerCoins);
                    coinOwners.put(buyer, buyerCoins);

                    // Change NFT owner
                    nfts = NFTOwners.get(userId);
                    nft = existingNFT.get(req.getRequestedNFT());
                    nfts.remove(nft);

                    nft.setOwner(buyer);
                    nft.decRequests();
                    nfts = NFTOwners.get(buyer);
                    nfts.add(nft);
                    NFTOwners.put(buyer, nfts);

                    NFTRequests.put(nftName, new HashMap<>());

                    response.setId(coinIDCounter);
                    return DTICryptoResponse.toBytes(response);

                case CHECK_MY_NFT_REQUESTS:
                    userId = request.getParams().getUserId();

                    ArrayList<HashMap<Integer, NFTRequest>> allRequests = new ArrayList<>(NFTRequests.values());
                    ArrayList<NFTRequest> userRequests = new ArrayList<>();

                    for (HashMap<Integer, NFTRequest> currRequest : allRequests) {
                        if (currRequest.containsKey(userId)) {
                            userRequests.add(currRequest.get(userId));
                        }
                    }
                    response.setNftRequests(new ArrayList<>(userRequests));
                    return DTICryptoResponse.toBytes(response);
            }
            return null;
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("Failed to process ordered request", ex);
            return new byte[0];
        }
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        try {
            DTICryptoResponse response = new DTICryptoResponse();
            DTICryptoRequest request = DTICryptoRequest.fromBytes(command);
            DTICryptoRequestType cmd = request.getParams().getType();

            HashMap<Long, Float> coins;
            ArrayList<NFT> nfts;
            int userId;

            logger.info("Unordered execution of a {} request from {}", cmd, msgCtx.getSender());

            switch (cmd) {
                case MY_COINS:
                    if (coinOwners.get(request.getParams().getUserId()) == null) {
                        System.out.println("* Created wallet for user " + request.getParams().getUserId());
                        coinOwners.put(request.getParams().getUserId(), new HashMap<>());
                    }
                    coins = coinOwners.get(request.getParams().getUserId());

                    ArrayList<Pair<Long, Float>> listCoins = new ArrayList<>();
                    coins.forEach((a, b) -> listCoins.add(new Pair<>(a, b)));
                    response.setCoins(listCoins);
                    return DTICryptoResponse.toBytes(response);

                case MY_NFTS:
                    userId = request.getParams().getUserId();
                    nfts = NFTOwners.get(userId);
                    if (nfts == null) {
                        nfts = new ArrayList<>();
                        NFTOwners.put(userId, nfts);
                    }
                    response.setNfts(nfts);
                    return DTICryptoResponse.toBytes(response);

                case CHECK_EXISTING_NFTS:
                    userId = request.getParams().getUserId();
                    nfts = NFTOwners.get(userId);
                    if (nfts == null) {
                        nfts = new ArrayList<>();
                        NFTOwners.put(userId, nfts);
                    }

                    ArrayList<NFT> existingNFTs = new ArrayList<>(existingNFT.values());
                    existingNFTs.removeAll(nfts);
                    response.setNfts(existingNFTs);
                    return DTICryptoResponse.toBytes(response);

                case CURRENT_NFT_REQUESTS:
                    userId = request.getParams().getUserId();
                    String nftRequested = request.getParams().getName();
                    if (NFTOwners.containsKey(userId) && existingNFT.containsKey(nftRequested)) {
                        if (NFTOwners.get(userId).contains(existingNFT.get(nftRequested))) {
                            // if the user is the owner of the NFT
                            response.setNftRequests(new ArrayList<>(NFTRequests.get(nftRequested).values()));
                        }
                    }
                    return DTICryptoResponse.toBytes(response);

                case CHECK_MY_NFT_REQUESTS:
                    userId = request.getParams().getUserId();

                    ArrayList<HashMap<Integer, NFTRequest>> allRequests = new ArrayList<>(NFTRequests.values());
                    ArrayList<NFTRequest> userRequests = new ArrayList<>();

                    for (HashMap<Integer, NFTRequest> currRequest : allRequests) {
                        if (currRequest.containsKey(userId)) {
                            userRequests.add(currRequest.get(userId));
                        }
                    }
                    response.setNftRequests(new ArrayList<>(userRequests));
                    return DTICryptoResponse.toBytes(response);
            }
        } catch (IOException |
                 ClassNotFoundException ex) {
            logger.error("Failed to process unordered request", ex);
            return new byte[0];
        }
        return null;
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(NFTIDCounter);
            out.writeObject(coinIDCounter);
            out.writeObject(existingNFT);
            out.writeObject(NFTOwners);
            out.writeObject(coinOwners);
            out.writeObject(NFTRequests);
            out.flush();
            bos.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace(); //debug instruction
            return new byte[0];
        }
    }

    @Override
    public void installSnapshot(byte[] state) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(state);
             ObjectInput in = new ObjectInputStream(bis)) {
            NFTIDCounter = (Long) in.readObject();
            coinIDCounter = (Long) in.readObject();
            existingNFT = (HashMap<String, NFT>) in.readObject();
            NFTOwners = (HashMap<Integer, ArrayList<NFT>>) in.readObject();
            coinOwners = (HashMap<Integer, HashMap<Long, Float>>) in.readObject();
            NFTRequests = (HashMap<String, HashMap<Integer, NFTRequest>>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace(); //debug instruction
        }
    }

    private float coinsSum(int userId, ArrayList<Long> coins) {
        HashMap<Long, Float> buyerCoins = new HashMap<>(coinOwners.get(userId));
        float sum = 0;
        for (Long coinId : coins) {
            if (buyerCoins.get(coinId) != null) {
                sum += buyerCoins.get(coinId);
            }
        }
        return sum;
    }
}