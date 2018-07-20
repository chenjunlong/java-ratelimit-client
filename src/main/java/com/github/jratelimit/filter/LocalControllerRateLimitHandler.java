package com.github.jratelimit.filter;

import com.github.jratelimit.commons.constant.Constant;
import com.github.jratelimit.commons.utils.PropsRead;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjunlong on 2018/7/19.
 */
public class LocalControllerRateLimitHandler implements ControllerRateLimitHandler {

    private Map<String, Integer> limitConfig = new HashMap<>();

    public LocalControllerRateLimitHandler() throws IOException {
        PropsRead<String, Integer> propsRead = new PropsRead<>(Constant.J_RATELIMIT_CONFIG_URL);
        propsRead.parseMapIntValue();
        this.limitConfig = propsRead.getMap();
    }

    @Override
    public Map<String, Integer> limitConfig() {
        return limitConfig;
    }

    @Override
    public Object defaultValue(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return null;
    }
}