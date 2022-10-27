package com.chun.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chun.reggie.common.BaseContext;
import com.chun.reggie.common.Result;
import com.chun.reggie.entity.AddressBook;
import com.chun.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {


    @Autowired
    private AddressBookService addressBookService;

    /**
     * Add a new address
     * @param addressBook
     * @return
     */
    @PostMapping
    public Result<AddressBook> save(@RequestBody AddressBook addressBook){

        //Get current user id
        addressBook.setUserId(BaseContext.getCurrentId());

        log.info("addressBook: {}", addressBook);

        addressBookService.save(addressBook);

        return Result.success(addressBook);
    }


    /**
     * Set default address
     * @param addressBook
     * @return
     */
    @PutMapping("default")
    public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook){

        LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());

        //all is_default set to 0
        queryWrapper.set(AddressBook::getIsDefault, 0);

        addressBookService.update(queryWrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return Result.success(addressBook);
    }


    /**
     * Search the default address
     * @return
     */
    @GetMapping("default")
    public Result<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);


        //select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if(addressBook == null){
            return Result.error("User not exists");
        }else {
            return Result.success(addressBook);
        }


    }


    /**
     * List all address
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //Condition constructor
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId,addressBook.getUserId());

        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return Result.success(addressBookService.list(queryWrapper));

    }




}


