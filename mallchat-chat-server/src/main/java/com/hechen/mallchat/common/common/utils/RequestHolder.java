package com.hechen.mallchat.common.common.utils;

import com.hechen.mallchat.common.common.domain.dto.RequestInfo;

/**
 * ClassName: RequestHolder
 * Package: com.hechen.mallchat.common.common.utils
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 11:35
 * 通常用于存储和管理与当前请求相关的数据。它主要用来确保数据的线程安全，即使在并发环境中，每个线程都可以独立地访问和操作与自己处理的请求相关的数据，而不会影响到其他线程。
 * 请求上下文
 * @Version 1.0
 */
public class RequestHolder {
    private static final ThreadLocal<RequestInfo> threadLocal=new ThreadLocal<RequestInfo>();

    public static void set(RequestInfo requestInfo){
        threadLocal.set(requestInfo);
    }
    public static RequestInfo get(){
        return threadLocal.get();
    }
    public static void remove(){
        //把当前线程的用户信息移出防止内存泄露
        threadLocal.remove();
    }



}
