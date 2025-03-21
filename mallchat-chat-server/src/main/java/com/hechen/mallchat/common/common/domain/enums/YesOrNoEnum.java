package com.hechen.mallchat.common.common.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: YesOrNoEnum
 * Package: com.hechen.mallchat.common.common.domain.enums
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 12:14
 * @Version 1.0
 */
@AllArgsConstructor
@Getter
public enum YesOrNoEnum {
    NO(0,"否"),
    YES(1,"是"),
    ;
    private final Integer status;
    private final String desc;
}
