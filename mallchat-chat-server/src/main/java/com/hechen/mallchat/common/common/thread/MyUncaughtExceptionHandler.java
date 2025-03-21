package com.hechen.mallchat.common.common.thread;

import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: MyUncaughtExceptionHandler
 * Package: com.hechen.mallchat.common.common.thread
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/18 16:51
 * @Version 1.0
 */
@Slf4j
public class MyUncaughtExceptionHandler implements  Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread"+t.getName(),e);
    }
}
