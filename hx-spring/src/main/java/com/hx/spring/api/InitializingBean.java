package com.hx.spring.api;

/**
 * 初始化bean扩展接口
 */
public interface InitializingBean {
    void afterPropertiesSet();
}
