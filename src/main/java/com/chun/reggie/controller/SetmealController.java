package com.chun.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chun.reggie.common.Result;
import com.chun.reggie.dto.SetmealDto;
import com.chun.reggie.entity.Category;
import com.chun.reggie.entity.Setmeal;
import com.chun.reggie.service.CategoryService;
import com.chun.reggie.service.SetmealDishService;
import com.chun.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * COMBO MANAGE
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
@EnableCaching
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * Save new combo
     * @param setmealDto
     * @return
     */

    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto){

        log.info("New combo info is {}",setmealDto);

       setmealService.saveWithDish(setmealDto);

        return Result.success("New combo created!");

    }


    /**
     * Pagination for Combo
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){


        //pagination object
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        //include categoryId name
        Page<SetmealDto> dtoPage = new Page<>();

        //search condition, according name to do like search
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);


        //sort
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //object coppy
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list =  records.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            //Object copy
            BeanUtils.copyProperties(item, setmealDto);

            //categoryId
            Long categoryId = item.getCategoryId();

            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());

        //show combo in the page
        dtoPage.setRecords(list);

        return Result.success(dtoPage) ;
    }

    /**
     * Delete combo
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result<String> delete(List<Long> ids){

        log.info("Delete combo");
        setmealService.removeWithDish(ids);
        return Result.success("Combo deleted successfully!");
    }


    /**
     * According id to update status
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable int status, String[] ids){
        log.info("Update status of combo");
        setmealService.updateMealStatus(status,ids);
        return Result.success("Hello, Combo Status updated!");

    }


    /**
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> modifyById(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return Result.success(setmealDto);
    }


    /**
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return Result.success("Info modified!");

    }


    /**
     * In mobile to show the meal category
     * @param setmeal
     * @return
     */

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId +'_'+#setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());

        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return Result.success(list);

    }





}
