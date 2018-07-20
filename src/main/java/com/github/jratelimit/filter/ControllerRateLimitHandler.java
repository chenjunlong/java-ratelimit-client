package com.github.jratelimit.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public interface ControllerRateLimitHandler {

    Map<String, Integer> limitConfig();

    Object defaultValue(HttpServletRequest request, HttpServletResponse response, Object handler);
}
