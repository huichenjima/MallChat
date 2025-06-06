package com.hechen.mallchat.common.user.service;

/**
 * ClassName: LoginService
 * Package: com.hechen.mallchat.common.user.service
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/18 10:36
 * @Version 1.0
 */
public interface LoginService {


    /**
     * 校验token是不是有效
     *
     * @param token
     * @return
     */
    boolean verify(String token);

    /**
     * 刷新token有效期
     *
     * @param token
     */
    void renewalTokenIfNecessary(String token);

    /**
     * 登录成功，获取token
     *
     * @param uid
     * @return 返回token
     */
    String login(Long uid);

    /**
     * 如果token有效，返回uid
     *
     * @param token
     * @return
     */
    Long getValidUid(String token);

}
