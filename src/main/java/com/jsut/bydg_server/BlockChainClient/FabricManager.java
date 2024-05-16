package com.jsut.bydg_server.BlockChainClient;

import com.zy.paas_verifier.BlockChainClient.Bean.*;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: zhangyan
 * @Date: 2019/11/4 15:10
 * @Version 1.0
 */

@Slf4j
//@Component
public class FabricManager {
    private ChaincodeManager manager;

    private static FabricManager instance = null;

    //101.35.89.157
    private static String ipAddress="101.35.89.157";

    public static FabricManager obtain()
            throws Exception {
        if (null == instance) {
            synchronized (FabricManager.class) {
                if (null == instance) {
                    instance = new FabricManager();
                }
            }
        }
        return instance;
    }

    private FabricManager()
            throws Exception {
        manager = new ChaincodeManager(getConfig());
    }

    /**
     * 获取节点服务器管理器
     *
     * @return 节点服务器管理器
     */
    public ChaincodeManager getManager() {
        return manager;
    }

    /**
     * 根据节点作用类型获取节点服务器配置
     *
     * @param //type
     *            服务器作用类型（1、执行；2、查询）
     * @return 节点服务器配置
     */
    private FabricConfig getConfig() throws Exception{
        FabricConfig config = new FabricConfig();
        config.setOrderers(getOrderers());
        config.setPeers(getPeers());
        config.setChaincode(getChaincode("assetschannel", "assets", "github.com/chaincode/cross_domain", "1.0.1"));
        config.setChannelArtifactsPath(getChannleArtifactsPath());
        config.setCryptoConfigPath(getCryptoConfigPath());
        return config;
    }

    private Orderers getOrderers() {
        Orderers orderer = new Orderers();
        orderer.setOrdererDomainName("imocc.com");
        orderer.addOrderer("orderer.imocc.com", "grpc://"+ipAddress+":7050");
//        orderer.addOrderer("orderer0.example.com", "grpc://x.x.x.xx:7050");
//        orderer.addOrderer("orderer2.example.com", "grpc://x.x.x.xxx:7050");
        return orderer;
    }

    /**
     * 获取节点服务器集
     *
     * @return 节点服务器集
     */
    private Peers getPeers() {
        Peers peers = new Peers();
        peers.setOrgName("Org1");
        peers.setOrgMSPID("Org1MSP");
        peers.setOrgDomainName("org1.imocc.com");
        //peers.addPeer("peer0.org1.imocc.com", "peer0.org1.imocc.com", "grpc://"+ipAddress+":27051", "grpc://"+ipAddress+":27053", "http://"+ipAddress+":17054");
        peers.addPeer("peer1.org1.imocc.com", "peer1.org1.imocc.com", "grpc://"+ipAddress+":37051", "grpc://"+ipAddress+":37053", "http://"+ipAddress+":17054");
        //peers.addPeer("peer2.org1.imocc.com", "peer2.org1.imocc.com", "grpc://"+ipAddress+":47051", "grpc://"+ipAddress+":47053", "http://"+ipAddress+":17054");

        return peers;
    }

//    private Peers getPeer(String org,String orgMsp, String domain) {
//        Peers peers = new Peers();
//        peers.setOrgName("Org1");
//        peers.setOrgMSPID("Org1MSP");
//        peers.setOrgDomainName("org1.imocc.com");
//        peers.addPeer("peer0.org1.imocc.com", "peer0.org1.imocc.com", "grpc://"+ipAddress+":27051", "grpc://"+ipAddress+":27053", "http://"+ipAddress+":17054");
//        return peers;
//    }

    /**
     * 获取智能合约
     *
     * @param channelName
     *            频道名称
     * @param chaincodeName
     *            智能合约名称
     * @param chaincodePath
     *            智能合约路径
     * @param chaincodeVersion
     *            智能合约版本
     * @return 智能合约
     */
    private Chaincode getChaincode(String channelName, String chaincodeName, String chaincodePath, String chaincodeVersion) {
        Chaincode chaincode = new Chaincode();
        chaincode.setChannelName(channelName);
        chaincode.setChaincodeName(chaincodeName);
        chaincode.setChaincodePath(chaincodePath);
        chaincode.setChaincodeVersion(chaincodeVersion);
        chaincode.setInvokeWatiTime(100000);
        chaincode.setDeployWatiTime(120000);
        return chaincode;
    }

