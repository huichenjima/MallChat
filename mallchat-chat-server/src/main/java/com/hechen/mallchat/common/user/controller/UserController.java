package com.hechen.mallchat.common.user.controller;


import cn.hutool.system.UserInfo;
import com.hechen.mallchat.common.common.domain.dto.RequestInfo;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.common.interceptor.TokenInterceptor;
import com.hechen.mallchat.common.common.utils.RequestHolder;
import com.hechen.mallchat.common.user.domain.vo.req.ModifyNameReq;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
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
        userService.modifyName(uid,modifyNameReq);
        return ApiResult.success();
    }



}

