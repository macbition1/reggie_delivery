package com.chun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chun.reggie.entity.Orders;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService extends IService<Orders> {
    @Transactional
    void submit(Orders orders);
}
