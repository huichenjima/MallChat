package com.hechen.mallchat.common.user.domain.vo.req;

import io.swagger.annotations.Api;
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
public class ModifyNameReq {
    @ApiModelProperty("用户名")
    @NotBlank
    @Length(max=6,message = "用户名不可以取太长，不然我记不住哦")
    private String name;


}
