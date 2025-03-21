package com.hechen.mallchat.common.websocket.domain.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: WEBaseResp
 * Package: com.hechen.mallchat.common.websocket.domain.vo.resp
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/14 14:05
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSBaseResp<T> {
    /**
     * @see com.hechen.mallchat.common.websocket.domain.enums.WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
