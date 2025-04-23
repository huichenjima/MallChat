package com.hechen.mallchat.common.common.event.listener;

import com.hechen.mallchat.common.common.event.UserOfflineEvent;
import com.hechen.mallchat.common.common.event.UserOnlineEvent;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.UserActiveStatusEnum;
import com.hechen.mallchat.common.user.service.IpService;
import com.hechen.mallchat.common.user.service.UserService;
import com.hechen.mallchat.common.user.service.adapter.WSAdapter;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ClassName: UserOfflineListener
 * Package: com.hechen.mallchat.common.common.event.listener
 * Description:
 *
 * @Author 何琛
 * @Create 2025/4/23 10:48
 * @Version 1.0
 */
@Component
public class UserOfflineListener {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCache userCache;

    @Autowired
    WebSocketService webSocketService;

    @Autowired
    private WSAdapter wsAdapter; //构造消息的转换器，这个涉及了很多消息的处理，最后都转成了websocket的设置返回格式 WSBaseResp

    @Async
    @TransactionalEventListener(classes = UserOfflineEvent.class,phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true) //设置事务提交后执行 fallbackExecution 保证原方法没有事务监听也不失效
    public void saveDB(UserOfflineEvent event){
        //更新数据库中的用户信息
        User user = event.getUser();
        User update = User.builder().id(user.getId())
                .lastOptTime(user.getLastOptTime())
                .activeStatus(UserActiveStatusEnum.OFFLINE.getType()).build();
        boolean b = userDao.updateById(update);
    }

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        //更新redis缓存
        userCache.offline(user.getId(), user.getLastOptTime());
        //推送给所有在线用户，该用户下线，不推给自己，因为多端的其他并没有下线
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyResp(event.getUser()), event.getUser().getId());
    }
}
