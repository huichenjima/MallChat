package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.Role;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.mapper.RoleMapper;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-23
 */
@Service
public class RoleDao extends ServiceImpl<RoleMapper, Role> {

}
