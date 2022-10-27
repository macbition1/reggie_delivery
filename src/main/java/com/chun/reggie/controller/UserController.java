package com.chun.reggie.controller;


import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chun.reggie.common.Result;
import com.chun.reggie.entity.User;
import com.chun.reggie.service.UserService;
import com.chun.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {


    @Autowired
    private UserService userService;

    //save the msg into redis rather than HttpSession
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Send validate code
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user,  HttpSession session){

        //Get the phone number
        String phone = user.getPhone();
        if(!StringUtils.isEmpty(phone)){
            //Generates 4 bits validateCode
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);

//          SMSUtils.sendMessage("Reggie take-out", "", phone, code);

            //Map key is phone, value is code
//            session.setAttribute(phone,code);

            //Save msg code into redis, and keep it 5 mins
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return Result.success("Phone validatedCode sends success!!!");
        }
        return Result.error("Send code message error!");
    }


    /**
     * Front login
     * @param map
     * @param session
     * replace HttpSession session
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session){

        log.info(map.toString());

        //Get phone, also UserDto is fine
        String phone = map.get("phone").toString();


        //Get code
        String code = map.get("code").toString();

        //get msg code from session
//        Object codeInSession = session.getAttribute(phone);

        /*From redis get the validated code*/
        Object codeInSession = redisTemplate.opsForValue().get(phone);


        //Compare the code upgraded by page and stored in session
        if (codeInSession != null && codeInSession.equals(code)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if(user == null){
                //If user in current is  a new one?
                //if not a new one, create it
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);

            }

            //User exists
            session.setAttribute("user", user.getId());

            //delete code in redis if login
            redisTemplate.delete(phone);

            return Result.success(user);

        }

        return Result.error("Login error!");
    }

    @PostMapping("/loginout")
    public Result<String> loginOut(HttpServletRequest request){

        //clean the user id in session
        request.getSession().removeAttribute("user");
        return Result.success("Login out successfully");
    }



}
