package dti.models;

import java.io.Serializable;

public class Coin implements Serializable {
    Long id;
    int owner;
    Float value;

    public Coin(Long id, int owner, Float value) {
        this.id = id;
        this.owner = owner;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public int getOwner() {
        return owner;
    }

    public Float getValue() {
        return value;
    }
}
