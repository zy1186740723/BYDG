package com.jsut.bydg_server.BlockChainClient;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhangyan
 * @Date: 2020/3/27 11:01
 * @Version 1.0
 */
public class puf_bc_test {
    public static void main(String[] args) throws Exception{
        JSONObject jsonObject=new JSONObject();
        Map<String,String> map=new HashMap<>();

//        jsonObject.put("fcn","pufQuery");
//        map.put("iotAddress","a1");

        //jsonObject.put("fcn","pufRegister");
        jsonObject.put("fcn","pufUpdate");
        map.put("iotAddress","a1");
        map.put("challenge","goooohh3");
        map.put("serverChallenge","ho2");
        map.put("beta","jerrtj3");
        map.put("alpha","uuuuucekk4");
        map.put("timestamp","cpvvpe5");
        map.put("helperData","qqqqqqqqre6");
        map.put("hdr","chhdd7");
        map.put("Di","r7777");

//        jsonObject.put("fcn","getHistoryForKey");
//        map.put("iotAddress","a1");

        jsonObject.put("arg",map);
        System.out.println(jsonObject.toJSONString());
        BlockchainService blockchainService=new BlockchainService();
        System.out.println(blockchainService.ChainCode(jsonObject));




//        System.out.println("txId="+txId);
//
//        ChaincodeManager manager= FabricManager.obtain().getManager();
//
//        TransactionInfo transactionInfo=manager.queryTransactionById(txId);
//
//        System.out.println(transactionInfo.getValidationCode());
//
//        System.out.println("交易是否写入："+transactionInfo.getValidationCode().equals(0));










    }
}
