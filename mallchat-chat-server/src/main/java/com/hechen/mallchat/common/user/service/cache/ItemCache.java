package com.hechen.mallchat.common.user.service.cache;

import com.hechen.mallchat.common.user.dao.ItemConfigDao;
import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName: ItemCache
 * Package: com.hechen.mallchat.common.user.service.cache
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 18:22
 * @Version 1.0
 */
@Component
public class ItemCache {
    @Autowired
    private ItemConfigDao itemConfigDao;

    @Cacheable(cacheNames = "item",key="'itemsByType:'+#itemType")
    public List<ItemConfig> getByType(Integer itemType){
        return itemConfigDao.getValidByType(itemType);
    }

    @CacheEvict
    public void evictByType(Integer itemType){
    }

}
