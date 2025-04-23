package com.hechen.mallchat.common.websocket.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hechen.mallchat.common.common.config.ThreadPoolConfig;
import com.hechen.mallchat.common.common.event.UserOfflineEvent;
import com.hechen.mallchat.common.common.event.UserOnlineEvent;
import com.hechen.mallchat.common.common.thread.MyThreadFactory;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.IpInfo;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.enums.RoleEnum;
import com.hechen.mallchat.common.user.service.IRoleService;
import com.hechen.mallchat.common.user.service.LoginService;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import com.hechen.mallchat.common.websocket.NettyUtil;
import com.hechen.mallchat.common.websocket.domain.dto.WSChannelExtraDTO;
import com.hechen.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSBaseResp;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSLoginUrl;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSOnlineOfflineNotify;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
@Slf4j
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

    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();

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

    @Autowired
    private UserCache userCache;


    //保存channel连接
    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel,new WSChannelExtraDTO());

    }
    //清除连接
    @Override
    public void removed(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<Long> uidOptional = Optional.ofNullable(wsChannelExtraDTO)
                .map(WSChannelExtraDTO::getUid);
        boolean offlineAll = offline(channel, uidOptional);
        //用户下线 推送用户下线消息即用户下线广播
        if (uidOptional.isPresent() && offlineAll) {//已登录用户断连,并且全下线成功
            User user = new User();
            user.setId(uidOptional.get());
            user.setLastOptTime(new Date());
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
        }

    }

    /**
     * 用户下线
     * return 是否全下线成功
     */
    private boolean offline(Channel channel, Optional<Long> uidOptional) {
        //移除channel与uid的映射关系
        ONLINE_WS_MAP.remove(channel);
        if (uidOptional.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uidOptional.get());
            //移除uid与channel的映射关系，但是不是全部消除，因为可能是多端登录，其他端保存了uid与channel的映射
            if (CollectionUtil.isNotEmpty(channels)) {
                channels.removeIf(ch -> Objects.equals(ch, channel));
            }
            return CollectionUtil.isEmpty(ONLINE_UID_MAP.get(uidOptional.get()));
        }
        return true;
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

    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseResp, Long skipUid) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            if (Objects.nonNull(skipUid) && Objects.equals(ext.getUid(), skipUid)) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBaseResp));
        });
    }
    //没有跳过某个用户，一般是自身
    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseResp) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBaseResp));
        });
    }

    @Override
    public void sendToUid(WSBaseResp<?> wsBaseResp, Long uid) {
        //获取当前要发送用户uid的对应websocket,因为有多端所以channel可能是个集合这里就对应了集群广播中的连接过滤
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid);
        //为空说明不在线
        if (CollectionUtil.isEmpty(channels)) {
            log.info("用户：{}不在线", uid);
            return;
        }
        channels.forEach(channel -> {
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBaseResp));
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

        //更新上线列表
        online(channel, user.getId());
        //通知前端,推送成功消息,这里要传入权限信息
        sendMsg(channel,WebSocketAdapter.buildResp(user,token,roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER)));
        boolean online = userCache.isOnline(user.getId());
        // 用户上线成功的群发消息等任务
        if (!online){
            user.setLastOptTime(new Date()); //更新最后上线时间
            user.refreshIp(NettyUtil.getAttr(channel,NettyUtil.IP)); //更新ip地址
            applicationEventPublisher.publishEvent(new UserOnlineEvent(this,user));
        }


    }

    /**
     * 用户上线
     */
    private void online(Channel channel, Long uid) {
        //保存channel与uid的映射
        getOrInitChannelExt(channel).setUid(uid);
        //保存uid对应哪些channel ，即保存uid在哪些websocket上，方便消息的接收
        ONLINE_UID_MAP.putIfAbsent(uid, new CopyOnWriteArrayList<>());
        ONLINE_UID_MAP.get(uid).add(channel);
        NettyUtil.setAttr(channel, NettyUtil.UID, uid);
    }

    /**
     * 如果在线列表不存在，就先把该channel放进在线列表
     *
     * @param channel
     * @return
     */
    private WSChannelExtraDTO getOrInitChannelExt(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO =
                ONLINE_WS_MAP.getOrDefault(channel, new WSChannelExtraDTO());
        WSChannelExtraDTO old = ONLINE_WS_MAP.putIfAbsent(channel, wsChannelExtraDTO);
        return ObjectUtil.isNull(old) ? wsChannelExtraDTO : old;
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
