package dti.utils;

import java.io.Serializable;

public class Pair<K, T> implements Serializable {

    private K a;
    private T b;

    public Pair(K a, T b) {
        this.a = a;
        this.b = b;
    }

    public K getA() {
        return a;
    }

    public T getB() {
        return b;
    }

    public void setA(K a) {
        this.a = a;
    }

    public void setB(T b) {
        this.b = b;
    }
}
