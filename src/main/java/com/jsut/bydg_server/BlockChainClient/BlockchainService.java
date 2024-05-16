package com.jsut.bydg_server.BlockChainClient;

import com.alibaba.fastjson.JSONObject;
import com.zy.paas_verifier.BlockChainClient.Bean.ChaincodeManager;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: zhangyan
 * @Date: 2019/11/4 17:47
 * @Version 1.0
 */
@Component
public class BlockchainService {

    //根据block的id查询block信息


    public  Map<String, String> ChainCode (JSONObject json) throws Exception,CryptoException, InvalidArgumentException, ProposalException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, IOException {
        Map<String, String> res=new HashMap<>();
        String fcn=json.getString("fcn");
        JSONObject argJson=json.getJSONObject("arg");
        //System.out.println(argJson.toJSONString());

        Map<String,String> reslutMap;
        List<String> args=new LinkedList<String>();
        String execCode="";
        String execResult="";
        try {
            ChaincodeManager manager= FabricManager.obtain().getManager();
            switch (fcn) {
                //查询用户是否存在

                case "domainRegister":
                    args.add(argJson.containsKey("domain_id") ? argJson.get("domain_id").toString() : "");
                    args.add(argJson.containsKey("ecc_type") ? argJson.get("ecc_type").toString() : "");
                    args.add(argJson.containsKey("pairing_type") ? argJson.get("pairing_type").toString() : "");
                    args.add(argJson.containsKey("P") ? argJson.get("P").toString() : "");
                    args.add(argJson.containsKey("Generator1") ? argJson.get("Generator1").toString() : "");
                    args.add(argJson.containsKey("Generator2") ? argJson.get("Generator2").toString() : "");
                    args.add(argJson.containsKey("Acc") ? argJson.get("Acc").toString() : "");
                    args.add(argJson.containsKey("Version") ? argJson.get("Version").toString() : "");
                    args.add(argJson.containsKey("PK_server") ? argJson.get("PK_server").toString() : "");
                    String[] arguments = new String[args.size()];
                    args.toArray(arguments);
                    res=manager.invoke(fcn,arguments);
                    break;
                case "domainQuery":
                    args.add(argJson.containsKey("domain_id")?argJson.get("domain_id").toString():"");
                    String[] arguments2 = new String[args.size()];
                    args.toArray(arguments2);
                    res= manager.query(fcn,arguments2);
                    break;
                case "domainUpdate":
                    args.add(argJson.containsKey("domain_id") ? argJson.get("domain_id").toString() : "");
                    args.add(argJson.containsKey("ecc_type") ? argJson.get("ecc_type").toString() : "");
                    args.add(argJson.containsKey("pairing_type") ? argJson.get("pairing_type").toString() : "");
                    args.add(argJson.containsKey("Acc") ? argJson.get("Acc").toString() : "");
                    args.add(argJson.containsKey("Version") ? argJson.get("Version").toString() : "");
                    args.add(argJson.containsKey("PK_server") ? argJson.get("PK_server").toString() : "");
                    String[] arguments3 = new String[args.size()];
                    args.toArray(arguments3);
                    res=manager.invoke(fcn,arguments3);
                    break;
                case "getHistoryForKey":
                    args.add(argJson.containsKey("iotAddress")?argJson.get("iotAddress").toString():"");
                    String[] arguments4 = new String[args.size()];
                    args.toArray(arguments4);
                    res= manager.query(fcn,arguments4);
                    break;
                case "getKeySet":
                    args.add(argJson.containsKey("iotAddress")?argJson.get("iotAddress").toString():"");
                    String[] arguments6 = new String[args.size()];
                    args.toArray(arguments6);
                    res= manager.query(fcn,arguments6);
                    break;
            }}catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        catch (CryptoException e1){}

        return res;

    }
}
