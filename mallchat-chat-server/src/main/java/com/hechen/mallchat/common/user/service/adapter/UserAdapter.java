package com.hechen.mallchat.common.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: UserAdapter
 * Package: com.hechen.mallchat.common.user.service.adapter
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 15:45
 * @Version 1.0
 */
@Component
public class UserAdapter {


    public static User buildUserSave(String openId) {
        return  User.builder().openId(openId).build();
    }

    public static User buildAuthorizeUser(Long uid, WxOAuth2UserInfo userInfo) {
        User user=new User();
        user.setId(uid);
        user.setName(userInfo.getNickname());
        user.setAvatar(userInfo.getHeadImgUrl());
        user.setSex(userInfo.getSex());
        return user;
    }

    public static UserInfoResp buildUserInfo(User user, Integer modifyNameChance) {
        UserInfoResp userInfoResp = BeanUtil.copyProperties(user, UserInfoResp.class);
        userInfoResp.setModifyNameChance(modifyNameChance);
        return userInfoResp;
    }
}
