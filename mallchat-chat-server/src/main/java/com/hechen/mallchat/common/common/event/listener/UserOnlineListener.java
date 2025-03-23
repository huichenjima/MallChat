package com.hechen.mallchat.common.common.event.listener;

import com.hechen.mallchat.common.common.event.UserOnlineEvent;
import com.hechen.mallchat.common.common.event.UserRegisterEvent;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemEnum;
import com.hechen.mallchat.common.user.domain.enums.UserActiveStatusEnum;
import com.hechen.mallchat.common.user.service.IUserBackpackService;
import com.hechen.mallchat.common.user.service.IpService;
import com.hechen.mallchat.common.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ClassName: UserRegisterListener
 * Package: com.hechen.mallchat.common.common.event.listener
 * Description:用户注册时间监听者 也即消费者
 *
 * @Author 何琛
 * @Create 2025/3/22 15:07
 * @Version 1.0
 */
@Component
public class UserOnlineListener {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;
    @Autowired
    private IpService ipService;

    @Async
    @TransactionalEventListener(classes = UserOnlineEvent.class,phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true) //设置事务提交后执行 fallbackExecution 保证原方法没有事务监听也不失效
    public void saveDB(UserOnlineEvent event){
        //更新数据库中的用户信息
        User user = event.getUser();
        User update = User.builder().id(user.getId())
                .lastOptTime(user.getLastOptTime())
                .ipInfo(user.getIpInfo())
                .activeStatus(UserActiveStatusEnum.ONLINE.getType()).build();
        boolean b = userDao.updateById(update);
        //更新完后进行ip详情的解析，这里要使用淘宝的接口
        ipService.refreshIpDetailAsync(user.getId());



    }



}
