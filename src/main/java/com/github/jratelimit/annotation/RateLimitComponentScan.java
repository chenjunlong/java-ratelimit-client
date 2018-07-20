package com.github.jratelimit.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by chenjunlong on 2018/7/18.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RateLimitBeanDefinitionRegistrar.class)
public @interface RateLimitComponentScan {

    String[] basePackages() default {};
    
    boolean mode() default false;
}
