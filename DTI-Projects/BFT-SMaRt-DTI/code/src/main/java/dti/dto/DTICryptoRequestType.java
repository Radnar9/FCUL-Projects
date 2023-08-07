/**
 * BFT Map implementation (message types).
 */

package dti.dto;

public enum DTICryptoRequestType {
    MY_COINS,
    MINT_COIN,
    SPEND,
    MY_NFTS,
    MINT_NFT,
    REQUEST_NFT,
    CANCEL_REQUEST,
    CURRENT_NFT_REQUESTS,
    PROCESS_NFT_TRANSFER,

    CHECK_EXISTING_NFTS,
    CHECK_MY_NFT_REQUESTS,
}

