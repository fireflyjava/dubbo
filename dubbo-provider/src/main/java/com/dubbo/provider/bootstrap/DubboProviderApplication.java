package com.dubbo.provider.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author kevinli
 * @date 2018/4/24
 */
@SpringBootApplication
public class DubboProviderApplication {
    public static void main(String [] args){
        SpringApplication.run(DubboProviderApplication.class,args);
    }
}
