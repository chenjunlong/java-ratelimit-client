package com.github.jratelimit.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.github.jratelimit.annotation.RateLimit;
import com.github.jratelimit.support.ServiceRateLimitConfig;
import com.github.jratelimit.support.ServiceRateLimitCounter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.jratelimit.support.ApplicationContextHolder.getBean;

/**
 * Created by chenjunlong on 2018/7/18.
 */
@Activate(group = {Constants.PROVIDER})
public class DubboRateLimitFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Map<String, Object> defaultValueMaps = new ConcurrentHashMap<>();

    private DubboRateLimitHandler rateLimitHandler = getBean(DubboRateLimitHandler.class);
    private RateLimitCallable rateLimitCallable = getBean(RateLimitCallable.class);;

    public DubboRateLimitFilter() throws IOException {
        if (null == rateLimitHandler) {
            rateLimitHandler = new LocalDubboRateLimitHandler();
        }
        if (null == rateLimitCallable) {
            rateLimitCallable = new LocalRateLimitCallable();
        }

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
        daemon.setName("DubboRateLimitFilter-daemon-Thread0");
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
                Long beLimit = currentQps - limit;
                if (currentQps > limit) {
                    Object methodDefaultValue = defaultValueMaps.get(methodName);
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
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Method method = null;
        try {
            method = invoker.getInterface().getDeclaredMethod(methodName, invocation.getParameterTypes());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (null == method) {
            return invoker.invoke(invocation);
        }
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (null == rateLimit) {
            return invoker.invoke(invocation);
        }

        // get current method qps
        String rateLimitName = ServiceRateLimitConfig.getMode()
                ? interfaceName + "." + methodName
                : methodName;
        long methodQps = ServiceRateLimitCounter.incrementAndGet(rateLimitName);

        // get limit config
        Map<String, Integer> limitConfig = rateLimitHandler.limitConfig();
        Integer methodLimited = limitConfig.get(rateLimitName);
        if (null == methodLimited || methodLimited == 0) {
            return invoker.invoke(invocation);
        }

        // triggers limit
        if (methodQps > methodLimited) {
            // result default
            String defaultMethodName = rateLimit.defaultMethod();
            Object resultDefaultValue = null;
            if (StringUtils.isEmpty(defaultMethodName)) {
                resultDefaultValue = rateLimitHandler.defaultValue(interfaceName, methodName, invocation.getParameterTypes());
            } else {
                try {
                    Object target = getBean(invoker.getInterface());
                    Method defaultMethod = null;
                    Method[] methods = target.getClass().getDeclaredMethods();
                    for (Method targetMethod : methods) {
                        if (targetMethod.getName().equals(defaultMethodName)) {
                            defaultMethod = targetMethod;
                            break;
                        }
                    }
                    if (null != defaultMethod) {
                        resultDefaultValue = defaultMethod.invoke(target, invocation.getArguments());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            defaultValueMaps.put(rateLimitName, resultDefaultValue);
            rateLimitCallable.call(interfaceName, methodName);
            return new RpcResult(resultDefaultValue);
        }
        return invoker.invoke(invocation);
    }
}
