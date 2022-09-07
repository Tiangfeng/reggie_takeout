package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import com.itheima.reggie.util.Utility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private UserService userService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders order) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空, 无法下单。");
        }

        User user = userService.getById(userId);
        Long addressBookId = order.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null)
            throw new CustomException("地址信息为空, 无法下单。");

        AtomicInteger amount = new AtomicInteger(0);

        long orderId = IdWorker.getId();

        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        order.setNumber(String.valueOf(orderId));
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2);
        order.setAmount(new BigDecimal(amount.get()));
        order.setUserId(userId);
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress(Utility.checkAddress(addressBook.getProvinceName())
        + Utility.checkAddress(addressBook.getCityName())
        + Utility.checkAddress(addressBook.getDistrictName())
        + Utility.checkAddress(addressBook.getDetail()));
        this.save(order);

        orderDetailService.saveBatch(orderDetailList);

        // 清空购物车。
        shoppingCartService.remove(queryWrapper);
    }
}
