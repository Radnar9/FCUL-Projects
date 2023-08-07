package dti.dto;

import dti.models.NFT;
import dti.models.NFTRequest;
import dti.utils.Pair;

import java.io.*;
import java.util.ArrayList;

public class DTICryptoResponse implements Serializable {

    private long id;
    private ArrayList<NFT> nfts;
    private ArrayList<NFTRequest> nftRequests;
    private ArrayList<Pair<Long, Float>> coins;

    public DTICryptoResponse() {
    }

    public static byte[] toBytes(DTICryptoResponse message) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(message);

        objOut.flush();
        byteOut.flush();

        return byteOut.toByteArray();
    }

    public static <K, V> DTICryptoResponse fromBytes(byte[] rep) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(rep);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        return (DTICryptoResponse) objIn.readObject();
    }

    public ArrayList<Pair<Long, Float>> getCoins() {
        return coins;
    }

    public void setCoins(ArrayList<Pair<Long, Float>> coins) {
        this.coins = coins;
    }

    public ArrayList<NFT> getNfts() {
        return nfts;
    }

    public void setNfts(ArrayList<NFT> nfts) {
        this.nfts = nfts;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<NFTRequest> getNftRequests() {
        return nftRequests;
    }

    public void setNftRequests(ArrayList<NFTRequest> nftRequests) {
        this.nftRequests = nftRequests;
    }
}
