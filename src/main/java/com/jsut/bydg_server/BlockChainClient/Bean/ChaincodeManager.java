package com.jsut.bydg_server.BlockChainClient.Bean;

import com.zy.paas_verifier.BlockChainClient.Monitor_event;
import com.zy.paas_verifier.Model.SPs;
import com.zy.paas_verifier.Model.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @Author: zhangyan
 * @Date: 2019/11/4 14:47
 * @Version 1.0
 */

@Slf4j
public class ChaincodeManager {
    //private static Logger log=Logger.getLogger(ChaincodeManager.class);
    private final static Logger logger = log;

    private FabricConfig config;
    private Orderers orderers;
    private Peers peers;
    private Chaincode chaincode;

    private HFClient client;
    private FabricOrg fabricOrg;
    public Channel channel;
    private ChaincodeID chaincodeID;
    //private static Map<String,TxEventFlag> txMap=new MyHashMap();
    private static Map<String,TxEventFlag> txMap=new ConcurrentHashMap<>();
    private static ChaincodeEventListener chaincodeEventListener;
    private static Vector<ChaincodeEventCapture> chaincodeEvents;

    public static List<Monitor_event> monitor_events=new ArrayList<>();

    public ChaincodeManager(FabricConfig fabricConfig)
            throws CryptoException, InvalidArgumentException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException, TransactionException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.config = fabricConfig;

        orderers = this.config.getOrderers();
        peers = this.config.getPeers();
        chaincode = this.config.getChaincode();

        client = HFClient.createNewInstance();
        log.debug("Create instance of HFClient");
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        log.debug("Set Crypto Suite of HFClient");

        fabricOrg = getFabricOrg();
        channel = getChannel();
        chaincodeID = getChaincodeID();

        client.setUserContext(fabricOrg.getPeerAdmin()); // 也许是1.0.0测试版的bug，只有节点管理员可以调用链码
    }

    private FabricOrg getFabricOrg() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {

        // java.io.tmpdir : C:\Users\yangyi47\AppData\Local\Temp\
        System.out.println("创建临时文件 create");
        File storeFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
        System.out.println(storeFile.getPath() + " " + storeFile.getName() + " "
                + storeFile.getAbsolutePath());
        FabricStore fabricStore = new FabricStore(storeFile);

        // Get Org1 from configuration
        FabricOrg fabricOrg = new FabricOrg(peers, orderers, fabricStore, config.getCryptoConfigPath());
        log.debug("Get FabricOrg");
        return fabricOrg;
    }

    public Channel getChannel()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException, CryptoException, InvalidArgumentException, TransactionException {
        client.setUserContext(fabricOrg.getPeerAdmin());
        return getChannel(fabricOrg, client);
    }

    private Channel getChannel(FabricOrg fabricOrg, HFClient client)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException, CryptoException, InvalidArgumentException, TransactionException {
        Channel channel = client.newChannel(chaincode.getChannelName());
        log.debug("Get Chain " + chaincode.getChannelName());

//        channel.setTransactionWaitTime(chaincode.getInvokeWatiTime());
//        channel.setDeployWaitTime(chaincode.getDeployWatiTime());

        for (int i = 0; i < peers.get().size(); i++) {
            File peerCert = Paths.get(config.getCryptoConfigPath(), "/peerOrganizations", peers.getOrgDomainName(), "peers", peers.get().get(i).getPeerName(), "tls/server.crt")
                    .toFile();
            if (!peerCert.exists()) {
                throw new RuntimeException(
                        String.format("Missing cert file for: %s. Could not find at location: %s", peers.get().get(i).getPeerName(), peerCert.getAbsolutePath()));
            }
            Properties peerProperties = new Properties();
            peerProperties.setProperty("pemFile", peerCert.getAbsolutePath());
            // ret.setProperty("trustServerCertificate", "true"); //testing
            // environment only NOT FOR PRODUCTION!
            peerProperties.setProperty("hostnameOverride", peers.getOrgDomainName());
            peerProperties.setProperty("sslProvider", "openSSL");
            peerProperties.setProperty("negotiationType", "TLS");
            // 在grpc的NettyChannelBuilder上设置特定选项
            peerProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);
            channel.addPeer(client.newPeer(peers.get().get(i).getPeerName(), fabricOrg.getPeerLocation(peers.get().get(i).getPeerName()), peerProperties));
            if (peers.get().get(i).isAddEventHub()) {
                channel.addEventHub(
                        client.newEventHub(peers.get().get(i).getPeerEventHubName(), fabricOrg.getEventHubLocation(peers.get().get(i).getPeerEventHubName()), peerProperties));
            }
        }

        for (int i = 0; i < orderers.get().size(); i++) {
            File ordererCert = Paths.get(config.getCryptoConfigPath(), "/ordererOrganizations", orderers.getOrdererDomainName(), "orderers", orderers.get().get(i).getOrdererName(),
                    "tls/server.crt").toFile();
            if (!ordererCert.exists()) {
                throw new RuntimeException(
                        String.format("Missing cert file for: %s. Could not find at location: %s", orderers.get().get(i).getOrdererName(), ordererCert.getAbsolutePath()));
            }
            Properties ordererProperties = new Properties();
            ordererProperties.setProperty("pemFile", ordererCert.getAbsolutePath());
            ordererProperties.setProperty("hostnameOverride", orderers.getOrdererDomainName());
            ordererProperties.setProperty("sslProvider", "openSSL");
            ordererProperties.setProperty("negotiationType", "TLS");
            ordererProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);
            ordererProperties.setProperty("ordererWaitTimeMilliSecs", "300000");
            channel.addOrderer(
                    client.newOrderer(orderers.get().get(i).getOrdererName(), fabricOrg.getOrdererLocation(orderers.get().get(i).getOrdererName()), ordererProperties));
        }

        config.setRegisterEvent(true);


