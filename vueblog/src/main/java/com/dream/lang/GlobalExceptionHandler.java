package com.dream.lang;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED) //因为前后端分离 返回一个状态 一般是401 没有权限
    @ExceptionHandler(value =  ShiroException.class)//捕获运行时异常ShiroException是大部分异常的父类
    public Result handler(ShiroException e){
        return new Result(401,e.getMessage(),null);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED) //因为前后端分离 返回一个状态 一般是401 没有权限
    @ExceptionHandler(value = UnauthenticatedException.class)//捕获运行时异常ShiroException是大部分异常的父类
    public Result handler(UnauthenticatedException e){
        return new Result(401,"token已过期,请登录后进行操作",null);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST) //因为前后端分离 返回一个状态
    @ExceptionHandler(value =  RuntimeException.class)//捕获运行时异常
    public Result handler(RuntimeException e){
        return Result.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = UnauthorizedException.class)
    public Result handler(UnauthorizedException e){
        return new Result(403,"你没有权限！",null);
    }



}
