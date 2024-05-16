package com.jsut.bydg_server.BlockChainClient;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyHashMap extends LinkedHashMap {

    private final int MAX_CACHE_SIZE=1000;

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size()>MAX_CACHE_SIZE;
    }



}