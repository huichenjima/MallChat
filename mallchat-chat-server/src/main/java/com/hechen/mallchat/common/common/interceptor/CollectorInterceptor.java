package com.hechen.mallchat.common.common.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.hechen.mallchat.common.common.domain.dto.RequestInfo;
import com.hechen.mallchat.common.common.utils.RequestHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * ClassName: CollectorInterceptor
 * Package: com.hechen.mallchat.common.common.interceptor
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 11:29
 * @Version 1.0
 */
//收集用户信息拦截器,uid和ip地址
@Component
public class CollectorInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token拦截器的token
        Long uid = Optional.ofNullable(request.getAttribute(TokenInterceptor.UID))
                .map(Object::toString)
                .map(Long::parseLong)
                .orElse(null);
        //获取客户端ip地址
        String ip = ServletUtil.getClientIP(request);
        RequestInfo requestInfo=new RequestInfo(uid,ip);
        //设置当前线程的用户信息
        RequestHolder.set(requestInfo);

        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }
}
