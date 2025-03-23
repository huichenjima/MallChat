package com.hechen.mallchat.common.websocket.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hechen.mallchat.common.common.config.ThreadPoolConfig;
import com.hechen.mallchat.common.common.event.UserOnlineEvent;
import com.hechen.mallchat.common.common.thread.MyThreadFactory;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.IpInfo;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.hechen.mallchat.common.user.service.LoginService;
import com.hechen.mallchat.common.websocket.NettyUtil;
import com.hechen.mallchat.common.websocket.domain.dto.WSChannelExtraDTO;
import com.hechen.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSBaseResp;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSLoginUrl;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.service.WxService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.sound.sampled.Line;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: WebSocketServiceImpl
 * Package: com.hechen.mallchat.common.websocket.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 13:16
 * 专门管理websocket的逻辑，包括推拉
 * @Version 1.0
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Autowired
    LoginService loginService;

    @Autowired
    private UserDao userDao;

//    ConcurrentHashMap是线程安全的容器，专门用于多线程并发环境下共享数据的场景。
    /*
    管理所有用户的连接包括登录态和游客
     */
    public static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    public static final int MAXIMUM_SIZE = 10000; //最大映射个数
    public static final Duration DURATION = Duration.ofHours(1); //最大过期时间

    /*
                登录的临时映射 ，随机code和channe的映射 方面后面微信通过code寻找channel
    */
    public static final Cache<Integer,Channel> WATT_LOGIN_MAP = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(DURATION)
            .build();

    @Autowired
    @Lazy
    private WxMpService wxMpService;

    @Autowired //事件生产者 即发送方
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private IRoleService roleService;

    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    //保存channel连接
    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel,new WSChannelExtraDTO());

    }
    //清除连接
    @Override
    public void remove(Channel channel) {
        ONLINE_WS_MAP.remove(channel);
        // todo 用户下线 推送用户下线消息即用户下线广播
    }

    /*
    用户扫码成功以及授权成功后登录操作
     */
    @Override
    public void scanLoginAndAuthorizeSuccess(Integer code, Long uid) {
        //确认连接在服务器上
        Channel channel = WATT_LOGIN_MAP.getIfPresent(code);
        if(Objects.isNull(channel))
            return;
        User user = userDao.getById(uid);
        //移除code与channel的映射关系
        WATT_LOGIN_MAP.invalidate(code);
        //调用登录模块获取token
        String token = loginService.login(uid);
        //用户登录,向前端发送消息，resp里带了user信息和token
        loginSuccess(channel,user,token);


    }

    //申请二维码
    @SneakyThrows
    @Override
    public void handleLoginReq(Channel channel) {
        //生成随机码code并且保存code与channel的映射关系
        Integer code = generateLoginCode(channel);
        //找微信申请带参数二维码
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) DURATION.getSeconds());
        //把码推送给前端
        sendMsg(channel, WebSocketAdapter.buildResp(wxMpQrCodeTicket));


    }

    //发送消息给所有连接
    @Override
    public void sendMsgToAll(WSBaseResp<?> msg) {
        ONLINE_WS_MAP.forEach((channel,ext)->{
            threadPoolTaskExecutor.execute(()->{
                sendMsg(channel,msg);
            });

        });

    }

    //前端发送信息进行认证
    @Override
    public void authorize(Channel channel, String token) {
        //这里不用转token data里就是tokenstring了没有再嵌套一层token
        Long validUid = loginService.getValidUid(token);
        if(Objects.nonNull(validUid)){
            //token检验成功
            //用户登录,向前端发送消息，resp里带了user信息和token
            User user = userDao.getById(validUid);
            loginSuccess(channel,user,token);

        }
        else{
            //如果这个token已经校验不合格了已经，发送消息通知前端去除掉token
            sendMsg(channel, WebSocketAdapter.buildInvalidTokenResp());
        }



    }

    private void loginSuccess(Channel channel, User user, String token) {

        //保存channel与uid的映射
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        wsChannelExtraDTO.setUid(user.getId());


        //通知前端,推送成功消息,这里要传入权限信息
        sendMsg(channel,WebSocketAdapter.buildResp(user,token,roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER)));

        // 用户上线成功的群发消息等任务
        user.setLastOptTime(new Date()); //更新最后上线时间
        user.refreshIp(NettyUtil.getAttr(channel,NettyUtil.IP)); //更新ip地址
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this,user));

    }

    //通知前端等待用户授权
    @Override
    public void waitAuthorize(Integer code) {
        Channel channel= WATT_LOGIN_MAP.getIfPresent(code);
        if (Objects.isNull(channel))
            return;
        sendMsg(channel,WebSocketAdapter.buildWaitAuthorizeResp());
    }

    //服务端向客户端发送消息
    @Override
    public void sendMsg(Channel channel, WSBaseResp<?> resp) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(resp)));
    }
    //生成随机码code并且保存进映射map，映射code与当前连接的channel
    private Integer generateLoginCode(Channel channel) {
        Integer code;
        do {
            code= RandomUtil.randomInt(Integer.MAX_VALUE);
        }while (Objects.nonNull(WATT_LOGIN_MAP.asMap().putIfAbsent(code,channel)));
        return code;

    }
}
