package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;
import com.hechen.mallchat.common.user.mapper.ItemConfigMapper;
import com.hechen.mallchat.common.user.service.IItemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 功能物品配置表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-20
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig> {

    //找出所有的徽章
    public List<ItemConfig> getAllBadges() {
        List<ItemConfig> list = lambdaQuery().eq(ItemConfig::getType, ItemTypeEnum.BADGE.getType())
                .list();
        return list;
    }

    //找出当前用户没有的徽章
    public List<ItemConfig> findhasNoIdList(List<Long> idList, Integer type) {
        return lambdaQuery().notIn(ItemConfig::getId, idList)
                .eq(ItemConfig::getType, type)
                .list();
    }

    //根据类型返回
    public List<ItemConfig> getValidByType(Integer itemType) {
        return lambdaQuery().eq(ItemConfig::getType, itemType)
                .list();

    }
}
