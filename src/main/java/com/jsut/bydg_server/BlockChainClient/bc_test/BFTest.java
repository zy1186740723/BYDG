package com.jsut.bydg_server.BlockChainClient.bc_test;

import com.zy.paas_verifier.BlockChainClient.Bean.ChaincodeManager;
import com.zy.paas_verifier.BlockChainClient.FabricManager;
import com.zy.paas_verifier.CryptoUtils.CommonUtils;
import com.zy.paas_verifier.CryptoUtils.HashTools;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BFTest {
    public static void main(String[] args) throws Exception {
        ChaincodeManager manager= FabricManager.obtain().getManager();
        int count=1;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        long now = System.currentTimeMillis();

        Future query_time=null;
        List<Future> res=new ArrayList();
        for (int i = 0; i < count; i++){
            //executorService.execute(new BFTest().new Task(cyclicBarrier,i,manager));
            query_time=executorService.submit(new BFTest().new Task2(cyclicBarrier,i,manager));
            res.add(query_time);
        }


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

        //计算平均时间
        long add_query_time=0;
        for (int i = 0; i < res.size(); i++) {
            add_query_time=add_query_time+(long)res.get(i).get();
        }
        System.out.println("average:"+add_query_time/count);
    }



    public class Task implements Runnable {
        private CyclicBarrier cyclicBarrier;
        private Integer i;
        private ChaincodeManager manager;


        public Task(CyclicBarrier cyclicBarrier,int i,ChaincodeManager manager) {
            this.cyclicBarrier = cyclicBarrier;
            this.i=i;
            this.manager=manager;

        }

        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cyclicBarrier.await();
                // 测试内容
                long queryStartTime = System.currentTimeMillis();
                byte[] domainId= HashTools.Hash1(CommonUtils.intToByteArray(1));
                String[] arguments = new String[]{Base64.getEncoder().encodeToString(domainId)};
                Map<String, String> res1 = manager.query("domainQuery", arguments);
                if (!(res1.get("code") == "success")) {
                    System.out.println("出现错误");
                }
                String playload = res1.get("data");
                System.out.println(playload);
                long queryTime = System.currentTimeMillis() - queryStartTime;
                System.out.println("query time:" + queryTime + "ms");





            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Task2 implements Callable {
        private CyclicBarrier cyclicBarrier;
        private Integer i;
        private ChaincodeManager manager;


        public Task2(CyclicBarrier cyclicBarrier,int i,ChaincodeManager manager) {
            this.cyclicBarrier = cyclicBarrier;
            this.i=i;
            this.manager=manager;

        }

        @Override
        public Object call() throws Exception{

                // 等待所有任务准备就绪
                cyclicBarrier.await();
                // 测试内容
                long queryStartTime = System.currentTimeMillis();
                byte[] domainId= HashTools.Hash1(CommonUtils.intToByteArray(1));
                String[] arguments = new String[]{Base64.getEncoder().encodeToString(domainId)};
                Map<String, String> res1 = manager.query("domainQuery", arguments);
                if (!(res1.get("code") == "success")) {
                    System.out.println("出现错误");
                }
                String playload = res1.get("data");
                System.out.println(playload);
                long queryTime = System.currentTimeMillis() - queryStartTime;
                System.out.println("query time:" + queryTime + "ms");




                return queryTime;

        }

    }



}
