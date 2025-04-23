package com.hechen.mallchat.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.hechen.mallchat.common.chat.dao.*;
import com.hechen.mallchat.common.chat.domain.entity.*;
import com.hechen.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessageBaseReq;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessagePageReq;
import com.hechen.mallchat.common.chat.domain.vo.req.ChatMessageReq;
import com.hechen.mallchat.common.chat.domain.vo.resp.ChatMemberStatisticResp;
import com.hechen.mallchat.common.chat.domain.vo.resp.ChatMessageResp;
import com.hechen.mallchat.common.chat.service.ChatService;
import com.hechen.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hechen.mallchat.common.chat.service.cahce.RoomCache;
import com.hechen.mallchat.common.chat.service.cahce.RoomGroupCache;
import com.hechen.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hechen.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hechen.mallchat.common.chat.service.strategy.msg.RecallMsgHandler;
import com.hechen.mallchat.common.common.domain.enums.NormalOrNoEnum;
import com.hechen.mallchat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.hechen.mallchat.common.common.event.MessageSendEvent;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Autowired
    private UserCache userCache;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private RecallMsgHandler recallMsgHandler;

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
        //todo 这里没有查出回复的消息 通过message的replyMsgId来查

        //查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, msgMark, receiveUid);
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long receiveUid) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, receiveUid);
    }

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        System.out.println(Thread.currentThread().getName());
        Long onlineNum = userCache.getOnlineNum();
//        Long offlineNum = userCache.getOfflineNum();不展示总人数
        ChatMemberStatisticResp resp = new ChatMemberStatisticResp();
        resp.setOnlineNum(onlineNum);
//        resp.setTotalNum(onlineNum + offlineNum);
        return resp;
    }

    //游标翻页查询
    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long receiveUid) {
        //用最后一条消息id，来限制被踢出的人能看见的最大一条消息
        Long lastMsgId = getLastMsgId(request.getRoomId(), receiveUid);
        CursorPageBaseResp<Message> cursorPage = messageDao.getCursorPage(request.getRoomId(), request, lastMsgId);
        if (cursorPage.isEmpty()){
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPage, getMsgRespBatch(cursorPage.getList(), receiveUid));
    }

    private Long getLastMsgId(Long roomId, Long receiveUid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            return null;
        }
        AssertUtil.isNotEmpty(receiveUid, "请先登录");
        Contact contact = contactDao.get(receiveUid, roomId);
        return contact.getLastMsgId();
    }
    //撤回消息
    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = messageDao.getById(request.getMsgId());
        //校验能不能执行撤回
        checkRecall(uid,message);
        //执行消息撤回
        recallMsgHandler.recall(uid,message);



    }
    private void checkRecall(Long uid,Message message){
        AssertUtil.isNotEmpty(message,"消息有误");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(),"消息已经撤回");
        //判断当前用户是否有资格进行撤回
        boolean hasPower = roleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
        if (hasPower)
            return;
        //如果没有管理员权限则判断是不是自己的消息，不是自己的消息不能撤回
        boolean self = Objects.equals(uid, message.getFromUid());
        AssertUtil.isTrue(self,"抱歉，你没有权限哦");
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between<2,"超过2分钟以上的消息不能进行撤回哦");

    }
}
