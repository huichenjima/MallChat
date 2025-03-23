package com.hechen.mallchat.common.websocket.service.adapter;

import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSBaseResp;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSBlack;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSLoginSuccess;
import com.hechen.mallchat.common.websocket.domain.vo.resp.WSLoginUrl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

/**
 * ClassName: WebSocketAdapter
 * Package: com.hechen.mallchat.common.websocket.service.adapter
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 14:07
 * 类型转换
 * @Version 1.0
 */
public class WebSocketAdapter {


    public static WSBaseResp<?> buildResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        WSBaseResp<WSLoginUrl> resp=new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        resp.setData(new WSLoginUrl(wxMpQrCodeTicket.getUrl()));
        return resp;
    }

    public static WSBaseResp<?> buildResp(User user, String token,boolean power) {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess build = WSLoginSuccess.builder()
                .avatar(user.getAvatar())
                .name(user.getName())
                .uid(user.getId())
                .token(token)
                .power(power? YesOrNoEnum.YES.getStatus() :YesOrNoEnum.NO.getStatus())
                .build();
        resp.setData(build);
        return resp;



    }


    public static WSBaseResp<?> buildWaitAuthorizeResp() {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return resp;

    }

    public static WSBaseResp<?> buildInvalidTokenResp() {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.INVALIDATE_TOKEN.getType());
        return resp;
    }

    public static WSBaseResp<?> buildBlack(User user) {
        WSBaseResp<WSBlack> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.BLACK.getType());
        resp.setData(WSBlack.builder().uid(user.getId()).build());
        return resp;

    }
}
