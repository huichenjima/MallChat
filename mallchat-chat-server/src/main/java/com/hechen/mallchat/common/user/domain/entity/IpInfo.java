package com.hechen.mallchat.common.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * ClassName: IpInfo
 * Package: com.hechen.mallchat.common.user.domain.entity
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/22 16:42
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpInfo implements Serializable {
    //注册时的ip
    private String createIp;
    //注册时的ip详情
    private IpDetail createIpDetail;

    //最新登录的ip
    private String updateIp;
    //更新时的ip详情
    private IpDetail updateIpDetail;



    public void refreshIp(String ip) {
        if (StringUtils.isBlank(ip))
                return;
        if (StringUtils.isBlank(createIp)){
            createIp=ip;
        }
        updateIp=ip;
    }

    public String needRefreshIp() {
        boolean notNeedRefresh = Optional.ofNullable(updateIpDetail)
                .map(IpDetail::getIp)
                .filter(ip -> Objects.equals(ip, updateIp))
                .isPresent();
        return notNeedRefresh?null:updateIp;

    }

    public void refreshIpDetail(IpDetail ipDetail) {
        if (Objects.equals(createIp,ipDetail.getIp()))
            createIpDetail=ipDetail;
        if(Objects.equals(updateIp,ipDetail.getIp()))
            updateIpDetail=ipDetail;

    }
}
