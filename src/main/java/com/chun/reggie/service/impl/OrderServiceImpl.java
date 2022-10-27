package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.common.BaseContext;
import com.chun.reggie.common.CustomException;
import com.chun.reggie.entity.*;
import com.chun.reggie.mapper.OrderMapper;
import com.chun.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * submit current order
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders){

        //1. Get current user id
        Long currentId = BaseContext.getCurrentId();

        //2.Show current shopping cart details
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        if(list == null || list.size() ==0){
            throw new CustomException("Shopping cart is empty");
        }

        //Show user info
        User user = userService.getById(currentId);

        //Inquiry current user address
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if(addressBook == null){
            throw new CustomException("Without address");
        }

        long orderId = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = list.stream().map((item) ->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet((item.getAmount().multiply(new BigDecimal(item.getNumber()))).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //Insert a item in orders
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//计算总金额
        orders.setUserId(currentId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());

        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                +(addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                +(addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);

        //Insert multi items
        orderDetailService.saveBatch(orderDetails);

        //Clean the shopping cart
        shoppingCartService.remove(queryWrapper);

    }

}
