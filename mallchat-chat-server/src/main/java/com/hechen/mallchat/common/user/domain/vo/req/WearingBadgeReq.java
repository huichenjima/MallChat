package com.hechen.mallchat.common.user.domain.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
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
public class WearingBadgeReq {
    @ApiModelProperty("勋章id")
    @NotNull
    private Long itemId;


}
