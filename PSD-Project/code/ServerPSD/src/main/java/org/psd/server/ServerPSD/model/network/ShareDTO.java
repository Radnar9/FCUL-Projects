package org.psd.server.ServerPSD.model.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.psd.server.ServerPSD.model.Share;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareDTO {
    private String user1;
    private String user2;
    private BigInteger shareholder;
    private BigInteger share;

    public Share toShare() {
        return new Share(user1, user2, shareholder, share);
    }
}
