package com.chun.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDto;
import com.chun.reggie.common.Result;
import com.chun.reggie.dto.DishDto;
import com.chun.reggie.entity.Category;
import com.chun.reggie.entity.Dish;
import com.chun.reggie.entity.DishFlavor;
import com.chun.reggie.service.CategoryService;
import com.chun.reggie.service.DishFlavorService;
import com.chun.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DISH MANAGE
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * ADD A NEW DISH
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        //If database changes, then need to clean redis cache
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);

        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return Result.success("Add dish!");

    }


    /**
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){

        //Pagination constructor
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //Condition constructor
        LambdaQueryWrapper< Dish > queryWrapper = new LambdaQueryWrapper<>();

        //Add filter condition
        queryWrapper.like(name != null, Dish::getName, name);

        //Sort
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //Running pagination
        dishService.page(pageInfo, queryWrapper);

        //Object copy
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map(item ->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();

            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;

        }).collect(Collectors.toList());


        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);

    }


    /**
     * According id to search meal info and flavor
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }


    /**
     * UPDATE DISH INFO
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //Clean old cache
        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);

        return Result.success("Modify dish!");

    }


    /**
     * According categoryId to show information in combo list
     * @param dish
     * @return
     */

    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {


        //Set key
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_"+dish.getStatus();

        //From redis get data if there exist
        dishDtoList  =(List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList != null){
            return Result.success(dishDtoList);
        }

            //If redis doesn't cache the data of dish, need to check database
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

            //Add search condition
            queryWrapper.eq(Dish::getStatus, 1);
            queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

            //Sort
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

            List<Dish> list = dishService.list(queryWrapper);

                dishDtoList = list.stream().map((item) -> {
                DishDto dishDto = new DishDto();

                BeanUtils.copyProperties(item, dishDto);
                Long categoryId = item.getCategoryId();

                //According category id to search
                Category category = categoryService.getById(categoryId);

                if (category != null) {
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
                }

                //Current dish id
                Long dishId = item.getId();
                LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
                List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

                dishDto.setFlavors(dishFlavorList);

                return dishDto;

            }).collect(Collectors.toList());

            //Cache dish into redis
            redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

            return Result.success(dishDtoList);

    }



}



