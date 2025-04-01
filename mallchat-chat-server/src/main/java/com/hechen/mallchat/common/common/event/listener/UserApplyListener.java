package com.hechen.mallchat.common.common.event.listener;

import com.hechen.mallchat.common.common.event.UserApplyEvent;
import com.hechen.mallchat.common.user.dao.UserApplyDao;
import com.hechen.mallchat.common.user.domain.entity.UserApply;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSFriendApply;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class UserApplyListener {
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private WebSocketService webSocketService;

//    @Autowired
//    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = UserApplyEvent.class, fallbackExecution = true)
    public void notifyFriend(UserApplyEvent event) {
        UserApply userApply = event.getUserApply();
        Integer unReadCount = userApplyDao.getUnReadCount(userApply.getTargetId());
        log.info("未读数为：{}",unReadCount);
//        pushService.sendPushMsg(WSAdapter.buildApplySend(new WSFriendApply(userApply.getUid(), unReadCount)), userApply.getTargetId());
    }

}