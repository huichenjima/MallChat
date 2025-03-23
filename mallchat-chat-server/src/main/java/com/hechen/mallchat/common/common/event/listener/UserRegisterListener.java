package com.hechen.mallchat.common.common.event.listener;

import com.hechen.mallchat.common.common.event.UserRegisterEvent;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.service.IUserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.swing.border.EmptyBorder;

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
public class UserRegisterListener {
    @Autowired
    private IUserBackpackService userBackpackService;

    @Autowired
    private UserDao userDao;

    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class,phase = TransactionPhase.AFTER_COMMIT) //设置事务提交后执行
    public void sendCard(UserRegisterEvent event){
        User user = event.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID,""+user.getId());



    }
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class,phase = TransactionPhase.AFTER_COMMIT)
    public void sendBadge(UserRegisterEvent event){
        //发放前100名和前10的注册徽章
        User user = event.getUser();
        int registedCount = userDao.count();
        if (registedCount<10)
        {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(), IdempotentEnum.UID,""+user.getId());
        }else if(registedCount<100)
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID,""+user.getId());



    }
}
