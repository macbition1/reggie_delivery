package com.chun.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Food menu
 */

@Data
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //name of meal
    private String name;

    //category id
    private Long categoryId;

    //price
    private BigDecimal price;

    //code
    private String code;

    //picture
    private String image;

    //information
    private String description;

    //0 not sell, 1 sell
    private Integer status;

    //order
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //delete
    private Integer isDeleted;

}
