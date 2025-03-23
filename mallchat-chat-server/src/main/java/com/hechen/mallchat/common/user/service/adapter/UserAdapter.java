package com.hechen.mallchat.common.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static List<BadgeResp> buildBadgeResp(List<ItemConfig> itemConfigs, List<UserBackpack> backpacks, User user) {
        Set<Long> obtainItemSet = backpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());
        List<BadgeResp> resps = itemConfigs.stream().map(a -> {
            BadgeResp resp = BeanUtil.copyProperties(a, BadgeResp.class);
            resp.setObtain(obtainItemSet.contains(a.getId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
            resp.setWearing(Objects.equals(a.getId(),user.getItemId())?YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
            return resp;

        })
                .sorted(Comparator.comparing(BadgeResp::getWearing,Comparator.reverseOrder())
        .thenComparing(BadgeResp::getObtain,Comparator.reverseOrder()))
                .collect(Collectors.toList());
        return resps;

    }
}
