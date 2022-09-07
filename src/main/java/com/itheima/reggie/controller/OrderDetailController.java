package com.itheima.reggie.controller;

import com.itheima.reggie.service.OrderDetailService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class OrderDetailController {
    @Resource
    private OrderDetailService orderDetailService;
}
