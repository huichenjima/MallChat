package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.user.domain.entity.Black;
import com.hechen.mallchat.common.user.mapper.BlackMapper;
import com.hechen.mallchat.common.user.service.IBlackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 黑名单 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-23
 */
@Service
public class BlackDao extends ServiceImpl<BlackMapper, Black> {

    public Black getbyUid(String uid) {
        return lambdaQuery().eq(Black::getTarget,uid).one();

    }
}
