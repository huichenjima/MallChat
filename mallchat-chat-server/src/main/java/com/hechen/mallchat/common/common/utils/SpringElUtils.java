package com.hechen.mallchat.common.common.utils;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * ClassName: SpringElUtils
 * Package: com.hechen.mallchat.common.common.utils
 * Description:SpringEl表达式的工具类
 *
 * @Author 何琛
 * @Create 2025/3/22 13:25
 * @Version 1.0
 */
public class SpringElUtils {
    private static final ExpressionParser PARSER=new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER=new DefaultParameterNameDiscoverer();

    public static String getMethodKey(Method method){
        return method.getDeclaringClass()+"#"+method.getName();

    }

    public static String parseSpringEl(Method method, Object[] args, String spEl) {
        String[] params = Optional.ofNullable(PARAMETER_NAME_DISCOVERER.getParameterNames(method)).orElse(new String[]{});//解析方法的参数名
        EvaluationContext context = new StandardEvaluationContext();//el解析需要的上下文对象
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);//所有参数都作为原材料扔进去
        }
        Expression expression = PARSER.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }



}
