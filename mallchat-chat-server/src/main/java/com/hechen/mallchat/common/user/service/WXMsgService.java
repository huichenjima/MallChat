package com.hechen.mallchat.common.user.service;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * ClassName: WXMsgService
 * Package: com.hechen.mallchat.common.user.service
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 15:09
 * @Version 1.0
 */
public interface WXMsgService {
    /*
    用户扫码成功
     */
    WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage);

    void authorize(WxOAuth2UserInfo zh_cn);
}
