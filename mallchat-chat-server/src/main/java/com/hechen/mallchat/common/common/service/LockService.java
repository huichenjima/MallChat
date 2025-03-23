package com.hechen.mallchat.common.common.service;

import com.hechen.mallchat.common.common.exception.BusinessException;
import com.hechen.mallchat.common.common.exception.CommonErrorEnum;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * ClassName: LockService
 * Package: com.hechen.mallchat.common.common.service
 * Description:分布式锁service
 *
 * @Author 何琛
 * @Create 2025/3/21 21:48
 * @Version 1.0
 */
@Service
public class LockService {
    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows //有等待时间的reddison锁
    public <T> T executeWithLock(String key, int waitTime, TimeUnit timeUnit, Supplier<T> supplier){
        RLock lock = redissonClient.getLock(key);
        boolean success = lock.tryLock(waitTime, timeUnit);
        if(!success)
        {
            throw new BusinessException(CommonErrorEnum.LOCK_LIMIT);
        }
        try {
            T t = supplier.get();
            return t;
        }finally {
            //解锁
            lock.unlock();
        }

    }

    @SneakyThrows //没有等待时间的reddison锁，失败直接返回
    public <T> T executeWithLock(String key, Supplier<T> supplier){
        return executeWithLock(key,-1,TimeUnit.MILLISECONDS,supplier);

    }


    @FunctionalInterface
    public interface Supplier<T>  {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }



}
