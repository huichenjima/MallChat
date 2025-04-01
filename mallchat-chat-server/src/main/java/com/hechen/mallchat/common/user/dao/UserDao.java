package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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

    //通过微信openid获取用户
    public User getByOpenId(String openId) {
        User user = lambdaQuery().eq(User::getOpenId, openId).one();
        return user;
    }
    //通过名字获取用户
    public User getByName(String name){
        User user = lambdaQuery().eq(User::getName,name).one();
        return user;
    }
    //修改用户名字
    public boolean modifyName(Long uid, String name) {
        return lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, name)
                .update();
    }
    //佩戴徽章即修改itemid的值
    public boolean wearingBadge(Long uid, Long itemId) {
        return lambdaUpdate().eq(User::getId, uid)
                .set(User::getItemId, itemId)
                .update();

    }

    //拉黑用户，即更新用户状态
    public void invalidUid(Long id) {
        boolean update = lambdaUpdate().eq(User::getId, id)
                .set(User::getStatus, YesOrNoEnum.YES.getStatus())
                .update();

    }
    //根据uid列表获取好友信息
    public List<User> getFriendList(List<Long> uids) {
        return lambdaQuery()
                .in(User::getId, uids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();

    }
}
