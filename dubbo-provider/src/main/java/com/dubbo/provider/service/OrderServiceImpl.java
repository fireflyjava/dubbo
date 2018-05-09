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
