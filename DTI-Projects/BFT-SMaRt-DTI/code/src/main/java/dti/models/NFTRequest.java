package dti.models;

import java.io.Serializable;
import java.util.ArrayList;

public class NFTRequest implements Serializable {

    int issuer;
    String requestedNFT;
    float offeredValue;
    ArrayList<Long> wallet;
    long validity;


    public NFTRequest(int issuer, String requestedNFT, float offeredValue, ArrayList<Long> wallet, long validity) {
        this.issuer = issuer;
        this.requestedNFT = requestedNFT;
        this.offeredValue = offeredValue;
        this.wallet = wallet;
        this.validity = validity;
    }

    public int getIssuer() {
        return issuer;
    }

    public String getRequestedNFT() {
        return requestedNFT;
    }

    public float getOfferedValue() {
        return offeredValue;
    }

    public ArrayList<Long> getWallet() {
        return wallet;
    }

    public long getValidity() {
        return validity;
    }
}
