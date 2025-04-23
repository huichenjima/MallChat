package com.hechen.mallchat.common.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.hechen.mallchat.common.websocket.domain.enums.WSReqTypeEnum;
import com.hechen.mallchat.common.websocket.domain.vo.req.WSBaseReq;
import com.hechen.mallchat.common.websocket.service.WebSocketService;
import com.hechen.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandler.Sharable;
import sun.nio.ch.Net;

import javax.swing.*;

/**
 * ClassName: NettyWebSocketServerHandler
 * Package: com.hechen.mallchat.common.websocket
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/13 22:03
 * @Version 1.0
 */
@Slf4j
@Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>  {

    private WebSocketService webSocketService;
    //连接事件
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //通过钩子函数获取websocketservice容器获取
        webSocketService= SpringUtil.getBean(WebSocketService.class);
        webSocketService.connect(ctx.channel());

    }

    //客户端主动下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 可能出现业务判断离线后再次触发 channelInactive
        log.warn("触发 channelInactive 掉线![{}]", ctx.channel().id());
        userOffline(ctx.channel());
    }

    //握手完成后的事件,读取事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete)
        {
            System.out.println("握手完成，如果请求带了token直接进行token效验看是否登录");
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            if (StrUtil.isNotBlank(token))
            {
                webSocketService.authorize(ctx.channel(),token);
            }
            else
            {
                log.info("没有附带token");
            }


        }
        else if(evt instanceof IdleStateEvent){
            IdleStateEvent event=(IdleStateEvent) evt;
            if(event.state()== IdleState.READER_IDLE){
                System.out.println("读空闲");
                //表示客户端30s内都没有操作来发送请求，用户下线
                // TODO 用户下线  下面只是实现了连接的关闭还要实现一些业务逻辑 发消息通知
                userOffline(ctx.channel());
//                ctx.channel().close();
            }
        }
    }

    //用户下线统一处理
    private void userOffline(Channel channel){
        //清除映射关系，防止OOM问题
        webSocketService.removed(channel);
        channel.close();

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        WSBaseReq wsBaseReq = JSONUtil.toBean(text, WSBaseReq.class);
        switch (WSReqTypeEnum.of(wsBaseReq.getType())) {
            case AUTHORIZE://前端已经存储了token获取认证
                webSocketService.authorize(ctx.channel(),wsBaseReq.getData());
                break;
            case HEARTBEAT://心跳包检测
                break;
            case LOGIN://登录
                log.info("向微信请求二维码");
                webSocketService.handleLoginReq(ctx.channel());
                break;
            default:
                log.info("未知类型");

        }


    }
}
