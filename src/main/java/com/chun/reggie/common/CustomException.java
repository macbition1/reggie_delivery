package com.chun.reggie.common;

/**
 * Define customer task exception
 */

public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
