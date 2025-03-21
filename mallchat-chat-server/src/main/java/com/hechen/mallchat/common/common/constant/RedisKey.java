package com.hechen.mallchat.common.common.constant;

/**
 * ClassName: RedisKey
 * Package: com.hechen.mallchat.common.common.constant
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/18 14:56 统一管理redis的key
 * @Version 1.0
 */
public class RedisKey {
    public static  final String BASE_KEY="mallchat:chat";
    /*
     *用户token的key
     */
    public static  final  String USER_TOKEN_STRING="userToken:uid_%d";

    public static  String getKey(String key,Object... o){
        return BASE_KEY+String.format(key,o);

    }
}
