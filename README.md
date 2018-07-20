# java-ratelimit-client

### Java 限流组件 ###

1.0版本介绍：

1、Spring Web Controller 接口的限流<br/>
2、Dubbo 接口的限流<br/>

```
    <dependency>
      <groupId>com.github.jratelimit</groupId>
      <artifactId>java-ratelimit-client</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>   
```

### Spring Web Controller 接口限流使用示例 ###

#### Annotation方式 ####
```
// 添加@RateLimitComponentScan注解，这个注解会根据包路径扫描Controller上存在@RateLimit的注解，进行限流计数器的创建
@RateLimitComponentScan(basePackages = {"com.chenjunlong.springboot.example"})
@SpringBootApplication(scanBasePackages = "com.chenjunlong.springboot.example")
public class AnnotationRateLimitBootStrapApplication implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加自定义限流配置类，在这个接口里编写限流逻辑
        ControllerRateLimitHandler rateLimitHandler = new CustomRateLimitHandler();
        
        // 添加限流拦截器
        registry.addInterceptor(new ControllerRateLimitInterceptor(rateLimitHandler)).addPathPatterns("/**");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AnnotationRateLimitBootStrapApplication.class, args);
    }
}
```

#### XML方式 ####
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:ratelimit="http://www.github.com/schema/ratelimit"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.github.com/schema/ratelimit
        http://www.github.com/schema/ratelimit/ratelimit.xsd
        http://www.springframework.org/schema/mvc
		http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <ratelimit:annotation-scan base-package="com.chenjunlong.springboot.example.controller"/>

    // 添加自定义限流配置类
    <bean id="rateLimitHandler" class="com.chenjunlong.springboot.example.ratelimit.CustomRateLimitHandler"/>
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean class="com.github.jratelimit.filter.ControllerRateLimitInterceptor">
                <constructor-arg index="0" ref="rateLimitHandler"/>
            </bean>
        </mvc:interceptor>
    </mvc:interceptors>
</beans>
```

#### 限流配置 ####
实现 ControllerRateLimitHandler 接口
```
public class CustomRateLimitHandler implements ControllerRateLimitHandler {

    @Override
    public Map<String, Integer> limitConfig() {
        // k接口的方法名 v限流的配置数/每秒
        Map<String, Integer> maps = new HashMap<>();
        maps.put("action", 1);
        maps.put("action2", 1);
        maps.put("action3", 1);
        
        // 这个配置可以使用zookeeper或者redis之类的进行存储，方便随时修改
        return maps;
    }

    @Override
    public JSONObject defaultValue(HttpServletRequest request, HttpServletResponse response, Object handler) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "400");
        jsonObject.put("msg", "接口访问受限2...");
        return jsonObject;
    }
}
```

如果限流参数想配置成固定的，可以使用配置.properties文件的形式
如果没有实现ControllerRateLimitHandler接口，会默认读取jratelimit.properties配置
```
#k(方法名)=v(限流数/每秒)
action=1
action2=1
action3=1
```

#### 设置需要限流的接口 ####

```
@RestController
public class ExampleController {

    // defaultMethod参数是触发限流后执行的方法名，参数必须和Controller接口的参数一致，并且必须在同一个Controller中
    @RateLimit(defaultMethod = "actionDefaultMethod")
    @RequestMapping(value = "/", method = {RequestMethod.POST, RequestMethod.GET})
    public JSONObject action() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "1");
        jsonObject.put("msg", "操作成功");
        return jsonObject;
    }

    // 
    public JSONObject actionDefaultMethod() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "400");
        jsonObject.put("msg", "接口访问受限");
        return jsonObject;
    }
}
```


### Dubbo 接口限流使用示例 ###

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:ratelimit="http://www.github.com/schema/ratelimit"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.github.com/schema/ratelimit
        http://www.github.com/schema/ratelimit/ratelimit.xsd">

    <ratelimit:registered-driven/>
    <ratelimit:annotation-scan base-package="com.chenjunlong.dubbo.api"/>
</beans>
```

在com.alibaba.dubbo.rpc.Filter 中添加过滤器
```
DubboRateLimitFilter=com.github.jratelimit.filter.DubboRateLimitFilter
```

在Dubbo API中增加限流的注解
```
@RateLimit(defaultMethod = "sayHelloDefaultMethod")
String sayHello();

String sayHelloDefaultMethod();
```


### Demo ###
https://github.com/chenjunlong/springboot-example
https://github.com/chenjunlong/dubbo-example
