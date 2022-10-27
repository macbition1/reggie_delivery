package com.chun.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chun.reggie.entity.User;
import com.chun.reggie.mapper.UserMapper;
import com.chun.reggie.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
