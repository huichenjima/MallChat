package com.hechen.mallchat.common.common.event.listener;


import com.hechen.mallchat.common.chat.domain.dto.ChatMsgRecallDTO;
import com.hechen.mallchat.common.chat.service.ChatService;
import com.hechen.mallchat.common.chat.service.cahce.MsgCache;
import com.hechen.mallchat.common.common.event.MessageRecallEvent;
import com.hechen.mallchat.common.user.service.adapter.WSAdapter;
import com.hechen.mallchat.common.user.service.impl.PushService;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 消息撤回监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class MessageRecallListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private PushService pushService;

    //更新消息缓存
    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void evictMsg(MessageRecallEvent event) {
        ChatMsgRecallDTO recallDTO = event.getRecallDTO();
        msgCache.evictMsg(recallDTO.getMsgId());
    }

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void sendToAll(MessageRecallEvent event) {
        //对所有群成员发送撤回消息
        pushService.sendPushMsg(WSAdapter.buildMsgRecall(event.getRecallDTO()));
    }

}
