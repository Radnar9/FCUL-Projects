package dti.models;

import java.io.Serializable;

public class NFT implements Serializable {

    Long id;
    int owner;
    String name;
    String URI;
    int requests;

    public NFT(Long id, int owner, String name, String URI) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.URI = URI;
        requests = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getURI() {
        return URI;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public void incRequests() {
        ++requests;
    }
    public void decRequests() {
        --requests;
    }

    public int getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        return String.format("%d, %s, %s, %d, %d", id, name, URI, owner, requests);
    }
}
