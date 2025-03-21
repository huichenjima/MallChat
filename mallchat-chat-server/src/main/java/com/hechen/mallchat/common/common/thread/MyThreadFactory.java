package com.hechen.mallchat.common.common.thread;

import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * ClassName: MyThreadFactpry
 * Package: com.hechen.mallchat.common.common.thread
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/18 17:04
 * @Version 1.0
 */
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {
    private static  final MyUncaughtExceptionHandler MY_UNCAUGHT_EXCEPTION_HANDLER=new MyUncaughtExceptionHandler();
    private ThreadFactory original;

    @Override
    public Thread newThread(Runnable r)
    {
        //这是装饰器模式
        Thread thread = original.newThread(r); //生成
        thread.setUncaughtExceptionHandler(MY_UNCAUGHT_EXCEPTION_HANDLER);
        return  thread;

    }
}
