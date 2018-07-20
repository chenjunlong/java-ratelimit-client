package com.github.jratelimit.annotation;

import com.github.jratelimit.support.ServiceRateLimitConfig;
import com.github.jratelimit.support.ServiceRateLimitScanner;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class RateLimitBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes annAttr = AnnotationAttributes.fromMap(annotationMetadata
                .getAnnotationAttributes(RateLimitComponentScan.class.getName()));

        String[] basePackages = annAttr.getStringArray("basePackages");
        if (ArrayUtils.isEmpty(basePackages)) {
            throw new IllegalArgumentException("RateLimitComponentScan basePackages is empty");
        }

        boolean mode = annAttr.getBoolean("mode");
        ServiceRateLimitConfig.setMode(mode);

        ServiceRateLimitScanner scanner = new ServiceRateLimitScanner();
        for (String basePackage : basePackages) {
            scanner.doScan(basePackage);
        }
    }
}
