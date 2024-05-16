package com.jsut.bydg_server.BlockChainClient.Bean;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.File;

/**
 * @Author: zhangyan
 * @Date: 2019/11/4 15:07
 * @Version 1.0
 */

@Slf4j
public class FabricConfig {
    private static Logger logger=log;

    /** 节点服务器对象 */
    private Peers peers;
    /** 排序服务器对象 */
    private Orderers orderers;
    /** 智能合约对象 */
    private Chaincode chaincode;
    /** channel-artifacts所在路径：默认channel-artifacts所在路径/xxx/WEB-INF/classes/fabric/channel-artifacts/ */
    private String channelArtifactsPath;
    /** crypto-config所在路径：默认crypto-config所在路径/xxx/WEB-INF/classes/fabric/crypto-config/ */
    private String cryptoConfigPath;
    private boolean registerEvent = false;

    public FabricConfig() throws Exception{
        // 默认channel-artifacts所在路径 /xxx/WEB-INF/classes/fabric/channel-artifacts/
        channelArtifactsPath = getChannlePath() + "/channel-artifacts/";
        // 默认crypto-config所在路径 /xxx/WEB-INF/classes/fabric/crypto-config/
        cryptoConfigPath = getChannlePath() + "/crypto-config/";
    }

    /**
     * 默认fabric配置路径
     *
     * @return D:/installSoft/apache-tomcat-9.0.0.M21-02/webapps/xxx/WEB-INF/classes/fabric/channel-artifacts/
     */
    private String getChannlePath() throws Exception{
        String directorys = ChaincodeManager.class.getClassLoader().getResource("fabric").getFile();//"./"
        System.out.println("获取当前通道路径："+directorys);
        //String test=ChaincodeManager.class.getClassLoader().getResource("temp.txt").getFile();
        //System.out.println(test.);
        //log.info(test);//
        log.debug("directorys = " + directorys);
        ///JarFile directory=new JarFile(directorys);
        File directory = new File(directorys);
        if (!directory .getParentFile().exists()) {
            directory.getParentFile().mkdirs();
        }
        if (!directory.exists()){
            directory.createNewFile();
        }
        log.debug("directory = " + directory.getPath());
//        return directory.getPath();
        System.out.println("directory 是否存在"+directory.exists());
        System.out.println("directory.getPath()："+directory.getPath());
        return directory.getPath();
        //return directorys;
        // return "src/main/resources/fabric/channel-artifacts/";
    }

    public Peers getPeers() {
        return peers;
    }

    public void setPeers(Peers peers) {
        this.peers = peers;
    }

    public Orderers getOrderers() {
        return orderers;
    }

    public void setOrderers(Orderers orderers) {
        this.orderers = orderers;
    }

    public Chaincode getChaincode() {
        return chaincode;
    }

    public void setChaincode(Chaincode chaincode) {
        this.chaincode = chaincode;
    }

    public String getChannelArtifactsPath() {
        return channelArtifactsPath;
    }

    public void setChannelArtifactsPath(String channelArtifactsPath) {
        this.channelArtifactsPath = channelArtifactsPath;
    }

    public String getCryptoConfigPath() {
        return cryptoConfigPath;
    }

    public void setCryptoConfigPath(String cryptoConfigPath) {
        this.cryptoConfigPath = cryptoConfigPath;
    }

    public boolean isRegisterEvent() {
        return registerEvent;
    }

    public void setRegisterEvent(boolean registerEvent) {
        this.registerEvent = registerEvent;
    }

    /**
     * 根据节点作用类型获取节点服务的配置
     */
//    private FabricConfig getConfig(){
//        FabricConfig config=new FabricConfig();//创建配置
//        config.setOrderers(getOrderers());
//        config.setPeers(getPeers());
//        config.setChaincode(getChaincode());
//        config.setCryptoConfigPath(getCryptoConfigPath());
//        return config;
//    }

}
