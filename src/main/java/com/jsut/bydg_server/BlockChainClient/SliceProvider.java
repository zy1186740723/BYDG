package com.jsut.bydg_server.BlockChainClient;

import com.zy.paas_verifier.BlockChainClient.Bean.ChaincodeManager;
import lombok.Data;
import org.hyperledger.fabric.sdk.TransactionInfo;

@Data
public class SliceProvider {

    private Integer number;

    public static void main(String[] args) throws Exception{

        FabricManager fabricManager=FabricManager.obtain();

        ChaincodeManager manager=fabricManager.getManager();

        TransactionInfo transactionInfo=manager.queryTransactionById("94efc54ad13defc3f4be534b4edf9827471c3f22f87cb4e8dcb23eee5f468217");



//        String[] arguments = new String[]{"mytest_0608_23_45" , "2fd_faff", "3", "4", "5"};
//        Map<String, String> res = manager.invoke("userRegister", arguments);
//
//        //ZKPoK以后，确定所有节点都接受
//        System.out.println(res.get("eventDone"));

        //构造并行的SP，进行处理

        //BFTest.bfTest(8);


       // ChaincodeManager manager2=fabricManager.getManager();



//        String[] arguments2 = new String[]{"AVdO6vyvLB8uoWy5mkl1Dg==","9heGi9jEEEPcS+vHlSxwJA=="};
//        Map<String, String> res1 = manager.query("userQuery", arguments2);
//        if (!(res1.get("code") == "success")) {
//            System.out.println("出现错误");
//        }
//        String playload = res1.get("data");
//        System.out.println(playload);




    }
}
