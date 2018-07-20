package com.github.jratelimit.filter;

import com.github.jratelimit.commons.constant.Constant;
import com.github.jratelimit.commons.utils.PropsRead;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjunlong on 2018/7/20.
 */
public class LocalDubboRateLimitHandler implements DubboRateLimitHandler {

    private Map<String, Integer> limitConfig = new HashMap<>();

    public LocalDubboRateLimitHandler() throws IOException {
        PropsRead<String, Integer> propsRead = new PropsRead<>(Constant.J_RATELIMIT_CONFIG_URL);
        propsRead.parseMapIntValue();
        this.limitConfig = propsRead.getMap();
    }

    @Override
    public Map<String, Integer> limitConfig() {
        return limitConfig;
    }

    @Override
    public <T> T defaultValue(String interfaceName, String methodName, Object[] args) {
        return null;
    }
}
