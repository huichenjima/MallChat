package com.hechen.mallchat.common.common.event.listener;

import com.hechen.mallchat.common.common.event.UserBlackEvent;
import com.hechen.mallchat.common.common.event.UserOnlineEvent;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.hechen.mallchat.common.user.domain.enums.UserActiveStatusEnum;
import com.hechen.mallchat.common.user.service.IpService;
import com.hechen.mallchat.common.user.service.UserService;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import com.hechen.mallchat.common.user.service.handler.MsgHandler;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ClassName: UserRegisterListener
 * Package: com.hechen.mallchat.common.common.event.listener
 * Description:用户拉黑事件监听者 也即消费者
 *
 * @Author 何琛
 * @Create 2025/3/22 15:07
 * @Version 1.0
 */
@Component
public class UserBlackListener {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;
    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserCache userCache;

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class,phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true) //设置事务提交后执行 fallbackExecution 保证原方法没有事务监听也不失效
    public void sendMsg(UserBlackEvent event){
        //发送消息通知所有用户的前端 xx用户被拉黑了 ，前端会删除所有有关该拉黑用户的消息不进行展示
        User user = event.getUser();
        webSocketService.sendMsgToAll(WebSocketAdapter.buildBlack(user));



    }

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class,phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true) //设置事务提交后执行 fallbackExecution 保证原方法没有事务监听也不失效
    public void changeUserStatus(UserBlackEvent event){
        //更新数据库中的拉黑用户状态
        userDao.invalidUid(event.getUser().getId());

    }

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class,phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true) //设置事务提交后执行 fallbackExecution 保证原方法没有事务监听也不失效
    public void evictCache(UserBlackEvent event){
        //因为黑名单被更新了，清除缓存
        userCache.evictBlackMap();



    }



}
