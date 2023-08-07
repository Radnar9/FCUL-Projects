package dti.dto;


import java.io.*;

public class DTICryptoRequest implements Serializable {
    DTICryptoRequestParams params;

    public DTICryptoRequest(DTICryptoRequestParams params) {
        this.params = params;
    }

    public static byte[] toBytes(DTICryptoRequest message) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(message);

        objOut.flush();
        byteOut.flush();

        return byteOut.toByteArray();
    }

    public static DTICryptoRequest fromBytes(byte[] rep) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(rep);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        return (DTICryptoRequest) objIn.readObject();
    }

    public DTICryptoRequestParams getParams() {
        return params;
    }

    public void setParams(DTICryptoRequestParams params) {
        this.params = params;
    }
}

