package com.hechen.mallchat.common.user.service;

import com.hechen.mallchat.common.user.domain.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-23
 */
public interface IRoleService {
    //判断用户是否有某个权限
    boolean hasPower(Long uid, RoleEnum roleEnum);

}
