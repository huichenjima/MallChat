package com.hechen.mallchat.common.user.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.service.UserService;
import com.hechen.mallchat.common.user.service.WXMsgService;
import com.hechen.mallchat.common.user.service.adapter.TextBuilder;
import com.hechen.mallchat.common.user.service.adapter.UserAdapter;
import com.hechen.mallchat.common.websocket.NettyWebSocketServerHandler;
import com.hechen.mallchat.common.websocket.domain.dto.WSChannelExtraDTO;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.common.websocket.service.impl.WebSocketServiceImpl;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: WXMsgServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 15:10
 * @Version 1.0
 */
@Service
@Slf4j
public class WXMsgServiceImpl implements WXMsgService {
    @Autowired
    private WebSocketService webSocketService;

    //openid与code的映射关系map，key是openid，key是Integer,同样使用了ConcurrentHashMap 保证多并发场景
    private static final ConcurrentHashMap<String,Integer> WATT_AUTHORIZE_MAP=new ConcurrentHashMap<>();

    private static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
    @Value("${wx.mp.callback}")
    private String callback;

    @Autowired
    UserDao userDao;
    @Autowired
    UserService userService;

    @Autowired
    @Lazy
    WxMpService wxMpService;




    /*
    用户授权
     */
    @Override
    public void authorize(WxOAuth2UserInfo userInfo) {
        //补全信息
        String openid = userInfo.getOpenid();
        User user = userDao.getByOpenId(openid);
        //更新用户信息
        if (StrUtil.isBlank(user.getAvatar()))
        {
            fillUserInfo(user.getId(),userInfo);
        }
        //取concurrenthashmapp的映射信息得到code 并且删除中间的映射关系openid与code的
        Integer code = WATT_AUTHORIZE_MAP.remove(openid);

        //调用websocket进行真正的登录: 再通过code得到channel信息 并且删除中间的映射关系code与channel的
        webSocketService.scanLoginAndAuthorizeSuccess(code,user.getId());




    }

    /*
    更新用户异常
     */
    private void fillUserInfo(Long uid, WxOAuth2UserInfo userInfo) {
        User user = UserAdapter.buildAuthorizeUser(uid, userInfo);
        try {
            userDao.updateById(user);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            String s = RandomUtil.randomNumbers(5);
            user.setName(s);
            userDao.updateById(user);
        }
    }

    /*
    用户扫码成功后的事件，进注册并且返回授权链接
     */
    @Override
    public WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage) {

        //eventkey就是传的code
        Integer code = getEventKey(wxMpXmlMessage);
        //扫的用户是谁？获取用户的openid
        String openId = wxMpXmlMessage.getFromUser();
        //如果code为空，说明扫码返回信息有误，不做任何处理
        if (Objects.isNull(code))
            return null;
        User user=userDao.getByOpenId(openId);
        //用户是否已经注册
        boolean registered=Objects.nonNull(user);
        //用户是否已经授权
        boolean authorized = registered && StrUtil.isNotBlank(user.getAvatar());
        //如果用户已在数据库并且头像和名字不为空说明已经注册且授权，即已经登录成功了
        if(registered&&authorized)
        {
            //走登录成功逻辑，通过code找到channel 再设置uid与channel的关系
            webSocketService.scanLoginAndAuthorizeSuccess(code,user.getId());
        }
        //用户未注册，先注册
        if(!registered)
        {
            User insert = UserAdapter.buildUserSave(openId);
            userService.register(insert);
        }
        //保存openid与code的关系
        WATT_AUTHORIZE_MAP.put(openId,code);
        //向前端发送一条消息表明扫码成功下面需要进行授权
        webSocketService.waitAuthorize(code);
        //没有授权所以没有头像和名字，发送用户授权链接
        String authorizeUrl =String.format(URL,wxMpService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback + "/wx/portal/public/callBack"));
        System.out.println(authorizeUrl);
        WxMpXmlOutMessage.TEXT().build();
        return TextBuilder.build("请点击链接授权：<a href=\"" + authorizeUrl + "\">登录</a>", wxMpXmlMessage);



    }

    //返回的code为qrscene_xxxx 要把前面的qrscene去掉才是真正的code
    private Integer getEventKey(WxMpXmlMessage wxMpXmlMessage){
        try {
            String eventKey= wxMpXmlMessage.getEventKey();
            String code=eventKey.replace("qrscene_","");
            return Integer.parseInt(code);
        }catch (Exception e){
            log.error("getEventKey error eventKey:{}",wxMpXmlMessage.getEventKey(),e);
            return null;

        }


    }
}
