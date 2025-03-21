package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import com.hechen.mallchat.common.user.mapper.ItemConfigMapper;
import com.hechen.mallchat.common.user.service.IItemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
