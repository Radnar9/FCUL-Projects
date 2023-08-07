package dti.dto;

import dti.models.NFT;

import java.io.Serializable;
import java.util.ArrayList;

public class DTICryptoRequestParams implements Serializable {

    private DTICryptoRequestType type;
    private ArrayList<Long> coinsIds;
    private NFT nft;
    private int buyer;
    private Boolean accept;
    private long validity;
    private int userId;
    private int receiver;
    private Float value;
    private String name;
    private String URI;

    public DTICryptoRequestType getType() {
        return type;
    }

    public void setType(DTICryptoRequestType type) {
        this.type = type;
    }

    public ArrayList<Long> getCoinsIds() {
        return coinsIds;
    }

    public void setCoins(ArrayList<Long> coins) {
        this.coinsIds = coins;
    }

    public NFT getNft() {
        return nft;
    }

    public void setNft(NFT nft) {
        this.nft = nft;
    }

    public int getBuyer() {
        return buyer;
    }

    public void setBuyer(int buyer) {
        this.buyer = buyer;
    }

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) {
        this.accept = accept;
    }

    public long getValidity() {
        return validity;
    }

    public void setValidity(long validity) {
        this.validity = validity;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

}
