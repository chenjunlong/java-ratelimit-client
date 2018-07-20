package com.github.jratelimit.support;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class ServiceRateLimitConfig {

    private static Boolean mode = false;

    public static Boolean getMode() {
        return mode;
    }

    public static void setMode(Boolean mode) {
        ServiceRateLimitConfig.mode = mode;
    }
}
