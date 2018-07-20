package com.github.jratelimit.support;

import com.github.jratelimit.annotation.RateLimit;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by chenjunlong on 2018/7/18.
 */
public class ServiceRateLimitScanner implements ResourceLoaderAware {

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(
                resourceLoader);
    }

    public void doScan(String scanPackage) {
        String scanPackagePath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(scanPackage))
                + "/**/*.class";
        Resource[] resources = new Resource[0];
        try {
            resources = this.resourcePatternResolver.getResources(scanPackagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            if (resource.isReadable()) {
                MetadataReader metadataReader = null;
                try {
                    metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    Class classz = Class.forName(metadataReader.getClassMetadata().getClassName());
                    Method[] methods = classz.getMethods();
                    if (ArrayUtils.isEmpty(methods)) {
                        continue;
                    }
                    for (Method method : methods) {
                        Annotation annotation = method.getAnnotation(RateLimit.class);
                        if (null != annotation) {
                            String methodName = ServiceRateLimitConfig.getMode()
                                    ? classz.getName() + "." + method.getName()
                                    : method.getName();
                            ServiceRateLimitCounter.addCounter(methodName);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
