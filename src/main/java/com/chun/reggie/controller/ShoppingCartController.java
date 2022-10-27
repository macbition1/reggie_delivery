package com.chun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chun.reggie.common.BaseContext;
import com.chun.reggie.common.Result;
import com.chun.reggie.entity.ShoppingCart;
import com.chun.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j

public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * Add shopping cart, including add meal or dish
     * @param shoppingCart
     * @return
     */

    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("ShopingCart data {}", shoppingCart);

        //According user id to make sure which user's shopping_cart
         Long currentId = BaseContext.getCurrentId();
         shoppingCart.setUserId(currentId);

         //Check current dish or meal in cart?
         Long dishId = shoppingCart.getDishId();
         Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if(dishId != null){

            //Add into cart is dish
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else {
            //In cart is meal
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }


        //SQL:select *from shopping_cart where user_id=? and dish_id/setmeal_id =?
         ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null){
            //Cart exists, add 1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //Not exists cart
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            //save current cart
            shoppingCartService.save(shoppingCart);

            cartServiceOne = shoppingCart;
        }
        return Result.success(cartServiceOne);

    }


    /**
     * Show the shopping_cart
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("Show shopping cart");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);

    }

    /**
     * Sub the dish in shopping cart
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

         Long setmealId = shoppingCart.getSetmealId();
         Long dishId = shoppingCart.getDishId();

         LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

         if(setmealId != null){
             queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
         }else {
             queryWrapper.eq(ShoppingCart::getDishId, dishId);
         }

         ShoppingCart one = shoppingCartService.getOne(queryWrapper);
         Integer number  = one.getNumber();

         if(number == 1){
            shoppingCartService.remove(queryWrapper);
         }else {
             one.setNumber(number -1);
             shoppingCartService.updateById(one);

         }

        return Result.success(one);
    }



    @DeleteMapping("/clean")
    public Result<String> clean(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return Result.success("Delete the shopping cart!");
    }

}
