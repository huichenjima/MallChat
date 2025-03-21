package com.hechen.mallchat.common.user.service.handler;

import com.hechen.mallchat.common.user.service.WXMsgService;
import com.hechen.mallchat.common.user.service.adapter.TextBuilder;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
//扫码事件提醒
@Component
public class ScanHandler extends com.hechen.mallchat.common.user.service.handler.AbstractHandler {

    @Autowired
    private WXMsgService wxMsgService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        return wxMsgService.scan(wxMpXmlMessage);
        //eventkey就是传的code
//        String code = wxMpXmlMessage.getEventKey();
        //扫的用户是谁？获取用户的openid
//        String openId = wxMpXmlMessage.getFromUser();
        //todo 扫码事件后返回
        //思路是把这个你好返回转换为一个授权链接，请注意如果这里code重复了公众号不会回消息也就是eventkey不能重复
//        return TextBuilder.build("你好",wxMpXmlMessage);


    }

}
