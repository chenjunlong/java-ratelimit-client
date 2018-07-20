package com.github.jratelimit.support;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class ServiceRateLimitCounter {

    private static ConcurrentMap<String, AtomicLong> counterStorage = new ConcurrentHashMap<>();

    protected static void addCounter(String methodName) {
        counterStorage.put(methodName, new AtomicLong(0L));
    }

    public static long incrementAndGet(String methodName) {
        AtomicLong counter = counterStorage.get(methodName);
        if (null != counter) {
            return counter.incrementAndGet();
        }
        return 0L;
    }

    public static long getRate(String methodName) {
        AtomicLong counter = counterStorage.get(methodName);
        if (null != counter) {
            return counter.get();
        }
        return 0L;
    }

    public static void clearCounter() {
        if (MapUtils.isNotEmpty(counterStorage)) {
            for (Map.Entry<String, AtomicLong> entry : counterStorage.entrySet()) {
                entry.getValue().set(0);
            }
        }
    }
}
