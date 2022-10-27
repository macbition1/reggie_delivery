package com.chun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chun.reggie.common.Result;
import com.chun.reggie.entity.Employee;
import com.chun.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {


    //spring injects EmployeeService when EmployeeController created, autowire relationships between collaborating beans
    @Autowired
    private EmployeeService employeeService;


    /**Employee login
    * @param request
    * @param employee
    * @return
    *
    */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.encrypt password using md5
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2. according username to search database
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        //from database
        Employee emp = employeeService.getOne(queryWrapper);

        // 3. if username in the database continue step4 otherwise failed

        if(emp == null){
            return Result.error("User not exists!!!");
        }

        //4. compare password

        if(!emp.getPassword().equals(password)){
            return Result.error("Password errors!!!");
        }

        //5. if true, check the status of this employee is locked or not
        if(emp.getStatus() == 0){
            return Result.error("Account locked!!!");
        }
        //6.login success, save employee id into the session return result

        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }



    /** Employee logout method
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        //1. clean session employee id...
        request.getSession().removeAttribute("employee");

        //2.exit
        return Result.success("Logout success!!!");


    }


    /**
     * ADD A NEW EMPLOYEE
     * @param employee
     * @return
     *
     * */
    @PostMapping
    public Result<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("Add a new employee: {}", employee.toString());

        //Add a initial password using md5 encrypt
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //Get current manager id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return Result.success("Add new employee!");
    }


    /**
     * Employee information paginate search
     * @param page
     * @param pageSize
     * @param name
     * @return
     * */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize,String name){
        log.info("page ={},pageSize={},name={}",page,pageSize,name);

        //1.pagination constructor
        Page pageInfo = new Page(page,pageSize);

        //2.condition constructor
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        //add filter
        queryWrapper.like(StringUtils.hasText(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //4..running search
        employeeService.page(pageInfo,queryWrapper);


        return Result.success(pageInfo);
    }




    /**
     * @param employee
     * */
    @PutMapping
    public Result<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        //check current thread id
        long id = Thread.currentThread().getId();
        log.info("Current thread id is {}", id);

//        Long empId =(Long) request.getSession().getAttribute("employee");

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);

        employeeService.updateById(employee);
        return Result.success("Update employee information success!");
    }


    /**
     * Search employee information by id
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("Search employee info by id");
        Employee employee = employeeService.getById(id);

        if(employee != null) {
            return Result.success(employee);
        }
        return Result.error("Employee not exists!");
    }

}
