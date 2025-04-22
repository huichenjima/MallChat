package com.hechen.mallchat.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.hechen.mallchat.common.chat.dao.GroupMemberDao;
import com.hechen.mallchat.common.chat.dao.MessageDao;
import com.hechen.mallchat.common.chat.dao.MessageMarkDao;
import com.hechen.mallchat.common.chat.dao.RoomFriendDao;
import com.hechen.mallchat.common.chat.domain.entity.*;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessageReq;
import com.hechen.mallchat.common.chat.domain.vo.resp.ChatMessageResp;
import com.hechen.mallchat.common.chat.service.ChatService;
import com.hechen.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hechen.mallchat.common.chat.service.cahce.RoomCache;
import com.hechen.mallchat.common.chat.service.cahce.RoomGroupCache;
import com.hechen.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hechen.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hechen.mallchat.common.common.domain.enums.NormalOrNoEnum;
import com.hechen.mallchat.common.common.event.MessageSendEvent;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ChatServiceImpl
 * Package: com.hechen.mallchat.common.chat.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/4/2 21:40
 * @Version 1.0
 */
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private RoomCache roomCache;

    @Autowired
    private RoomGroupCache roomGroupCache;

    @Autowired
    private RoomFriendDao roomFriendDao;

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private MessageMarkDao messageMarkDao;

    //发送消息
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        //判断是否有该会话以及是否被踢出了该会话
        check(request, uid);
        //根据类型通过工程生成对应的消息处理类,检验和保存
        AbstractMsgHandler msgHandler= MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    private void check(ChatMessageReq request, Long uid) {
        Room room = roomCache.get(request.getRoomId());
        if (room.isHotRoom()) {//全员群跳过校验
            return;
        }
        if (room.isRoomFriend()) {
            RoomFriend roomFriend = roomFriendDao.getByRoomId(request.getRoomId());
            //删好友了会禁用该房间，status设置为1，也就是被对方拉黑了
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }

    }

    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }
    public List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long receiveUid) {
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        //查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, msgMark, receiveUid);
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long receiveUid) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, receiveUid);
    }
}
