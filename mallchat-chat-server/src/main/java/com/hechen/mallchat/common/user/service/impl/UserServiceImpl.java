package com.hechen.mallchat.common.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.system.UserInfo;
import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.common.exception.BusinessException;
import com.hechen.mallchat.common.user.dao.UserBackpackDao;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.hechen.mallchat.common.user.domain.enums.ItemEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.domain.vo.req.ModifyNameReq;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
import com.hechen.mallchat.common.user.service.UserService;
import com.hechen.mallchat.common.user.service.adapter.UserAdapter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * ClassName: UserServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 15:53
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    UserBackpackDao userBackpackDao;



    @Override
    @Transactional
    public Long register(User insert) {
        //todo 后序有其他业务 比如新注册用户送改名卡之类，涉及到多个数据库的操作，这里要有事务
        userDao.save(insert);
        //todo 发送用户注册的事件

        //返回用户的uid
        return insert.getId();

    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        //查询用户基本信息
        User user= userDao.getById(uid);
        //查询改名卡张数
        Integer modifyNameChance = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        return UserAdapter.buildUserInfo(user,modifyNameChance);
    }

    @Override
    @Transactional
    public void modifyName(Long uid, ModifyNameReq modifyNameReq) {
//        名字不能重复,重复则返回
        User oldUser = userDao.getByName(modifyNameReq.getName());
        if(Objects.nonNull(oldUser)){
            throw new BusinessException("用户名重复,换个名字吧");
        }
        //先判断有没有改名卡了
        //查询改名卡张数
        Integer modifyNameChance = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        if(modifyNameChance==null||modifyNameChance<=0) //没有改名机会了
            return;
        //进行改名，先减少改名卡再进行改名,只更新一张改名卡
        boolean update = userBackpackDao.lambdaUpdate()
                .eq(UserBackpack::getItemId, ItemEnum.MODIFY_NAME_CARD.getId())
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .set(UserBackpack::getStatus, YesOrNoEnum.YES.getStatus())
                .last("LIMIT 1")
                .update();
        //再进行改名，

        boolean update1 = userDao.lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, modifyNameReq.getName())
                .update();
        //结束
    }
}