//        if (config.isRegisterEvent()) {
//            log.info("区块监听器初始化了！！！");
//            channel.registerBlockListener(blockEvent ->  {
//
//               {
//                    // TODO
//                    System.out.println("监听事件开始");
//                    log.debug("========================Event事件监听开始========================");
//                    try {
//
//                        log.info("！！！！！监听事件开始");
//                        System.out.println("监听事件开始");
//                        log.info("event.getChannelId() = " + blockEvent.getChannelId());
//                        log.info("event.getEvent().getChaincodeEvent().getPayload().toStringUtf8() = " + blockEvent.getChannelId());//getEvent().getChaincodeEvent().getPayload().toStringUtf8());
//                        log.info("event.getBlock().getData().getDataList().size() = " + blockEvent.getBlock().getData().getDataList().size());
//                        ByteString byteString =blockEvent.getBlock().getData().getData(0);
//                        String result = byteString.toStringUtf8();
//
//                        //log.info("byteString.toStringUtf8() = " + result);
//
//                        String r1[] = result.split("END CERTIFICATE");
//                        String rr = r1[2];
//                        //log.info("rr = " + rr);
//                    } catch (InvalidProtocolBufferException e) {
//                        // TODO
//                        e.printStackTrace();
//                    }
//                    log.debug("========================Event事件监听结束========================");
//                }
//            });
//        }

        log.info("第二种时间监听器");
        //Vector<ChaincodeEventCapture> chaincodeEvents = new Vector<>(); // Test list to capture
        //String chaincodeEventListenerHandle = ChaincodeManager.setChaincodeEventListener(channel, expectedEventName);
        chaincodeEvents = new Vector<>();
        chaincodeEventListener=ChaincodeManager.setChaincodeEventListener(chaincodeEvents);
        log.info("第二种时间监听器设置完毕");

        log.debug("channel.isInitialized() = " + channel.isInitialized());
        if (!channel.isInitialized()) {
            channel.initialize();
        }
        System.out.println("config是否注册时间" + config.isRegisterEvent());

        return channel;
    }

    private ChaincodeID getChaincodeID() {
        return ChaincodeID.newBuilder().setName(chaincode.getChaincodeName()).setVersion(chaincode.getChaincodeVersion()).setPath(chaincode.getChaincodePath()).build();
    }

    /**
     * 执行智能合约
     *
     * @param fcn  方法名
     * @param args 参数数组
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws IOException
     * @throws TransactionException
     * @throws CryptoException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, String> invoke(String fcn, String[] args)
            throws InvalidArgumentException, ProposalException, InterruptedException, ExecutionException, TimeoutException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, CryptoException, TransactionException, IOException {
        Map<String, String> resultMap = new HashMap<>();

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        /// Send transaction proposal to all peers
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn(fcn);
        transactionProposalRequest.setArgs(args);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        transactionProposalRequest.setTransientMap(tm2);
        String expectedEventName = null;
        switch (fcn) {
            case "deviceRegister":
                expectedEventName = "Device Ledger Registered";
                break;
            case "registerVerifier":
                expectedEventName = "VerifierPK Registered";
                break;
            case "ZKPoK":
                expectedEventName = "ZKP Uploaded";
                break;
            case "ZKPoKRe":
                expectedEventName = "ZKPRe Uploaded";
                break;
            case "registerCRPs":
                expectedEventName = "CRP Registered";
                break;

        }

        // chaincode events.
        String eventListenerHandle = channel.registerChaincodeEventListener(Pattern.compile(".*"),
                Pattern.compile(Pattern.quote(expectedEventName)), chaincodeEventListener);
        log.info(eventListenerHandle);
        List list=new ArrayList();
        list.add(eventListenerHandle);list.add(chaincodeEvents);

//        log.info("第二种时间监听器");
//        //Vector<ChaincodeEventCapture> chaincodeEvents = new Vector<>(); // Test list to capture
//        //String chaincodeEventListenerHandle = ChaincodeManager.setChaincodeEventListener(channel, expectedEventName);
//        List list=ChaincodeManager.setChaincodeEventListener(channel, expectedEventName);
//        log.info("第二种时间监听器设置完毕");


        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
        if (proposalConsistencySets.size() != 1) {
            log.error("Expected only one set of consistent proposal responses but got " + proposalConsistencySets.size());
        }

        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
            log.error("Not enough endorsers for inspect:" + failed.size() + " endorser error: " + firstTransactionProposalResponse.getMessage() + ". Was verified: "
                    + firstTransactionProposalResponse.isVerified());
            resultMap.put("code", "error");
            resultMap.put("data", firstTransactionProposalResponse.getMessage());
            return resultMap;
        } else {
            log.info("Successfully received transaction proposal responses.");
            ProposalResponse resp = transactionPropResp.iterator().next();
            logger.debug("success:"+successful.size());
            TxEventFlag txEventFlag=new TxEventFlag();
            txEventFlag.setNumber(0);
            txMap.put(resp.getTransactionID(),txEventFlag);
            System.out.println("放入txId="+resp.getTransactionID());

            byte[] x = resp.getChaincodeActionResponsePayload();
            String resultAsString = null;
            if (x != null) {
                resultAsString = new String(x, "UTF-8");
            }
            log.info("resultAsString = " + resultAsString);
            channel.sendTransaction(successful);
            resultMap.put("code", "success");
            resultMap.put("data", resultAsString);
            resultMap.put("TransactionId", resp.getTransactionID());
            System.out.println("invoke的" + resp.getTransactionID());

             //START WAIT FOR THE EVENT-------------------------------------
            boolean eventDone = false;
            eventDone = ChaincodeManager
                    .waitForChaincodeEvent(150, channel, chaincodeEvents, list.get(0).toString(),resp.getTransactionID());
            log.info("eventDone: " + eventDone);
            if (eventDone) {
                resultMap.put("eventDone", "True");
            } else {
                resultMap.put("eventDone", "False");
            }

            txMap.remove(resp.getTransactionID());

            return resultMap;
        }

//        channel.sendTransaction(successful).thenApply(transactionEvent -> {
//            if (transactionEvent.isValid()) {
//                log.info("Successfully send transaction proposal to orderer. Transaction ID: " + transactionEvent.getTransactionID());
//            } else {
//                log.info("Failed to send transaction proposal to orderer");
//            }
//            // chain.shutdown(true);
//            return transactionEvent.getTransactionID();
//        }).get(chaincode.getInvokeWatiTime(), TimeUnit.SECONDS);
    }

    /**
     * 查询智能合约
     *
     * @param fcn  方法名
     * @param args 参数数组
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     * @throws IOException
     * @throws TransactionException
     * @throws CryptoException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, String> query(String fcn, String[] args) throws InvalidArgumentException, ProposalException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, CryptoException, TransactionException, IOException {
        Map<String, String> resultMap = new HashMap<>();
        String payload = "";
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(args);
        queryByChaincodeRequest.setFcn(fcn);
        queryByChaincodeRequest.setChaincodeID(chaincodeID);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
        for (ProposalResponse proposalResponse : queryProposals) {
            if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                log.debug("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() + ". Messages: "
                        + proposalResponse.getMessage() + ". Was verified : " + proposalResponse.isVerified());
                resultMap.put("code", "error");
                resultMap.put("data", "Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() + ". Messages: "
                        + proposalResponse.getMessage() + ". Was verified : " + proposalResponse.isVerified());
            } else {
                payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                log.debug("Query payload from peer: " + proposalResponse.getPeer().getName());
                log.debug("" + payload);
                log.info("Transaction id:" + proposalResponse.getTransactionID());
                log.info("eventName:" );
                resultMap.put("code", "success");
                resultMap.put("data", payload);
                resultMap.put("TransactionId", proposalResponse.getTransactionID());

            }
        }
        return resultMap;
    }

    //根据blockchain的id，查询block
    public BlockInfo queryBlockById(String transactionId) throws ProposalException, InvalidArgumentException {

        BlockInfo returnedBlock = channel.queryBlockByTransactionID(transactionId);

        log.info("block number:" + returnedBlock.getBlockNumber());

        return returnedBlock;

    }

    //根据transactionID，查询transaction的信息
    public TransactionInfo queryTransactionById(String transactionId) throws InvalidArgumentException, ProposalException {

        List<String> nameList = new ArrayList<>();
        System.out.println(channel.getPeers().size());
        //Iterator<Peer> iterator=channel.getPeers().iterator();

        //Peer peer=iterator.next();

        TransactionInfo TransactionInfo = channel.queryTransactionByID(transactionId);

        return TransactionInfo;

    }

    public BlockchainInfo queryBlockChain() throws InvalidArgumentException, ProposalException {
        BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();

        List<String> nameList = new ArrayList<>();
        System.out.println(channel.getPeers().size());
        Iterator<Peer> iterator = channel.getPeers().iterator();

        while (iterator.hasNext()) {
            Peer peer = iterator.next();
            nameList.add(peer.getName());
        }

        System.out.println(nameList);

        return blockchainInfo;
    }

    public static ChaincodeEventListener setChaincodeEventListener(Vector<ChaincodeEventCapture> chaincodeEvents)
            throws InvalidArgumentException {


        ChaincodeEventListener chaincodeEventListener = new ChaincodeEventListener() {
            @Override
            public void received(String handle, BlockEvent blockEvent,
                                 ChaincodeEvent chaincodeEvent) {
                logger.info("监听到事件");

                chaincodeEvents.add(new ChaincodeEventCapture(handle, blockEvent, chaincodeEvent));

                logger.info(chaincodeEvent.getEventName()+" 内容："+new String(chaincodeEvent.getPayload()));

                //监听到了适合的事件
                boolean invoke_or_lister=true;
                switch (chaincodeEvent.getEventName()) {

                    case "EPSS Uploaded":{
                        try {
                            monitorEPSS("EPSS Uploaded", new String(chaincodeEvent.getPayload()));
                            invoke_or_lister=false;
                            break;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "EPSSRe Uploaded":{
                        try {
                            monitorEPSSRe("EPSSRe Uploaded", new String(chaincodeEvent.getPayload()));
                            invoke_or_lister=false;
                            break;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }

                if (!invoke_or_lister){
                    return ;
                }

                while (txMap.get(chaincodeEvent.getTxId())==null){

                }

                if (txMap.get(chaincodeEvent.getTxId()).getEventHub()==null){
                    TxEventFlag txEventFlag=new TxEventFlag();
                    txEventFlag.setEventHub(blockEvent.getPeer().getName());
                    txEventFlag.setNumber(1);
                    txMap.put(chaincodeEvent.getTxId(),txEventFlag);
                } else if (txMap.get(chaincodeEvent.getTxId()).getEventHub() != null
                        && blockEvent.getPeer().getName()
                        .equals(txMap.get(chaincodeEvent.getTxId()).getEventHub()) == false) {
                    Integer flag = txMap.get(chaincodeEvent.getTxId()).getNumber() + 1;
                    TxEventFlag txEventFlag = new TxEventFlag();
                    txEventFlag.setEventHub(blockEvent.getPeer().getName());
                    txEventFlag.setNumber(flag);
                    txMap.put(chaincodeEvent.getTxId(),txEventFlag);
                }

                logger.info(chaincodeEvent.getTxId()+":"+txMap.get(chaincodeEvent.getTxId()).getNumber());

                String eventHub = blockEvent.getPeer().getName();
                if (eventHub != null) {
                    eventHub = blockEvent.getPeer().getName();
                } else {
                    eventHub = blockEvent.getPeer().getName();
                }
                // Here put what you want to do when receive chaincode event
                System.out.println("RECEIVED CHAINCODE EVENT with handle: " + handle
                        + ", chaincodeId: " + chaincodeEvent.getChaincodeId()
                        + ", chaincode event name: " + chaincodeEvent.getEventName()
                        + ", transactionId: " + chaincodeEvent.getTxId() + ", event Payload: "
                        + new String(chaincodeEvent.getPayload()) + ", from eventHub: " + eventHub);


            }
        };
        // chaincode events.
//        String eventListenerHandle = channel.registerChaincodeEventListener(Pattern.compile(".*"),
//                Pattern.compile(Pattern.quote(expectedEventName)), chaincodeEventListener);
//        log.info(eventListenerHandle);
//        List list=new ArrayList();
//        list.add(eventListenerHandle);list.add(chaincodeEvents);
        return chaincodeEventListener;
    }

    private static void monitorEPSS(String eventName,String s) throws Exception {

        Monitor_event monitor_event=new Monitor_event();

        String[] read=s.split(":");
        monitor_event.setEventName(eventName);monitor_event.setAddress(read[0]);
        monitor_event.setNonce(read[1]);

        Verifier.processEPPS(monitor_event);

    }

    private static void monitorEPSSRe(String eventName,String s) throws Exception {

        Monitor_event monitor_event=new Monitor_event();

        String[] read=s.split(":");
        monitor_event.setEventName(eventName);monitor_event.setAddress(read[0]);
        monitor_event.setNonce(read[1]);

        Verifier.processEPPSRe(monitor_event);

    }

    private static void monitorZKPoK(String eventName,String s) {

        Monitor_event monitor_event=new Monitor_event();

        String[] read=s.split(":");
        monitor_event.setEventName(eventName);monitor_event.setAddress(read[0]);
        monitor_event.setNonce(read[1]);

        SPs.processZKP(monitor_event);

    }

    public static boolean waitForChaincodeEvent(Integer timeout, Channel channel,
                                                Vector<ChaincodeEventCapture> chaincodeEvents, String chaincodeEventListenerHandle,String txId)
            throws InvalidArgumentException {
        boolean eventDone = false;
        if (chaincodeEventListenerHandle != null) {


            int numberEventsExpected = channel.getEventHubs().size() + channel
                    .getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).size();



            log.info("numberEventsExpected: " + numberEventsExpected
                    +" txMap Size="+txMap.size()+" number:"+txMap.get(txId));
            System.out.println("number:"+txMap.get(txId).getNumber());
            //just make sure we get the notifications
            if (timeout.equals(0)) {
                // get event without timer
                while (txMap.get(txId).getNumber() != numberEventsExpected) {
                    // do nothing
                }
                eventDone = true;
            } else {
                // get event with timer
                //int temp=chaincodeEvents.size();
                for (int i = 0; i < timeout; i++) {
                    //if (chaincodeEvents.size() == numberEventsExpected)
                    if (txMap.get(txId).getNumber()>=numberEventsExpected) {
                        eventDone = true;
                        break;
                    } else {
                        try {
                            double j = i;
                            j = j / 10;
                            log.info(j + " second");
                            Thread.sleep(100); // wait for the events for one tenth of second.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            log.info("chaincodeEvents.size(): " + chaincodeEvents.size());
            channel.unregisterChaincodeEventListener(chaincodeEventListenerHandle);

            // unregister event listener
            //channel.unregisterChaincodeEventListener(chaincodeEventListenerHandle);
            int i = 1;
            // arrived event handling
//            for (ChaincodeEventCapture chaincodeEventCapture : chaincodeEvents) {
//                log.info("Event number. " + i);
//                log.info("event capture object: " + chaincodeEventCapture.toString());
//                log.info("Event Handle: " + chaincodeEventCapture.getHandle());
//                log.info("Event TxId: " + chaincodeEventCapture.getChaincodeEvent().getTxId());
//                log.info("Event Name: " + chaincodeEventCapture.getChaincodeEvent().getEventName());
//                log.info("Event Payload: " + chaincodeEventCapture.getChaincodeEvent()
//                        .getPayload()); // byte
//                log.info("Event ChaincodeId: " + chaincodeEventCapture.getChaincodeEvent()
//                        .getChaincodeId());
//                BlockEvent blockEvent = chaincodeEventCapture.getBlockEvent();
//
//
//                try {
//                    log.info("Event Channel: " + blockEvent.getChannelId());
//                } catch (InvalidProtocolBufferException e) {
//                    e.printStackTrace();
//                }
//                log.info("Event Hub: " + blockEvent.getEventHub());
//                i++;
//            }
            chaincodeEvents.clear();


        } else {
            log.info("chaincodeEvents.isEmpty(): " + chaincodeEvents.isEmpty());
        }
        return eventDone;
    }

    public Peers getPeers() {
        return peers;
    }

    public Vector<ChaincodeEventCapture> getChaincodeEvents() {
        return chaincodeEvents;
    }
}
