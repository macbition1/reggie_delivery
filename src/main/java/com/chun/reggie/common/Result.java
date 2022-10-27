package com.chun.reggie.common;


import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/*
* ALl the result of service return will wrapped as this class
*and return to the front
* @param<T>
* */
@Data
public class Result<T> implements Serializable {
    private Integer code; //1 or 0 login success or fail
    private String msg; //error message
    private T data; //employee data
    private Map map = new HashMap(); // dynamic data

    public static <T> Result<T> success(T object) {
        Result<T> r = new Result<T>();
        r.data =object;
        r.code = 1;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result r = new Result();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public Result<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
