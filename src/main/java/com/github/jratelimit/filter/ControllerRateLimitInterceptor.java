package com.github.jratelimit.filter;

import com.alibaba.fastjson.JSON;
import com.github.jratelimit.annotation.RateLimit;
import com.github.jratelimit.support.ServiceRateLimitConfig;
import com.github.jratelimit.support.ServiceRateLimitCounter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class ControllerRateLimitInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Map<String, Object> defaultValueMaps = new ConcurrentHashMap<>();

    private ControllerRateLimitHandler rateLimitHandler;

    public ControllerRateLimitInterceptor() throws IOException {
        this(new LocalControllerRateLimitHandler());
    }

    public ControllerRateLimitInterceptor(ControllerRateLimitHandler rateLimitHandler) {
        this.rateLimitHandler = rateLimitHandler;

        Thread daemon = new Thread(() -> {
            while (true) {
                try {
                    this.qps();
                    ServiceRateLimitCounter.clearCounter();
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("ControllerRateLimitInterceptor-daemon-Thread0");
        daemon.start();
    }

    private void qps() {
        Map<String, Integer> limitConfigMaps = rateLimitHandler.limitConfig();
        if (MapUtils.isNotEmpty(limitConfigMaps)) {
            for (Map.Entry<String, Integer> entry : limitConfigMaps.entrySet()) {
                String methodName = entry.getKey();
                Integer limit = entry.getValue();
                if (null == limit || limit <= 0) {
                    continue;
                }
                Long currentQps = ServiceRateLimitCounter.getRate(methodName);
                if (currentQps > limit) {
                    Object methodDefaultValue = defaultValueMaps.get(methodName);
                    Long beLimit = currentQps - limit;
                    if (null != methodDefaultValue) {
                        String methodDefaultValueString = JSON.toJSONString(methodDefaultValue);
                        logger.warn("[{}] Method triggers a limit configuration , CurrentQps:{} , Config:{}, be limiting:{} , The default data format:{}"
                                , methodName, currentQps, limit, beLimit, methodDefaultValueString);
                    } else {
                        logger.warn("[{}] Method triggers a limit configuration , CurrentQps:{} , Config:{}, be limiting:{}"
                                , methodName, currentQps, limit, beLimit);
                    }
                }
            }
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod mHandler = (HandlerMethod) handler;
            if (null == mHandler) {
                return true;
            }
            RateLimit rateLimit = mHandler.getMethodAnnotation(RateLimit.class);
            if (null == rateLimit) {
                return true;
            }

            String interfaceName = mHandler.getBean().getClass().getName();
            String methodName = mHandler.getMethod().getName();
            String rateLimitName = ServiceRateLimitConfig.getMode()
                    ? interfaceName + "." + methodName : methodName;
            // get current method qps
            long methodQps = ServiceRateLimitCounter.incrementAndGet(rateLimitName);

            // get limit config
            Map<String, Integer> limitConfig = rateLimitHandler.limitConfig();
            Integer methodLimited = limitConfig.get(rateLimitName);
            if (null == methodLimited || methodLimited == 0) {
                return true;
            }

            // triggers limit
            if (methodQps > methodLimited) {
                // result default
                String defaultMethodName = rateLimit.defaultMethod();
                Object resultDefaultValue = null;
                if (StringUtils.isEmpty(defaultMethodName)) {
                    resultDefaultValue = rateLimitHandler.defaultValue(request, response, handler);
                } else {
                    try {
                        Object target = mHandler.getBean();
                        Method defaultMethod = null;
                        Method[] methods = target.getClass().getDeclaredMethods();
                        for (Method targetMethod : methods) {
                            if (targetMethod.getName().equals(defaultMethodName)) {
                                defaultMethod = targetMethod;
                                break;
                            }
                        }
                        if (null != defaultMethod) {
                            Collection<String[]> paramsValues = request.getParameterMap().values();
                            Object[] params = new Object[paramsValues.size()];
                            int i = 0;
                            for (String[] value : paramsValues) {
                                params[i] = value[0];
                                i++;
                            }
                            resultDefaultValue = defaultMethod.invoke(target, params);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                defaultValueMaps.put(rateLimitName, resultDefaultValue);
                try {
                    response.addHeader("Content-Type", "application/json; charset=utf-8");
                    response.getWriter().print(resultDefaultValue);
                    response.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
