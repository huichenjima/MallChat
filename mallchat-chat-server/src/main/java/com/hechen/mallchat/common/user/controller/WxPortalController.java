package com.hechen.mallchat.common.user.controller;

//import com.hechen.mallchat.common.user.service.WxMsgService;
import com.hechen.mallchat.common.user.service.WXMsgService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Description: 微信api交互接口
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("wx/portal/public")
public class WxPortalController {

    private final WxMpService wxService;
    private final WxMpMessageRouter messageRouter;
    private final WXMsgService wxMsgService;

    @GetMapping("/test")
    public String getQeCode(@RequestParam Integer code) throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxService.getQrcodeService().qrCodeCreateTmpTicket(code, 10000);

        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);
        return url;
    }

    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                 timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }


        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    @GetMapping("/callBack")
    public RedirectView callBack(@RequestParam String code) throws WxErrorException {
        WxOAuth2AccessToken accessToken = wxService.getOAuth2Service().getAccessToken(code);
        WxOAuth2UserInfo zh_cn = wxService.getOAuth2Service().getUserInfo(accessToken, "zh_cn");
        System.out.println(zh_cn);
        wxMsgService.authorize(zh_cn);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("./test.html");
        return redirectView;
    }
//@GetMapping("/callBack")
//public String  callBack(@RequestParam String code) throws WxErrorException {
//    WxOAuth2AccessToken accessToken = wxService.getOAuth2Service().getAccessToken(code);
//    WxOAuth2UserInfo zh_cn = wxService.getOAuth2Service().getUserInfo(accessToken, "zh_cn");
//    System.out.println(zh_cn);
//    wxMsgService.authorize(zh_cn);
//
//    return "<html>" +
//            "<head><meta charset='UTF-8'><title>提示</title></head>" +
//            "<body style='text-align:center;margin-top:50px;'>" +
//            "<h1 style='color:green;'>你无敌了！🎉</h1>" +
//            "</body></html>";
//}

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!wxService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
        }

        log.debug("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter.route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }

        return null;
    }
                                     }