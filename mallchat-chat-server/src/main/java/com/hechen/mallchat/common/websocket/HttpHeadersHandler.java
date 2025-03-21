package com.hechen.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * ClassName: HttpHeadersHandler
 * Package: com.hechen.mallchat.common.websocket
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/14 15:12
 * 升级成ws协议前获得http中的ip地址并且保存进channel中
 * @Version 1.0
 */
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest)
        {
            FullHttpRequest request=(FullHttpRequest) msg;
            UrlBuilder urlBuilder=UrlBuilder.ofHttp(request.uri());

            //获取token参数
            String token = Optional.ofNullable(urlBuilder.getQuery()).map(k->k.get("token")).map(CharSequence::toString).orElse("");
            //设置token进自己的连接
            NettyUtil.setAttr(ctx.channel(),NettyUtil.TOKEN,token);

            //获取请求路劲
            request.setUri(urlBuilder.getPath().toString());
            HttpHeaders headers = request.headers();
            String ip = headers.get("X-Real-IP");
            if(StringUtils.isEmpty(ip)){
                //如果没有经过nginx的转发，就直接获取远端地址
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);
            //同样地获取了ip后就可以丢弃这个处理器了
            ctx.pipeline().remove(this);
            //传递到下一个处理器
            ctx.fireChannelRead(request);



        }
            ctx.fireChannelRead(msg);

    }
}
