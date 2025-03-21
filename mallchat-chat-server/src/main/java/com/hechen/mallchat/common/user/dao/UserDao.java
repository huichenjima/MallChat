package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-14
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {

    public User getByOpenId(String openId) {
        User user = lambdaQuery().eq(User::getOpenId, openId).one();
        return user;
    }

    public User getByName(String name){
        User user = lambdaQuery().eq(User::getName,name).one();
        return user;
    }
}
