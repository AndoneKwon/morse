package com.morse.observing.repository;

import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class FailCountRepository {
    ConcurrentMap<String, Integer> failMap = new ConcurrentHashMap<>();

    public void addFail(String presenterIdx) {
        this.failMap.put(presenterIdx,1);
    }

    public void increaseFail(String presenterIdx) {
        this.failMap.computeIfPresent(presenterIdx, (String key, Integer value) -> ++value);
    }

    public int getFail(String presenterIdx) {
        return this.failMap.get(presenterIdx);
    }

    public void deleteFail(String presenterIdx) {
        this.failMap.remove(presenterIdx);
    }

    public boolean checkContain(String presenterIdx) {
        return this.failMap.containsKey(presenterIdx);
    }
}
