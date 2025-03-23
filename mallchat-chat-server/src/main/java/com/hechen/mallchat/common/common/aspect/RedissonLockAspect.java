package com.hechen.mallchat.common.common.aspect;

import com.hechen.mallchat.common.common.annotation.RedissonLock;
import com.hechen.mallchat.common.common.service.LockService;
import com.hechen.mallchat.common.common.utils.SpringElUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName: RedissonLockAspect
 * Package: com.hechen.mallchat.common.common.aspect
 * Description:分布式锁的切面
 *
 * @Author 何琛
 * @Create 2025/3/22 13:08
 * @Version 1.0
 */
@Component
@Aspect
@Order(0) //确保比事务注解先执行，分布式锁在事务外，即这个注解应该比transactial先生效
public class RedissonLockAspect {
    @Autowired
    private LockService lockService;

    @Around("@annotation(redissonLock)") //表示在加了该注解的方法上执行这个切点方法 ，还有一种是直接指定方法 ，"execution(* com.yourpackage..*.*(..))"
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock)throws Throwable{
        //注解标识的方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String prefix= StringUtils.isBlank(redissonLock.prefixKey())? SpringElUtils.getMethodKey(method):redissonLock.prefixKey();
        String key=SpringElUtils.parseSpringEl(method,joinPoint.getArgs(),redissonLock.key());
        return lockService.executeWithLock(prefix + ":" + key, redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }
//    //测试el表达式
//    public static void main(String[] args) {
//
//        List<Integer> primes = new ArrayList<Integer>();
//        primes.addAll(Arrays.asList(2,3,5,7,11,13,17));
//
//        // 创建解析器
//        ExpressionParser parser = new SpelExpressionParser();
//        //构造上下文
//        StandardEvaluationContext context = new StandardEvaluationContext();
//        context.setVariable("primes",primes);
//
//        //解析表达式
//        Expression exp =parser.parseExpression("#primes.?[#this>10]");
//        // 求值
//        List<Integer> primesGreaterThanTen = (List<Integer>)exp.getValue(context);
//    }
}
