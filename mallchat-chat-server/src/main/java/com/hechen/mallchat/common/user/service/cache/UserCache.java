package com.hechen.mallchat.common.user.service.cache;

import com.hechen.mallchat.common.user.dao.BlackDao;
import com.hechen.mallchat.common.user.dao.ItemConfigDao;
import com.hechen.mallchat.common.user.dao.UserRoleDao;
import com.hechen.mallchat.common.user.domain.entity.Black;
import com.hechen.mallchat.common.user.domain.entity.ItemConfig;
import com.hechen.mallchat.common.user.domain.entity.UserRole;
import com.hechen.mallchat.common.user.domain.enums.BlackTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class UserCache {
    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BlackDao blackDao;

    //用户所有的角色缓存
    @Cacheable(cacheNames = "user",key="'rolesByUid:'+#uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> userRoles = userRoleDao.listByUid(uid);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
    }
    //存放黑名单的map缓存，1对应的set代表uid黑名单，2对应的set代表ip的黑名单
    @Cacheable(cacheNames = "user",key="'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        Map<Integer, Set<String>> map=new HashMap<>();
        List<Black> list = blackDao.lambdaQuery().eq(Black::getType, BlackTypeEnum.UID.getType()).list();
        Set<String> uids = list.stream().map(Black::getTarget).collect(Collectors.toSet());
        map.put( BlackTypeEnum.UID.getType(),uids);
        List<Black> listIp = blackDao.lambdaQuery().eq(Black::getType, BlackTypeEnum.IP.getType()).list();
        Set<String> ips = listIp.stream().map(Black::getTarget).collect(Collectors.toSet());
        map.put( BlackTypeEnum.IP.getType(),ips);
        return map;


    }

    //更新缓存，再对黑名单拉黑时进行更新缓存操作，这里是删除操作最简单的缓存更新策略
    @CacheEvict(cacheNames = "user",key="'blackList'")
    public Map<Integer, Set<String>> evictBlackMap(){
        return null;
    }


}
