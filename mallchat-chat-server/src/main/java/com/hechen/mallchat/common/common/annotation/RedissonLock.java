package com.hechen.mallchat.common.common.annotation;

import lombok.Builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RedissonLock
 * Package: com.hechen.mallchat.common.common.annotation
 * Description: 分布式锁注解式实现方式
 *
 * @Author 何琛
 * @Create 2025/3/22 12:55
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME) //运行时生效
@Target(ElementType.METHOD) //方法上生效
public @interface RedissonLock {
    //获取锁的key的前缀
    String prefixKey() default "";
    //支持springEL表示式
    String key();
    //默认不等待，失败直接拒绝
    int waitTime() default -1;
    //等待默认时间单位，默认为毫秒
    TimeUnit unit() default TimeUnit.MILLISECONDS;



}
