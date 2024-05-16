package com.jsut.bydg_server.BlockChainClient.Bean;

import lombok.Data;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;

/**
 * @Author: zhangyan
 * @Date: 2020/4/3 15:11
 * @Version 1.0
 */
@Data
public class ChaincodeEventCapture {
    private final String handle;
    private final BlockEvent blockEvent;
    private final ChaincodeEvent chaincodeEvent;

    public ChaincodeEventCapture(String handle, BlockEvent blockEvent,
                                 ChaincodeEvent chaincodeEvent) {
        this.handle = handle;
        this.blockEvent = blockEvent;
        this.chaincodeEvent = chaincodeEvent;
    }


}
