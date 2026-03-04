package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    //捕获DuplicateKeyException异常，处理数据库唯一约束异常
    @ExceptionHandler(DuplicateKeyException.class)
    public Result exceptionHandler(DuplicateKeyException ex) {
        System.out.println("捕获到键重复异常!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (ex.getMessage().contains("Duplicate entry")) {
            String message = ex.getMessage();
            int start=message.indexOf("Duplicate");
            String msg = message.substring(start).split(" ")[2];
            return Result.error(msg + "已存在");
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
