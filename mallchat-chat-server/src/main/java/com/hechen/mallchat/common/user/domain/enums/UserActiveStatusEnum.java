package com.hechen.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: UserActiveStatusEnum
 * Package: com.hechen.mallchat.common.user.domain.enums
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/22 17:07
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum UserActiveStatusEnum {
    ONLINE(1,"在线"),
    OFFLINE(2,"离线"),
    ;
    private final Integer type;
    private final String desc;



}
