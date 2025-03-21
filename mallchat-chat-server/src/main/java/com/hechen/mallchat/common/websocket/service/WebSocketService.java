package com.hechen.mallchat.common.websocket.service;

import com.hechen.mallchat.common.websocket.domain.vo.resp.WSBaseResp;
import io.netty.channel.Channel;

/**
 * ClassName: WebSocketService
 * Package: com.hechen.mallchat.common.websocket.service
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 13:16
 * @Version 1.0
 */
public interface WebSocketService {
    void connect(Channel channel);

    void handleLoginReq(Channel channel);

    void remove(Channel channel);

    void scanLoginAndAuthorizeSuccess(Integer code, Long id);

    void sendMsg(Channel channel, WSBaseResp<?> resp);

    void waitAuthorize(Integer code);

    void authorize(Channel channel, String token);
}
