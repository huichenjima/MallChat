package com.hechen.mallchat.common.user.service.impl;

import com.hechen.mallchat.common.common.annotation.RedissonLock;
import com.hechen.mallchat.common.common.event.ItemReceiveEvent;
import com.hechen.mallchat.common.common.service.LockService;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.user.dao.UserBackpackDao;
import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.hechen.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.service.IUserBackpackService;
import com.hechen.mallchat.common.user.service.cache.ItemCache;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


/**
 * ClassName: UserBackpackServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 21:14
 * @Version 1.0
 */
@Service
public class UserBackpackServiceImpl implements IUserBackpackService {
    @Autowired
    UserBackpackDao userBackpackDao;

//    @Autowired 不使用redssion锁，使用自己写的Lock服务，里面实现了redisson的模板
//    RedissonClient redissonClient;
    @Autowired
    LockService lockService;

    @Autowired
    private ItemCache itemCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @Lazy
    private UserBackpackServiceImpl userBackpackService; //解决事务失效问题

//    //编程式redisson锁实现版本
//    @Override
//    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String buinessId) {
//        String idempotent = getIdempotent(itemId, idempotentEnum, buinessId);
//        lockService.executeWithLock("acquireItem"+idempotent, new LockService.Supplier<Boolean>() {
//            @Override
//            public Boolean get() {
//                //分布式锁获取成功，先进行判断是否已经存在该幂等性物品 即幂等性检查
//                UserBackpack userBackpack=userBackpackDao.getByIdempotent(idempotent);
//                AssertUtil.isEmpty(userBackpack,"已经在该渠道发过了该物品了");
//                //判断没有在该渠道发过该物品，下面进行发放，对数据库进行更新
//                boolean result=userBackpackDao.acquireItem(uid,itemId,idempotent);
//                return result;
//            }
//        });
//
//
//
//
//    }
//注解式redisson锁实现版本
@Override
public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String buinessId) {
    String idempotent = getIdempotent(itemId, idempotentEnum, buinessId); //获取幂等号
    userBackpackService.doAcquireItem(uid,itemId,idempotent);




}
    @Transactional
    @RedissonLock(prefixKey = "acquireItem",key="#idempotent",waitTime = 5000)//注解配合aop实现redisson分布式锁
    public void doAcquireItem(Long uid, Long itemId,String idempotent){
                //分布式锁获取成功，先进行判断是否已经存在该幂等性物品 即幂等性检查
                UserBackpack userBackpack=userBackpackDao.getByIdempotent(idempotent);
                AssertUtil.isEmpty(userBackpack,"已经在该渠道发过了该物品了");
                //业务检查
                ItemConfig itemConfig = itemCache.getById(itemId); //从缓存取徽章信息
                if (ItemTypeEnum.BADGE.getType().equals(itemConfig.getType())) {//徽章类型做唯一性检查
                    Integer countByValidItemId = userBackpackDao.getCountByValidItemId(uid, itemId);
                        if (countByValidItemId > 0) {//已经有徽章了不发
                            return;
                                        }
                }
                //判断没有在该渠道发过该物品，下面进行发放，对数据库进行更新
                UserBackpack userBackpack1 = userBackpackDao.acquireItem(uid, itemId, idempotent);
                //用户收到物品的事件 ,监听器给他自动佩戴徽章并且更新缓存
                applicationEventPublisher.publishEvent(new ItemReceiveEvent(this, userBackpack1));



    }

    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String buinessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), buinessId);


    }

}
