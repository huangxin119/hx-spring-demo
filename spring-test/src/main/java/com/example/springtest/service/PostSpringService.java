package com.example.springtest.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class PostSpringService implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof TestSpring1Interface){
            System.out.println("2----前置处理器执行----beanName is "+beanName);
        }
        if(bean instanceof TestSpring2Interface){
            System.out.println("2----前置处理器执行----beanName is "+beanName);
        }


        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if("testSpring1Service".equals(beanName)){
            System.out.println("4----后置处理器执行----beanName is "+beanName);
        }
        if("testSpring2Service".equals(beanName)){
            System.out.println("4----后置处理器执行----beanName is "+beanName);
        }
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
            return proxyInstance;
        }
//        //aop实现原理
//        if(bean instanceof TestSpring2Interface){
//            System.out.println("4----后置处理器执行,bean is "+bean.toString()+"---beanName is "+beanName);
//            Object proxyInstance = Proxy.newProxyInstance(PostSpringService.class.getClassLoader(),bean.getClass().getInterfaces(), new InvocationHandler() {
//                @Override
//                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                    System.out.println("aop统一处理逻辑");
//                    return method.invoke(bean,args);
//                }
//            });
//            return proxyInstance;
//        }

        return bean;
    }
}
