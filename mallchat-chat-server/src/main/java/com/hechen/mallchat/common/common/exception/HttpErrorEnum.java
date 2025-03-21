package com.hechen.mallchat.common.common.exception;

import cn.hutool.http.ContentType;
import com.google.common.base.Charsets;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.common.utils.JsonUtils;
import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ClassName: HttpErrorEnum
 * Package: com.hechen.mallchat.common.common.exception
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 10:51
 * Http错误的枚举对象
 * @Version 1.0
 */
@AllArgsConstructor
public enum HttpErrorEnum {
    ACCESS_DENIED(401,"登录失效请重新登录");
    private Integer httpcode;
    private String desc;
    public void sendHttpError(HttpServletResponse response) throws IOException {
        response.setStatus(httpcode);
        response.setContentType(ContentType.JSON.toString(Charsets.UTF_8));
        response.getWriter().write(JsonUtils.toStr(ApiResult.fail(httpcode,desc)));
    }

}
