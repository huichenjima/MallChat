package com.hechen.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.common.value.qual.ArrayLen;

/**
 * ClassName: IdepotentEnum
 * Package: com.hechen.mallchat.common.user.domain.enums
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 21:08
 * @Version 1.0
 */
@AllArgsConstructor
@Getter
public enum IdempotentEnum {
    UID(1,"uid"),
    MSG_ID(2,"消息id"),
    ;
    private final Integer type;
    private final String desc;

}
