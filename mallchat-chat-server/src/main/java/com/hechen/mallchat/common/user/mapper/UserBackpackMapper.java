package com.hechen.mallchat.common.user.mapper;

import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;

import java.util.List;

/**
 * <p>
 * 用户背包表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-20
 */
public interface UserBackpackMapper extends BaseMapper<UserBackpack> {
    public List<BadgeResp> findBadgeResp(Long uid,Integer type);

}
