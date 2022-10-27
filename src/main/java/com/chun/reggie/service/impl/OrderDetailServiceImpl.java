package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.entity.OrderDetail;
import com.chun.reggie.mapper.OrderDetailMapper;
import com.chun.reggie.service.OrderDetailService;
import com.chun.reggie.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
