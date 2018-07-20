package com.github.jratelimit.xml;

import com.github.jratelimit.support.ApplicationContextHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by chenjunlong on 2018/6/7.
 */
public class RegisteredDrivenBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        Object eleSource = parserContext.extractSource(element);

        RootBeanDefinition sourceDef = new RootBeanDefinition(ApplicationContextHolder.class);
        sourceDef.setSource(eleSource);
        sourceDef.setRole(BeanDefinition.ROLE_APPLICATION);
        String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);


        CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
        compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
        parserContext.registerComponent(compositeDef);

        return null;
    }
}
