package com.hx.test.service;


import com.hx.spring.anation.Component;
import com.hx.spring.api.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("postSpringService")
public class PostSpringService implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)  {
        if(bean instanceof TestSpring1Interface){
            System.out.println("2----前置处理器执行,bean is "+bean.toString()+"---beanName is "+beanName);
        }


        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)  {
        //aop实现原理
        if(bean instanceof TestSpring1Interface){
            System.out.println("4----后置处理器执行,bean is "+bean.toString()+"---beanName is "+beanName);
            Object proxyInstance = Proxy.newProxyInstance(PostSpringService.class.getClassLoader(),bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("aop统一处理逻辑");
                    return method.invoke(bean,args);
                }
            });
            System.out.println("代理bean is"+proxyInstance);
            return proxyInstance;
        }
        return bean;
    }
}
