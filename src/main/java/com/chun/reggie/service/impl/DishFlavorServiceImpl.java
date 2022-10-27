package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.entity.DishFlavor;
import com.chun.reggie.mapper.DishFlavorMapper;
import com.chun.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService  {
}
