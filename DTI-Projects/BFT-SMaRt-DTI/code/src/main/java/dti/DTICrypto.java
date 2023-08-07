package dti;

import bftsmart.tom.ServiceProxy;
import dti.dto.DTICryptoRequest;
import dti.dto.DTICryptoRequestParams;
import dti.dto.DTICryptoRequestType;
import dti.dto.DTICryptoResponse;
import dti.models.Coin;
import dti.models.NFT;
import dti.models.NFTRequest;
import dti.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class DTICrypto {

    private final int userId;
    private final ServiceProxy serviceProxy;
    private final Logger logger = LoggerFactory.getLogger("bftsmart");

    public DTICrypto(int id) {
        this.userId = id;
        serviceProxy = new ServiceProxy(id);
    }

    //  |-------| Coin Operations |-------|

    public ArrayList<Pair<Long, Float>> myCoins() {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.MY_COINS);
            params.setUserId(userId);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeUnordered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MY_COINS request");
            return null;
        }

        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getCoins();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of MY_COINS request");
            return null;
        }

    }

    public Long mint(Float value) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.MINT_COIN);
            params.setUserId(userId);
            params.setValue(value);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MINT_COIN request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of MINT_COIN request");
            return null;
        }
    }

    public Long spend(ArrayList<Long> coins, int receiver, Float value) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.SPEND);
            params.setUserId(userId);
            params.setCoins(coins);
            params.setValue(value);
            params.setReceiver(receiver);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send SPEND request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of SPEND request");
            return null;
        }
    }

    //  |-------| NFT Operations |-------|

    public ArrayList<NFT> myNFTs() {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.MY_NFTS);
            params.setUserId(userId);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeUnordered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MY_NFTS request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getNfts();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of MY_NFTS request");
            return null;
        }
    }


    public ArrayList<NFT> existingNFTs() {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.CHECK_EXISTING_NFTS);
            params.setUserId(userId);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeUnordered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MY_NFTS request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getNfts();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of MY_NFTS request");
            return null;
        }
    }

    public Long mintNFT(String name, String uri) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.MINT_NFT);
            params.setUserId(userId);
            params.setName(name);
            params.setURI(uri);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send MINT_NFT request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of MINT_NFT request");
            return null;
        }
    }

    public Long requestNFT(String nftName, float value, ArrayList<Long> coins, long validity) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.REQUEST_NFT);
            params.setUserId(userId);
            params.setValue(value);
            params.setName(nftName);
            params.setCoins(coins);
            params.setValidity(validity);

            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send REQUEST_NFT request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of REQUEST_NFT request");
            return null;
        }
    }

    public Long cancelRequestNFT(String nft) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.CANCEL_REQUEST);
            params.setName(nft);
            params.setUserId(userId);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send CANCEL_REQUEST request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of CANCEL_REQUEST request");
            return null;
        }
    }

    public ArrayList<NFTRequest> myNFTRequests(String nft) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.CURRENT_NFT_REQUESTS);
            params.setUserId(userId);
            params.setName(nft);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeUnordered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send CURRENT_NFT_REQUESTS request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getNftRequests();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of CURRENT_NFT_REQUESTS request");
            return null;
        }
    }


    public Long processNFTTransfer(String nft, int buyer, Boolean accept) {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.PROCESS_NFT_TRANSFER);
            params.setUserId(userId);
            params.setName(nft);
            params.setBuyer(buyer);
            params.setAccept(accept);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeOrdered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send PROCESS_NFT_TRANSFER request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getId();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of PROCESS_NFT_TRANSFER request");
            return null;
        }
    }

    public ArrayList<NFTRequest> myRequests() {
        byte[] rep;
        try {
            DTICryptoRequestParams params = new DTICryptoRequestParams();
            params.setType(DTICryptoRequestType.CHECK_MY_NFT_REQUESTS);
            params.setUserId(userId);
            DTICryptoRequest request = new DTICryptoRequest(params);

            // invokes BFT-SMaRt
            rep = serviceProxy.invokeUnordered(DTICryptoRequest.toBytes(request));
        } catch (IOException e) {
            logger.error("Failed to send PROCESS_NFT_TRANSFER request");
            return null;
        }
        if (rep.length == 0) {
            return null;
        }
        try {
            DTICryptoResponse response = DTICryptoResponse.fromBytes(rep);
            return response.getNftRequests();
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Failed to deserialized response of PROCESS_NFT_TRANSFER request");
            return null;
        }
    }
    public void close() {
        serviceProxy.close();
    }


}
