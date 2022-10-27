package com.chun.reggie.dto;



import com.chun.reggie.entity.Setmeal;
import com.chun.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
