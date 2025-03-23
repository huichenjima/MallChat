package com.hechen.mallchat.common.user.domain.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * ClassName: ModifyNameReq
 * Package: com.hechen.mallchat.common.user.domain.vo.req
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/21 13:45
 * @Version 1.0
 */
@Data
public class BlackReq {
    @ApiModelProperty("拉黑用户uid")
    @NotNull
    private Long uid;


}
