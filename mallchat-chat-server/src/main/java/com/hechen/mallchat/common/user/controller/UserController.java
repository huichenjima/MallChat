package com.hechen.mallchat.common.user.controller;


import cn.hutool.system.UserInfo;
import com.hechen.mallchat.common.common.domain.dto.RequestInfo;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.common.interceptor.TokenInterceptor;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.common.utils.RequestHolder;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.domain.vo.req.BlackReq;
import com.hechen.mallchat.common.user.domain.vo.req.ModifyNameReq;
import com.hechen.mallchat.common.user.domain.vo.req.WearingBadgeReq;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.hechen.mallchat.common.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.transform.Result;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-14
 */
//capi表示costumer 消费者端接口  bapi表示商业端 可以理解为管理端？
@RestController
@RequestMapping("/capi/user")
@Api(tags = "用户模块")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private IRoleService roleService;

    @GetMapping("/userInfo")
    @ApiOperation("获取用户个人信息")
    public ApiResult<UserInfoResp> getUserInfo(HttpServletRequest request){
        //token拦截器解析token来获取uid，并且collect拦截器进一步拦截保存uid和ip进threadlocal中
        RequestInfo requestInfo = RequestHolder.get();
        Long uid = requestInfo.getUid();
        return ApiResult.success(userService.getUserInfo(uid));
    }

    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult modifyName(@RequestBody @Valid ModifyNameReq modifyNameReq){
        RequestInfo requestInfo = RequestHolder.get();
        Long uid = requestInfo.getUid();
        userService.modifyName(uid,modifyNameReq.getName());
        return ApiResult.success();
    }
    @GetMapping("/badges")
    @ApiOperation("可选徽章预览")
    public ApiResult<List<BadgeResp>> badges(){
        RequestInfo requestInfo = RequestHolder.get();
        Long uid = requestInfo.getUid();
        return ApiResult.success(userService.badges(uid));
    }

    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public ApiResult<Void> wearingBadge(@RequestBody @Valid WearingBadgeReq req){
        RequestInfo requestInfo = RequestHolder.get();
        Long uid = requestInfo.getUid();
        userService.wearingBadge(uid,req.getItemId());
        return ApiResult.success();
    }

    @PutMapping("/black")
    @ApiOperation("添加黑名单")
    public ApiResult<Void> black(@RequestBody @Valid BlackReq req){
        RequestInfo requestInfo = RequestHolder.get();
        Long uid = requestInfo.getUid();
        AssertUtil.isTrue(roleService.hasPower(uid, RoleEnum.ADMIN),"当前用户没有权限哦");
        //验证了当前用户为管理员，有权限进行拉黑操作
        userService.black(req);
        return ApiResult.success();
    }



}

