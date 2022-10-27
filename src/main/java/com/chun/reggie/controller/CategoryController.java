package com.chun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chun.reggie.common.Result;
import com.chun.reggie.entity.Category;
import com.chun.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category manage
 */

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<String> save(@RequestBody Category category){
        log.info("Category:{}", category);
        categoryService.save(category);
        return Result.success("New Category created!");

    }


    /**
     * Page search
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize){


        //Pagination constructor
        Page<Category> pageInfo = new Page<>(page, pageSize);

        //condition constructor
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //sort
        queryWrapper.orderByAsc(Category::getSort);

        //doing pagination
        categoryService.page(pageInfo,queryWrapper);

        return Result.success(pageInfo);
    }


    /**
     * According id to delete meal category
     * @param id
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long id) {
        log.info("Delete category, id is {}", id);
        categoryService.remove(id);

        return Result.success("Category deleted successfully!");
    }


    /**
     * According id to edit information
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category){
        log.info("Edit information: {}",category);
        categoryService.updateById(category);
        return Result.success("Edit category information successfully!");

    }


    /**
     * According type the search category
     * list(String type) is not so good than category
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        //condition constructor
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();

        //Add condition
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());

        //Sort
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);


        return Result.success(list);

    }


}
