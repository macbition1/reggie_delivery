package com.chun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chun.reggie.dto.DishDto;
import com.chun.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    public void saveWithFlavor(DishDto dishDto);


    //According id to search meal info and flavor
    public DishDto getByIdWithFlavor(Long id);

    //Update meal info and flavor
    public void updateWithFlavor(DishDto dishDto);
}
