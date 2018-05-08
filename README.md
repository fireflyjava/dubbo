# SpringBoot整合Dubbo+Zookeeper简单分布式实践



[TOC]

## Dubbo 简介 
Apache Dubbo是由阿里巴巴提供开源的高性能的RPC Java框架。在众多RPC框架中，Dubbo是基于定义服务的想法，指定可以通过参数和返回类型的方式远程调用的方法。在服务提供方，服务提供者实现服务接口并处理消费者的调用请求。在服务消费方，消费方通过RPC协议消费服务。
## Dubbo 架构
![](http://dubbo.apache.org/images//dubbo-architecture.png)

Provider： 服务提供者

Consumer：服务消费者

Registry：注册中心

Monitor：监控中心

调用流程 
0.启动服务提供者。 
1.服务提供者在启动时，向注册中心注册自己提供的服务。 
2.服务消费者在启动时，向注册中心订阅自己所需的服务。
3.注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。 
4.服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。 
5.服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。

注册中心 
服务提供方：针对所提供的服务到注册中心发布。 
服务消费方：到服务中心订阅所需的服务。 
对于任何一方，不论服务提供方或者服务消费方都有可能同时兼具两种角色，即需要提供服务也需要消费服务。

##注册中心Zookeeper安装

> [去官网下载ZooKeeper](https://zookeeper.apache.org/)

把包下载后，解压到目录，例如：d:\kevin\zookeeper （解压后更名为zookeeper） 
修改zoo_sample.cfg 文件名(d:\kevin\zookeeper\conf) 为 zoo.cfg 
如果不需要集群，zoo.cfg的内容如下： 

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=d:\kevin\zookeeper\data 
dataLogDir=d:\kevin\zookeeper\log
clientPort=2181
```

如果需要集群，zoo.cfg的内容如下：

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=d:\kevin\zookeeper\data 
dataLogDir=d:\kevin\zookeeper\log
clientPort=2181
server.1=10.20.153.10:2555:3555
server.2=10.20.153.11:2555:3555
```

在data目录下放置myid文件。myid 指明自己的 id，对应上面 zoo.cfg 中 server. 后的数字，第一台的内容为 1，第二台的内容为 2，内容如下：

```
1
```

启动

```
d:\kevin\zookeeper\bin\zkServer.sh start
```

停止

```
d:\kevin\zookeeper\bin\zkServer.sh stop
```

##Dubbo与SpringBoot的集成

###环境要求

- JDK：version 6 或更高
- Gradle：gradle4.0 或更高

###Gradle 依赖

```
compile 'com.alibaba.boot:dubbo-spring-boot-starter:0.1.0'
compile 'com.101tec:zkclient:0.10'
```

###创建dubbo-api工程

服务消费者和服务提供者依赖相同的接口，推荐将接口的定义放在独立的模块便于共享给给服务消费者和提供者。

```
package com.dubbo.service;
/**
 * @author kevinli
 * @date 2018/4/23
 */
public interface OrderService {
    String queryOrderId(String orderType);
}
```

###创建dubbo-provider

实例服务接口

```
package com.dubbo.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.service.OrderService;

/**
 * @author kevinli
 * @date 2018/4/23
 */
@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class OrderServiceImpl implements OrderService {
    @Override
    public String queryOrderId(String orderType) {

        return orderType+"_"+System.currentTimeMillis();
    }
}
```

application.properties增加如下参数

```
#########################
# Server Configurations #
#########################
server.port=7801
server.undertow.accesslog.dir=../logs
server.undertow.accesslog.enabled=true
server.undertow.accesslog.pattern=combined
spring.application.name = dubbo-provider-demo
management.port = 7802
management.security.enabled=false
management.security.roles=ADMIN
security.user.name=admin
security.user.password==123456

##########################
# Logging Configurations #
##########################
logging.file=provider
logging.path=logs
logging.level.com.dubbo=DEBUG
logging.level.org.apache.commons.configuration=DEBUG

# Base packages to scan Dubbo Components (e.g., @Service, @Reference)
dubbo.scan.basePackages  = com.dubbo.provider.service

# Dubbo Config properties
## ApplicationConfig Bean
dubbo.application.id = dubbo-provider
dubbo.application.name = dubbo-provider
dubbo.application.qos.port=22222
dubbo.application.qos.enable=true

## ProtocolConfig Bean
dubbo.protocol.id = dubbo
dubbo.protocol.name = dubbo
dubbo.protocol.port = 9001
dubbo.protocol.status = server

## RegistryConfig Bean
dubbo.registry.id = sz-registry
dubbo.registry.address = zookeeper://127.0.0.1:2181

# Dubbo Endpoint (default status is disable)
endpoints.dubbo.enabled = true

# Dubbo Health
## StatusChecker Name defaults (default : "memory", "load" )
management.health.dubbo.status.defaults = memory
## StatusChecker Name extras (default : empty )
management.health.dubbo.status.extras = load,threadpool
```

###创建dubbo-comsumer

服务消费者要求Spring Bean以注解方式关联到OrderService

```
package com.dubbo.consumer.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dubbo.service.OrderService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kevinli
 * @date 2018/4/24
 */
@RestController
public class OrderInfoController {
    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}")
    private OrderService orderService;

    @RequestMapping("/queryOrderInfo")
    public String queryOrderInfo(@RequestParam String orderType) {
        return orderService.queryOrderId(orderType);
    }
}
```
提供应用启动类
```
package com.dubbo.consumer.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author kevinli
 * @date 2018/4/24
 */
@SpringBootApplication
public class DubboConsumerApplication {
    public static void main(String args[]){
        SpringApplication.run(DubboConsumerApplication.class,args);
    }
}
```
application.properties新增如下配置
```
#########################
# Server Configurations #
#########################
server.port=7701
server.undertow.accesslog.dir=../logs
server.undertow.accesslog.enabled=true
server.undertow.accesslog.pattern=combined
spring.application.name =  dubbo-consumer-demo
management.port = 7702
management.security.enabled=false
management.security.roles=ADMIN
security.user.name=admin
security.user.password==123456


##########################
# Logging Configurations #
##########################
logging.file=consumer
logging.path=logs
logging.level.com.dubbo=DEBUG
logging.level.org.apache.commons.configuration=DEBUG

# Dubbo Config properties
## ApplicationConfig Bean
dubbo.application.id = dubbo-consumer
dubbo.application.name = dubbo-consumer

## Legacy QOS Config
dubbo.qos.port = 22223

## ProtocolConfig Bean
dubbo.protocol.id = dubbo
dubbo.protocol.name = dubbo
dubbo.protocol.port = 9001
dubbo.registry.address=zookeeper://127.0.0.1:2181

# Dubbo Endpoint (default status is disable)
endpoints.dubbo.enabled = true

# Dubbo Health
## StatusChecker Name defaults (default : "memory", "load" )
management.health.dubbo.status.defaults = memory
```
##Dubbo-admin的安装
管理控制台的主要功能包含：路由规则，动态配置，服务降级，访问控制，权重调整，负载均衡，等管理功能。

[官网下载tomcat](http://apache.etoak.com/tomcat/tomcat-6/v6.0.35/bin/apache-tomcat-6.0.35.tar.gz)

解压apache-tomcat-6.0.35.tar.gz

删除webapps/ROOT文件夹下面的文件

将dubbo-admin-2.0.0.war解压

配置 webapps/ROOT/WEB-INF/dubbo.properties

```
dubbo.registry.address=zookeeper://127.0.0.1:2181
dubbo.admin.root.password=root
dubbo.admin.guest.password=guest
```

启动

```
./bin/startup.sh
```

停止

```
./bin/shutdwon.sh
```

访问（ 用户：root，密码：root）

```
http://127.0.0.1:8080
```


