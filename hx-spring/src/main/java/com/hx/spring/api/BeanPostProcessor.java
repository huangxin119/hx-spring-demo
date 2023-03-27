package com.hx.spring.api;

/**
 * BeanPostProcessor前后置事件处理器
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean,String beanName);
    Object postProcessAfterInitialization(Object bean,String beanName);
}
