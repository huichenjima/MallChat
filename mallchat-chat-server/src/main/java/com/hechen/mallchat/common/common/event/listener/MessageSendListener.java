package com.hechen.mallchat.common.common.event.listener;


import com.hechen.mallchat.common.chat.dao.MessageDao;
import com.hechen.mallchat.common.chat.dao.RoomDao;
import com.hechen.mallchat.common.chat.dao.RoomFriendDao;
import com.hechen.mallchat.common.chat.domain.entity.Message;
import com.hechen.mallchat.common.chat.domain.entity.Room;
import com.hechen.mallchat.common.chat.domain.enums.HotFlagEnum;
import com.hechen.mallchat.common.chat.service.ChatService;
import com.hechen.mallchat.common.chat.service.cahce.GroupMemberCache;
import com.hechen.mallchat.common.chat.service.cahce.RoomCache;
import com.hechen.mallchat.common.common.constant.MQConstant;
import com.hechen.mallchat.common.common.domain.dto.MsgSendMessageDTO;
import com.hechen.mallchat.common.common.event.MessageSendEvent;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 消息发送监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class MessageSendListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageDao messageDao;
//    @Autowired
//    private IChatAIService openAIService;
//    @Autowired
//    WeChatMsgOperationService weChatMsgOperationService;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
//    @Autowired
//    private ContactDao contactDao;
//    @Autowired
//    private HotRoomCache hotRoomCache;
    @Autowired
    private MQProducer mqProducer;
    //事务节点必须选择 事务提交前，因为默认是事务提交后，比如下面的微信跟gpt都是事务提交后
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = MessageSendEvent.class, fallbackExecution = true)
    public void messageRoute(MessageSendEvent event) {
        Long msgId = event.getMsgId();
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC, new MsgSendMessageDTO(msgId), msgId);
    }

//    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
//    public void handlerMsg(@NotNull MessageSendEvent event) {
//        Message message = messageDao.getById(event.getMsgId());
//        Room room = roomCache.get(message.getRoomId());
//        if (isHotRoom(room)) {
//            //在里面判断了是否消息中@了AI
//            openAIService.chat(message);
//        }
//    }

    public boolean isHotRoom(Room room) {
        return Objects.equals(HotFlagEnum.YES.getType(), room.getHotFlag());
    }

    /**
     * 给用户微信推送艾特好友的消息通知
     * （这个没开启，微信不让推）
     */
//    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
//    public void publishChatToWechat(@NotNull MessageSendEvent event) {
//        Message message = messageDao.getById(event.getMsgId());
//        if (Objects.nonNull(message.getExtra().getAtUidList())) {
//            weChatMsgOperationService.publishChatMsgToWeChatUser(message.getFromUid(), message.getExtra().getAtUidList(),
//                    message.getContent());
//        }
//    }
}
