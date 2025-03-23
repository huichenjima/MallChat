package com.hechen.mallchat.common.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: CommonErrorEnum
 * Package: com.hechen.mallchat.common.common.exception
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 14:21
 * @Version 1.0
 */
@AllArgsConstructor
@Getter
public enum CommonErrorEnum implements ErrorEnum{
    BUSINESS_ERROR(0,"{0}"),
    PARAM_INVALID(-2,"参数校验失败"),
    SYSTEM_ERROR(-1,"系统出小差了，请稍后再试哦~~"),
    LOCK_LIMIT(-3,"请求太频繁了，请稍后再试"),
    ;
    private final Integer code;
    private final String msg;


    @Override
    public Integer getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}
