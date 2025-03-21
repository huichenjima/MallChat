package com.hechen.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

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
        }
        //继续下一个处理器，请注意在原始的最后一个变ws协议的处理器中最后不执行这一步因为已经是最后一个处理器了
            ctx.fireChannelRead(msg);
    }

}
