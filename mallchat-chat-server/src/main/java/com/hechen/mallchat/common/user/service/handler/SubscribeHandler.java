package com.hechen.mallchat.common.user.service.handler;



import com.hechen.mallchat.common.user.service.WXMsgService;
import com.hechen.mallchat.common.user.service.adapter.TextBuilder;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
//关注事件的处理 ，首次关注该公众号
@Component
public class SubscribeHandler extends AbstractHandler {

    @Autowired
    WXMsgService wxMsgService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        this.logger.info("新关注用户 OPENID: " + wxMessage.getFromUser());
        // todo 获取eventkey 即code

        WxMpXmlOutMessage responseResult = null;
        try {
            //判断是不是扫码关注的，里面存放了code值，如果为空表明是通过其他途径进行关注的
            responseResult = wxMsgService.scan(wxMessage);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }

        if (responseResult != null) {
            return responseResult;
        }



        return TextBuilder.build("感谢关注",wxMessage);
    }



}
