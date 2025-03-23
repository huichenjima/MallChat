package com.hechen.mallchat.common.common.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import com.hechen.mallchat.common.common.event.UserBlackEvent;
import com.hechen.mallchat.common.common.exception.HttpErrorEnum;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.common.utils.RequestHolder;
import com.hechen.mallchat.common.user.dao.BlackDao;
import com.hechen.mallchat.common.user.domain.entity.Black;
import com.hechen.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ClassName: BlackInterceptor
 * Package: com.hechen.mallchat.common.common.interceptor
 * Description: 黑名单用户拦截
 *
 * @Author 何琛
 * @Create 2025/3/23 15:56
 * @Version 1.0
 */
//这个拦截器在token和collect拦截器后，所以id和ip已经获取了
@Component
public class BlackInterceptor implements HandlerInterceptor {
    @Autowired //从缓存拿黑名单，不应该每次去查
    private UserCache userCache;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Long uid = RequestHolder.get().getUid();
        String ip=RequestHolder.get().getIp();
        //从缓存拿黑名单
        Map<Integer, Set<String>> blackMap = userCache.getBlackMap();
        if (inBlackList(uid, blackMap.get(BlackTypeEnum.UID.getType()))) {
            HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
            return false;
        }
        if (inBlackList(ip, blackMap.get(BlackTypeEnum.IP.getType()))) {
            HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
            return false;
        }
        return true;



    }
    private boolean inBlackList(Object target, Set<String> blackSet) {
        if (Objects.isNull(target) || CollectionUtil.isEmpty(blackSet)) { //为空不进行校验直接放
            return false;
        }
        return blackSet.contains(target.toString());
    }
}
