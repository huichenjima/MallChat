package com.hechen.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : limeng
 * @description : 申请类型枚举
 * @date : 2023/07/20
 */
@Getter
@AllArgsConstructor
public enum DeleteStatusEnum {

    Normal(0, "保持好友关系"),
    Delete(1,"删除好友关系");


    private final Integer code;

    private final String desc;
}
