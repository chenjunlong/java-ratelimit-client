package com.github.jratelimit.filter;

import java.util.Map;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public interface DubboRateLimitHandler {

    Map<String, Integer> limitConfig();

    <T> T defaultValue(String interfaceName, String methodName, Object[] args);
}