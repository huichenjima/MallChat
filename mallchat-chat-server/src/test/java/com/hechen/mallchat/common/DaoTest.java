package com.hechen.mallchat.common;

import com.auth0.jwt.interfaces.Claim;
import com.hechen.mallchat.common.common.config.ThreadPoolConfig;
import com.hechen.mallchat.common.common.thread.MyUncaughtExceptionHandler;
import com.hechen.mallchat.common.common.utils.JwtUtils;
import com.hechen.mallchat.common.common.utils.RedisUtils;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * ClassName: DaoTest
 * Package: com.hechen.mallchat.common
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/14 16:36
 * @Version 1.0
 */

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class DaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private WxMpService wxMpServicex;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private LoginService loginService;

    @Autowired
    @Qualifier(ThreadPoolConfig.MALLCHAT_EXECUTOR) //@Qualifier 注解是 Spring 框架中用于解决同一个类型存在多个Bean时，明确指定使用哪一个Bean的方式。
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Test
    public void jwttest(){
        String token = jwtUtils.createToken(1L);
        System.out.println(token);
        Map<String, Claim> stringClaimMap = jwtUtils.verifyToken(token);
        System.out.println(stringClaimMap);
        Long uidOrNull = jwtUtils.getUidOrNull(token);
        System.out.println(uidOrNull);

    }
    @Test
    public void redistest(){

        redisTemplate.opsForValue().set("test1","何琛");
        Object test = redisTemplate.opsForValue().get("test");
        System.out.println(test);
    }
    @Test
    public void redistest2(){
        RedisUtils.set("test2","何琛");
        String test = RedisUtils.getStr("test2");
        System.out.println(test);
    }

    @Test
    public void RedssionTest(){
        RLock lock = redissonClient.getLock("123");
        lock.lock();
        System.out.println();
        lock.unlock();

    }
    @Test
    public void RedisTest2(){
        String s="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjExMDE2LCJjcmVhdGVUaW1lIjoxNzQyMjgzMTI4fQ.OY13RGSU-eHqD3WdyJVKzsoybXCecBWE5nSbLwIKPyw";
        Long validUid = loginService.getValidUid(s);
        System.out.println(validUid);

    }
    @Test
    public void ThreadTest() throws InterruptedException {
        threadPoolTaskExecutor.execute(()->{
            if(1==1){
                log.info("123");
                throw new RuntimeException("123");
            }

        });
        Thread.sleep(200);

    }
    @Test
    public void ThreadTest2() throws InterruptedException {
        Thread thread = new Thread(()->{
            if(1==1){
                log.info("123");
                throw new RuntimeException("123");
            }

        });
        thread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        thread.start();
        Thread.sleep(200);

    }


    @Test
    public void test(){
        User byId = userDao.getById(1);
        System.out.println(byId);
        User user = new User();
        user.setId(new Integer(2).longValue());
        user.setOpenId("123");
        user.setName("hechen1");
        userDao.save(user);


    }
    @Test
    public void test2() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpServicex.getQrcodeService().qrCodeCreateTmpTicket(1, 10000);

        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);


    }
}
