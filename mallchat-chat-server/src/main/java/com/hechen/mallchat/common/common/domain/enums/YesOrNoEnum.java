package com.hechen.mallchat.common.common.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static Map<Integer, YesOrNoEnum> cache;

    static {
        cache = Arrays.stream(YesOrNoEnum.values()).collect(Collectors.toMap(YesOrNoEnum::getStatus, Function.identity()));
    }

    public static YesOrNoEnum of(Integer type) {
        return cache.get(type);
    }

    public static Integer toStatus(Boolean bool) {
        return bool ? YES.getStatus() : NO.getStatus();
    }
}
