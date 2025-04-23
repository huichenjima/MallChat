package com.hechen.mallchat.common.chat.service;

import com.hechen.mallchat.common.chat.domain.entity.Message;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessageBaseReq;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessagePageReq;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessageReq;
import com.hechen.mallchat.common.chat.domain.vo.resp.ChatMemberStatisticResp;
import com.hechen.mallchat.common.chat.domain.vo.resp.ChatMessageResp;
import com.hechen.mallchat.common.common.domain.vo.resp.CursorPageBaseResp;

/**
 * ClassName: ChatService
 * Package: com.hechen.mallchat.common.chat.service
 * Description: 会话服务
 *
 * @Author 何琛
 * @Create 2025/4/2 21:39
 * @Version 1.0
 */
public interface ChatService {

    Long sendMsg(ChatMessageReq request, Long uid);

    ChatMessageResp getMsgResp(Long msgId, Long uid);

    ChatMessageResp getMsgResp(Message message, Long receiveUid);


    ChatMemberStatisticResp getMemberStatistic();

    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long uid);

    void recallMsg(Long uid, ChatMessageBaseReq request);
}
