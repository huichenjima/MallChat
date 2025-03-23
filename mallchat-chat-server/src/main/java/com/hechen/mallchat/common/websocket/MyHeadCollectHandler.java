package com.hechen.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

//protocol不适合用来传自协议，所以这个改protocol的不好用
public class MyHeadCollectHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request= (HttpRequest) msg;
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.getUri());
            Optional<String> tokenOptional = Optional.ofNullable(urlBuilder)
                    .map(UrlBuilder::getQuery)
                    .map(k -> k.get("token"))
                    .map(CharSequence::toString);
            //如果token存在，就存进channel中的attr中
            tokenOptional.ifPresent(s -> NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, s));
            //netty最后握手处理器中的必须保证是原请求即 ws://localhost:8090 所以要去掉url中的参数部分
            request.setUri(urlBuilder.getPath().toString());
            //取用户ip
            //这是nginx上的
            String ip = request.headers().get("X-Real-IP");
            //如果nginx没有，则说明是在本地调试
            if(StringUtils.isBlank(ip))
            {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            //保存到channel属性中
            NettyUtil.setAttr(ctx.channel(),NettyUtil.IP,ip);

            //该处理器只使用一次，移除掉这个处理器
            ctx.pipeline().remove(this);
        }
        //继续下一个处理器，请注意在原始的最后一个变ws协议的处理器中最后不执行这一步因为已经是最后一个处理器了
            ctx.fireChannelRead(msg);
    }

}
