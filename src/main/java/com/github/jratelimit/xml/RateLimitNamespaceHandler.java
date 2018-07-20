package com.github.jratelimit.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class RateLimitNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("registered-driven", new RegisteredDrivenBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-scan", new AnnotationScanBeanDefinitionParser());
    }
}