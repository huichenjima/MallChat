package com.hechen.mallchat.common.common.exception;

//异常enum的规范
public interface ErrorEnum {

    Integer getErrorCode();

    String getErrorMsg();
}
