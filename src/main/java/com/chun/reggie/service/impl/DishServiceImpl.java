package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.dto.DishDto;
import com.chun.reggie.entity.Dish;
import com.chun.reggie.entity.DishFlavor;
import com.chun.reggie.mapper.DishMapper;
import com.chun.reggie.service.DishFlavorService;
import com.chun.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * ADDING NEW DISH AND SAVING FLAVOR
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //save dish info into table dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //Flavor
        List<DishFlavor> flavors = dishDto.getFlavors();

        //add dish_id
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());


        //save flavor into table dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * According id to search meal info and flavor
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {

        //1.Basic info  from dish
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish,dishDto);

        //2.flavor from table dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }



    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //update table dish
        this.updateById(dishDto);

        //clean or delete current flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //add new flavor
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
