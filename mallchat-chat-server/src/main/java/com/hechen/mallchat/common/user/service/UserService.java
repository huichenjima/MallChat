package com.hechen.mallchat.common.user.service;

import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hechen.mallchat.common.user.domain.vo.req.ModifyNameReq;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-14
 */
public interface UserService {

    Long register(User insert);

    UserInfoResp getUserInfo(Long uid);

    void modifyName(Long uid, ModifyNameReq modifyNameReq);
}
