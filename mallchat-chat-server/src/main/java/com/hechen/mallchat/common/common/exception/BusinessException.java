package com.hechen.mallchat.common.common.exception;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * ClassName: BusinessException
 * Package: com.hechen.mallchat.common.common.exception
 * Description:业务异常
 *
 * @Author 何琛
 * @Create 2025/3/21 15:00
 * @Version 1.0
 */
@Data
public class BusinessException extends RuntimeException{
    protected Integer errorCode;
    protected String errorMsg;

    public BusinessException(String errorMsg){
        super(errorMsg);
        this.errorMsg=errorMsg;
        this.errorCode=CommonErrorEnum.BUSINESS_ERROR.getErrorCode();
    }
    public BusinessException(Integer errorCode,String errorMsg)
    {
        super(errorMsg);
        this.errorCode=errorCode;
        this.errorMsg=errorMsg;
    }
}
