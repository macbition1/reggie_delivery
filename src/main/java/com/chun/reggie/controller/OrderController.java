package com.chun.reggie.controller;

import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chun.reggie.common.Result;
import com.chun.reggie.dto.OrdersDto;
import com.chun.reggie.entity.OrderDetail;
import com.chun.reggie.entity.Orders;
import com.chun.reggie.service.OrderDetailService;
import com.chun.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * Submit orders
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("Order data: {}", orders);

        orderService.submit(orders);

        return Result.success("Order done");
    }


    /**
     * Submit a order again
     * @param order1
     * @return
     */
    @Transactional
    @PostMapping("/again")
    public Result<String> again(@RequestBody Orders order1){

        //Obtain orderId
        Long id = order1.getId();
        Orders orders = orderService.getById(id);

        //Set order number
        long orderId = IdWorker.getId();
        orders.setId(orderId);

         String number = String.valueOf(IdWorker.getId());
         orders.setNumber(number);

         //set time of order
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);

        //Insert one item
        orderService.save(orders);

        //Modify table orderDetails
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);

        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        list.stream().map((item) ->{
            long detailId = IdWorker.getId();
            item.setOrderId(orderId);
            item.setId(detailId);
            return item;
        }).collect(Collectors.toList());


        orderDetailService.saveBatch(list);

        return Result.success("Order Again");
    }


    /**
     * Order management
     * @param page
     * @param pageSize
     * @return
     */
    //订单管理
    @Transactional
    @GetMapping("/userPage")
    public Result<Page> userPage(int page,int pageSize){
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Page<OrdersDto> ordersDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //进行分页查询
        orderService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records=pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);
            Long Id = item.getId();
            //根据id查分类对象
            Orders orders = orderService.getById(Id);
            String number = orders.getNumber();
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,number);
            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);
            int num=0;

            for(OrderDetail l:orderDetailList){
                num+=l.getNumber().intValue();
            }

            ordersDto.setSumNum(num);


            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return Result.success(ordersDtoPage);
    }


    /**
     * Check order
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String number, String beginTime, String endTime){

        //Pagination constructor
//        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<Orders> pageInfo = new Page<>(page,pageSize);

        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //According number to do fuzzy query
        queryWrapper.like(!StringUtils.isEmpty(number), Orders::getNumber,number);

        //According dateTime to search
        if(beginTime != null && endTime != null){
            queryWrapper.ge(Orders::getOrderTime, beginTime);
            queryWrapper.le(Orders::getOrderTime, endTime);
        }

        //Sort
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //Pagination search
       orderService.page(pageInfo,queryWrapper);

       //Object copy
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item, ordersDto);

            String name = "User" + item.getUserId();
            ordersDto.setUserName(name);
            return ordersDto;

        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return Result.success(ordersDtoPage);

    }


    @PutMapping
    public Result<String> send(@RequestBody Orders orders){
        Long id = orders.getId();
        Integer status = orders.getStatus();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId, id);

        Orders one = orderService.getOne(queryWrapper);

        //set status
        one.setStatus(status);
        orderService.updateById(one);

        return Result.success("Successful delivery");

    }

}
