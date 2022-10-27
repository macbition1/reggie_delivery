package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.common.CustomException;
import com.chun.reggie.entity.Category;
import com.chun.reggie.entity.Dish;
import com.chun.reggie.entity.Setmeal;
import com.chun.reggie.mapper.CategoryMapper;
import com.chun.reggie.service.CategoryService;
import com.chun.reggie.service.DishService;
import com.chun.reggie.service.SetmealService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * According id to decide whether to delete
     * @param id
     */

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //set search condition
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);


        //current meal whether related other meals
        if(count1 > 0){
            //throw exception
            throw new CustomException("Current category relate meals, can't delete!");
        }

        //current meal whether related other combo
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if(count2 > 0){
            //throw exception
            throw new CustomException("Current meal relates some combo, can't delete!");
        }

        //delete
        super.removeById(id);

    }
}
