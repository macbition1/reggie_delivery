package com.chun.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chun.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper

//BaseMapper provided by mybatis
public interface EmployeeMapper extends BaseMapper<Employee> {


}
