package com.hechen.mallchat.common.user.service;

import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hechen.mallchat.common.user.domain.enums.IdempotentEnum;

/**
 * <p>
 * 用户背包表 服务类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-20
 */
public interface IUserBackpackService {

    /**
    给用户发放一个物品
    uid 用户id
     itemid 物品id
     idempotentEnum 幂等类型
     buinessId 幂等唯一标识
     */
    void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum,String buinessId);

}