    /**
     * 获取channel-artifacts配置路径
     *
     * @return /WEB-INF/classes/fabric/channel-artifacts/
     */
    private String getChannleArtifactsPath() {
        String directorys = FabricManager.class.getClassLoader().getResource("fabric").getFile();
        log.debug("directorys = " + directorys);
        File directory = new File(directorys);
        log.debug("directory = " + directory.getPath());
        System.out.println(directory.getAbsolutePath());
        return directory.getPath() + "/channel-artifacts/";
    }

    /**
     * 获取crypto-config配置路径
     *
     * @return /WEB-INF/classes/fabric/crypto-config/
     */
    private String getCryptoConfigPath() {
        String directorys = FabricManager.class.getClassLoader().getResource("fabric").getFile();
        log.debug("directorys = " + directorys);
        File directory = new File(directorys);
        log.debug("directory = " + directory.getPath());
        System.out.println(directory.getAbsolutePath());
        return directory.getPath() + "/crypto-config/";
    }

    public static void main(String[] args) throws Exception,CryptoException, InvalidArgumentException, ProposalException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, IOException, InterruptedException, ExecutionException, TimeoutException {

        ChaincodeManager manager=FabricManager.obtain().getManager();
//
        //注册
//        long invokeStartTime=System.currentTimeMillis();
//        String[] arguments=new String[]{"my_domain3","ecc3","Pairing3","P3","gen3","gen3","acc3","version3","pks3"};
//        Map<String, String> res=manager.invoke("domainRegister",arguments);
//        long invokeTime = System.currentTimeMillis() - invokeStartTime;
//        System.out.println("invoke time:" + invokeTime + "ms");

        //AVdO6vyvLB8uoWy5mkl1Dg== 9heGi9jEEEPcS+vHlSxwJA==
        long queryStartTime=System.currentTimeMillis();
        String[] arguments2 = new String[]{"AVdO6vyvLB8uoWy5mkl1Dg==","9heGi9jEEEPcS+vHlSxwJA=="};
        Map<String, String> res1 = manager.query("userQuery", arguments2);
        if (!(res1.get("code") == "success")) {
            System.out.println("出现错误");
        }
        String playload = res1.get("data");
        System.out.println(playload);
        long queryTime = System.currentTimeMillis() - queryStartTime;
        System.out.println("query time:" + queryTime+ "ms");



//        String text="这是进行哈希的数据";
//        byte[] text_byte=text.getBytes("UTF-8");
//
//        //256 bit
//        byte[] text_byte_256= HashTools.Hash2(text_byte);
//
//        byte[] text_byte_128=HashTools.Hash1(text_byte_256);
//        long hashStartTime=System.nanoTime();
//        for (int i = 0; i < 10000; i++) {
//            byte[] a=HashTools.Hash1(text_byte_256);
//        }
//
//        long hashTime = System.nanoTime() - hashStartTime;
//        System.out.println("hash time:" + hashTime/10000000000.0+ "ms");
//
//
//        long aesStartTime=System.nanoTime();
//        AlgorithmParameters iv=CryptoTools.generateIV();
//        for (int i = 0; i < 10000; i++) {
//            CryptoTools.encryptByOwnKey(text_byte,CryptoTools.convertToKey(text_byte_128),iv);
//        }
//        long aesTime = System.nanoTime() - aesStartTime;
//        System.out.println("aes time:" + aesTime/10000000000.0+ "ms");



//        for (int i = 10; i < 100; i++) {
//            String[] arguments=new String[]{"my_domain"+i,"ecc1","Pairing1","P1","gen1","gen2","acc","version1","pks1"};
//            Map<String, String> res=manager.invoke("domainRegister",arguments);
//
//        }

//        String[] arguments=new String[]{"my_domain2"};
//        Map<String, String> res=manager.invoke("domainDelete",arguments);





//            String[] arguments2=new String[]{"my_domain1"};
//            Map<String, String> res=manager.query("domainQuery",arguments2);
//            if (!(res.get("code")=="success")){
//                System.out.println("出现错误");
//            }
//            String playload=res.get("data");
//            System.out.println(playload);



//
//        Map json= JSONObject.parseObject(playload);
//        System.out.println("这个是用JSONObject类的parse方法来解析JSON字符串!!!");
//        for (Object map : json.entrySet()){
//            System.out.println(((Map.Entry)map).getKey()+"  "+((Map.Entry)map).getValue());
//        }
//
//        System.out.println(manager.invoke("userRegister",arguments2));





    }
}
