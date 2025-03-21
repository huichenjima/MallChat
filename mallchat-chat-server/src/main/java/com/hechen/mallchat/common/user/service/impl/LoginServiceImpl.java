package com.hechen.mallchat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hechen.mallchat.common.common.constant.RedisKey;
import com.hechen.mallchat.common.common.utils.JwtUtils;
import com.hechen.mallchat.common.common.utils.RedisUtils;
import com.hechen.mallchat.common.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: LoginServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/18 10:36
 * @Version 1.0
 */
@Service
public class LoginServiceImpl implements LoginService {
    public static final int TOKEN_EXPIRE_DAYS = 3; //设置过期时间
    public static final int TOKEN_RENEWAL_DAYS = 1; //重新刷新过期时间的界限
    @Autowired
    private JwtUtils jwtUtils;

    /*
    校验token是否有效
     */
    @Override
    public boolean verify(String token) {
        return false;
    }
    /*
    重新刷新token有效期
     */
    @Override
    @Async //等同于线程池 ，异步调用
    public void renewalTokenIfNecessary(String token) {
        Long uid = getValidUid(token);
        String userTokenKey=getUserTokenKey(uid);
        Long expireDays = RedisUtils.getExpire(userTokenKey, TimeUnit.DAYS);
        if (expireDays==-2)
        {
            //不存在的key
            return;
        }
        else if(expireDays< TOKEN_RENEWAL_DAYS){
            RedisUtils.expire(getUserTokenKey(uid),TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        }


    }
    /*
    如果token有效，解码jwt获得uid,判断refis中的token是否过期
     */
    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if(Objects.isNull(uid))
            return null;
        String oldtoken= RedisUtils.getStr(getUserTokenKey(uid));
        if(StrUtil.isBlank(oldtoken))
            return null;
        return Objects.equals(token,oldtoken)?uid:null;
    }
    /*
    登录成功，返回token
     */
    @Override
    public String login(Long uid) {
        //放进redis
        String token = jwtUtils.createToken(uid);
        RedisUtils.set(getUserTokenKey(uid),token,TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        return token;

    }

    private String getUserTokenKey(Long uid){
        return RedisKey.getKey(RedisKey.USER_TOKEN_STRING,uid);
    }
}
