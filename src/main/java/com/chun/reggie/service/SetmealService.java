package com.chun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chun.reggie.common.Result;
import com.chun.reggie.dto.SetmealDto;
import com.chun.reggie.entity.Setmeal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {


    /**
     * New combo created
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);


    /**
     * Delete and multi-delete
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * Update status of combo
     * @param ids
     */
    public Result<String> updateMealStatus(@PathVariable int status, String[] ids);

    SetmealDto getByIdWithDish(Long id);


    void updateWithDish(SetmealDto setmealDto);
}
