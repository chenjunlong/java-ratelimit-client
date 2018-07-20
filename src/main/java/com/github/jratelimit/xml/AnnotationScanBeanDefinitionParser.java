package com.github.jratelimit.xml;

import com.github.jratelimit.support.ServiceRateLimitConfig;
import com.github.jratelimit.support.ServiceRateLimitScanner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by chenjunlong on 2018/6/8.
 */
public class AnnotationScanBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        String basePackage = element.getAttribute("base-package");
        if (StringUtils.isBlank(basePackage)) {
            throw new IllegalArgumentException("annotation-scan package is empty");
        }

        basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
        String[] basePackages = org.springframework.util.StringUtils.tokenizeToStringArray(basePackage, ",; \t\n");


        boolean mode = Boolean.valueOf(element.getAttribute("mode"));
        ServiceRateLimitConfig.setMode(mode);


        ServiceRateLimitScanner scanner = new ServiceRateLimitScanner();
        if (ArrayUtils.isEmpty(basePackages)) {
            return null;
        }
        for (String scanPackage : basePackages) {
            scanner.doScan(scanPackage);
        }

        return null;
    }
}
