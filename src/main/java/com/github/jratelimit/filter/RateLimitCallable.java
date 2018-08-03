package com.github.jratelimit.filter;

/**
 * Created by chenjunlong on 2018/7/30.
 */
public interface RateLimitCallable {

    void call(String interfaceName, String methodName);
}