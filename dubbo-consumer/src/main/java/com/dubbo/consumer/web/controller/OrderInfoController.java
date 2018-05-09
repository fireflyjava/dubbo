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
    //       url = "dubbo://localhost:12345")
    private OrderService orderService;

    @RequestMapping("/queryOrderInfo")
    public String queryOrderInfo(@RequestParam String orderType) {
        return orderService.queryOrderId(orderType);
    }
}
