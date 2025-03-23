package com.hechen.mallchat.common.common.exception;

import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ClassName: GlobalExceptionHandler
 * Package: com.hechen.mallchat.common.common.exception
 * Description:全局异常捕获类
 *
 * @Author 何琛
 * @Create 2025/3/21 14:06
 *
 * @Version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //valid注解框架中校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e){
        StringBuilder errorMsg=new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(x->errorMsg.append(x.getField()).append(x.getDefaultMessage()).append(","));
        String message = errorMsg.toString();
        log.error(message);
        return ApiResult.fail(CommonErrorEnum.PARAM_INVALID.getCode(),message.substring(0,message.length()-1));
    }

    //业务异常
    @ExceptionHandler(value = BusinessException.class)
    public ApiResult<?> businessException(BusinessException e){
        log.info("business exception! The reason is:{}",e.getMessage());
        return ApiResult.fail(e.getErrorCode(),e.getErrorMsg());

    }

    //系统异常也即语法 或者数据库一类的错误 ，严重错误
    @ExceptionHandler(value = Throwable.class)
    public ApiResult<?> throwable(Throwable e){
        log.error("system exception! The reason is:{}",e.getMessage());
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }

}
