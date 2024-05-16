package com.jsut.bydg_server.BlockChainClient;

import com.zy.paas_verifier.BlockChainClient.Bean.ChaincodeManager;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.util.Iterator;

/**
 * @Author: zhangyan
 * @Date: 2020/3/27 15:14
 * @Version 1.0
 */
public class Monitor {
    public static void main(String[] args) throws Exception {
        ChaincodeManager manager= FabricManager.obtain().getManager();

        //lockInfo blockInfo=manager.queryBlockById("b662282dadf2c8e882956be7314557b43f1307f58132fb8073c346078f380a44");

        /**
         *e5c16df2db379dd4d2c226e1815d6b212db37e0a4eec19991fe29b2b16fd2cf9
         *
         * cf1ded4ca9b5ac43c37e0ee0caa7f9907cad93b52acaa848b8ddc9727e336273
         *
         */

//
//        System.out.println(transactionInfo.getEnvelope().getPayload().toStringUtf8());

        BlockInfo blockInfo=manager.queryBlockById("cf1ded4ca9b5ac43c37e0ee0caa7f9907cad93b52acaa848b8ddc9727e336273");

        Iterator<BlockInfo.EnvelopeInfo> iterator=blockInfo.getEnvelopeInfos().iterator();

        while (iterator.hasNext()){
            BlockInfo.EnvelopeInfo envelope=iterator.next();
            System.out.println(envelope.getTransactionID()+"\n"+"validate"+envelope.getValidationCode());

        }








        //manager.queryBlockChain();

        //System.out.println(blockInfo);
    }
}
