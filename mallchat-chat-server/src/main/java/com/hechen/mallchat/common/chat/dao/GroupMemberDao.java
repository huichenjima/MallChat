package com.hechen.mallchat.common.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hechen.mallchat.common.chat.domain.entity.GroupMember;
import com.hechen.mallchat.common.chat.domain.enums.GroupRoleEnum;
import com.hechen.mallchat.common.chat.mapper.GroupMemberMapper;
import com.hechen.mallchat.common.chat.service.cahce.GroupMemberCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hechen.mallchat.common.chat.domain.enums.GroupRoleEnum.ADMIN_LIST;

/**
 * <p>
 * 群成员表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Service
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, GroupMember> {

    @Autowired
    @Lazy
    private GroupMemberCache groupMemberCache;


    public GroupMember getMember(Long id, Long uid) {
        GroupMember one = lambdaQuery().eq(GroupMember::getId, id)
                .eq(GroupMember::getUid, uid).one();
        return one;

    }
}
