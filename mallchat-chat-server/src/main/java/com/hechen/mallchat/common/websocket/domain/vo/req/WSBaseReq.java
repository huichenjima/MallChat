package com.hechen.mallchat.common.websocket.domain.vo.req;

import lombok.Data;

/**
 * ClassName: WEBaseReq
 * Package: com.hechen.mallchat.common.websocket.domain.vo.req
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/14 14:01
 * @Version 1.0
 */
@Data
public class WSBaseReq {

    /**
     * @see com.hechen.mallchat.common.websocket.domain.enums.WSReqTypeEnum
     */
    private Integer type;

    private String data;

}
