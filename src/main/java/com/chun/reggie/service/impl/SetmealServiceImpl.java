package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.common.CustomException;
import com.chun.reggie.common.Result;
import com.chun.reggie.dto.SetmealDto;
import com.chun.reggie.entity.Setmeal;
import com.chun.reggie.entity.SetmealDish;
import com.chun.reggie.mapper.SetmealMapper;
import com.chun.reggie.service.SetmealDishService;
import com.chun.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * New combo created
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //Basic dish information
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //Save dish and combo, on the table setmeal_dish, insert
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * Delete and multi-delete
     * There are two tables, so we need @Transactional
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {

        //Check status
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        //whether delete?
        if(count > 0){
            //Can't delete,throw customer exception
            throw new CustomException("Combo on selling, can't be deleted!");

        }

        //delete setmeal
        this.removeByIds(ids);


        //delete setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        //remove it
        setmealDishService.remove(lambdaQueryWrapper);


    }



    /**
     * Update status of combo
     * @param ids
     */
    public Result<String> updateMealStatus(@PathVariable int status, String[] ids) {
        //Check status
//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.in(Setmeal::getId,ids);
        for(String id: ids){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }

        return Result.success("Update status successfully!");


    }

    /**
     * Modify info of combo
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //Check basic information of combo, setmeal_dish
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //Check info of Dish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());

        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表基本信息
        this.updateById(setmealDto);

        //更新setmeal_dish表信息delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //更新setmeal_dish表信息insert操作
        List<SetmealDish> SetmealDishes = setmealDto.getSetmealDishes();

        SetmealDishes = SetmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(SetmealDishes);
    }


}
