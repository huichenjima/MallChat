package com.hechen.mallchat.common.user.service.impl;

import com.hechen.mallchat.common.user.dao.UserRoleDao;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * ClassName: RoleServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/23 14:04
 * @Version 1.0
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private UserCache userCache;
    @Override //判断用户是否有权限
    public boolean hasPower(Long uid, RoleEnum roleEnum) {
        //使用了caffeine缓存
        Set<Long> roleSet = userCache.getRoleSet(uid);
        //超级管理员有所有的权限
        return isAdmin(roleSet)||roleSet.contains(roleEnum.getId());
    }
    private boolean isAdmin(Set<Long> roleSet){
        return roleSet.contains(RoleEnum.ADMIN.getId());
    }
}
