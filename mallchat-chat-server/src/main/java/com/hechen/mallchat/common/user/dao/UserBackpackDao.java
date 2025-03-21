package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.hechen.mallchat.common.user.mapper.UserBackpackMapper;
import com.hechen.mallchat.common.user.service.IUserBackpackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户背包表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-20
 */
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack> {

    public Integer getCountByValidItemId(Long uid, Long itemId) {
        Integer count = this.lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .count();
        return count;

    }
}
