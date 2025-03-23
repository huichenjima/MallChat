package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.UserRole;
import com.hechen.mallchat.common.user.mapper.UserRoleMapper;
import com.hechen.mallchat.common.user.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户角色关系表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-23
 */
@Service
public class UserRoleDao extends ServiceImpl<UserRoleMapper, UserRole> {
    //自己写的，用不到
    public boolean getByUidAndRole(Long uid, Integer RoleId) {
        UserRole one = lambdaQuery().eq(UserRole::getUid, uid).eq(UserRole::getRoleId, RoleId).one();
        //如果找到了返回true说明有权限，没有返回false说明没有权限
        return Objects.nonNull(one);
    }

    public List<UserRole> listByUid(Long uid) {
        List<UserRole> list = lambdaQuery().eq(UserRole::getUid, uid).list();
        return list;
    }

}
