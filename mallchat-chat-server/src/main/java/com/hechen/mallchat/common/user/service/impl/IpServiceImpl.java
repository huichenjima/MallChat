package com.hechen.mallchat.common.user.service.impl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hechen.mallchat.common.common.domain.vo.resp.ApiResult;
import com.hechen.mallchat.common.common.utils.JsonUtils;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.IpDetail;
import com.hechen.mallchat.common.user.domain.entity.IpInfo;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.service.IpService;
import com.hechen.mallchat.common.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: IpServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/22 17:11
 * @Version 1.0
 */
@Service
@Slf4j
public class IpServiceImpl implements IpService, DisposableBean {
    @Autowired
    private UserDao userDao;
    //这个线程池只有一个线程，保证了排队的效果 ，防止淘宝接口请求过多报错
    private static ExecutorService executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500), new NamedThreadFactory("refresh-ipDetail", false));

    @Override
    public void refreshIpDetailAsync(Long uid) {
        executor.execute(()->{
            User user = userDao.getById(uid);
            IpInfo ipInfo = user.getIpInfo();
            if(Objects.isNull(ipInfo))
                return;
            //判断是否需要刷新ip细节并且返回ip
            String ip = ipInfo.needRefreshIp();
            if (StringUtils.isBlank(ip))
                    return;
            IpDetail ipDetail=tryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail))
            {
                ipInfo.refreshIpDetail(ipDetail);
                //从淘宝接口拿到了ip细节下面对数据库进行更新
                User update = User.builder().id(uid).ipInfo(ipInfo).build();
                boolean b = userDao.updateById(update);
            }
        });

    }

    private static IpDetail tryGetIpDetailOrNullTreeTimes(String ip) {
        for (int i = 0; i < 3; i++) {
            IpDetail ipDetail=getIpDetailOrNull(ip);
            if (Objects.nonNull(ipDetail))
                return ipDetail;
            //没有获取到说明淘宝接口使用太频繁，休眠两秒
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("tryGetIpDetailOrNullTreeTimes Exception",e);
            }
        }
        return null;

    }



    private static IpDetail getIpDetailOrNull(String ip) {
        try {
            String url="https://ip.taobao.com/outGetIpInfo?ip="+ip+"&accessKey=alibaba-inc";
            String data= HttpUtil.get(url);
            ApiResult<IpDetail> result = JsonUtils.toObj(data, new TypeReference<ApiResult<IpDetail>>() {

            });
            IpDetail detail = result.getData();
            return detail;

        }catch (Exception e){
            return null;

        }



    }
    //因为是自己的线程池不是spring的，所以要自己实现优雅停机
    @Override //优雅停机
    public void destroy() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {//最多等30秒，处理不完就拉倒
            if (log.isErrorEnabled()) {
                log.error("Timed out while waiting for executor [{}] to terminate", executor);
            }
        }
    }
//测试ip细节获取
//    public static void main(String[] args) {
//        Date begin=new Date();
//        for (int i = 0; i < 100; i++) {
//
//            int finalI = i;
//            executor.execute(()->{
//                IpDetail ipDetail = tryGetIpDetailOrNullTreeTimes("117.85.133.4");
//                if (Objects.nonNull(ipDetail))
//                {
//                    Date date=new Date();
//                    System.out.println(String.format("第%d次成功，目前耗时:%d", finalI,date.getTime()-begin.getTime()));
//                }
//            });
//
//        }
//    }
}
