package com.jsut.bydg_server.BlockChainClient;

import com.zy.paas_verifier.BlockChainClient.Bean.ChaincodeManager;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class BFTest {

    public Integer sum=0;


    public static void bfTest(int count) throws Exception{
        //ChaincodeManager manager= FabricManager.obtain().getManager();
        //int count=13;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++)
            executorService.execute(new BFTest().new Task(cyclicBarrier,i));

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("All is finished!---------"+(end-now));
    }

    public class Task implements Runnable {
        private CyclicBarrier cyclicBarrier;
        private Integer i;
        private ChaincodeManager manager;

        public Task(CyclicBarrier cyclicBarrier,int i ) throws Exception {
            this.cyclicBarrier = cyclicBarrier;
            this.i=i;
            this.manager=FabricManager.obtain().getManager();
        }

        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cyclicBarrier.await();
                // 测试内容
                //todd:计算分片
                //提交交易
                String[] arguments=new String[]{"test_06_08_30"+i,"2+00000s"+i,"3","4","5"};
                Map<String, String> res=manager.invoke("userRegister",arguments);
                System.out.println("hello word:"+i+res.get("eventDone"));
                sum=sum+1;


                    //HttpTools.sendRequest("localhost:","/Login",String.valueOf(8081));
//
//                    HttpTools.sendGetRequest("192.168.1.11:","/Login",String.valueOf(8083));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        BFTest.bfTest(1);
    }

}
