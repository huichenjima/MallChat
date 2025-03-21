package com.hechen.mallchat.common.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: RequestInfo
 * Package: com.hechen.mallchat.common.common.domain.dto
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 11:36
 * @Version 1.0
 */
@AllArgsConstructor
@Data
public class RequestInfo {
    private Long uid;
    private String ip;
}
