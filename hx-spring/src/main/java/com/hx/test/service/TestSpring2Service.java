package com.hx.test.service;

import com.hx.spring.anation.Autowired;
import com.hx.spring.anation.Component;
import com.hx.spring.api.BeanNameAware;
import com.hx.spring.api.InitializingBean;

@Component("testSpring2Service")
public class TestSpring2Service implements TestSpring2Interface, BeanNameAware, InitializingBean{
    @Autowired
    private TestSpring1Interface testSpring1Service;

    public TestSpring2Service(){
        System.out.println("TestSpring2Service实例化");
    }

    public void printAutowiredField(){
        System.out.println(testSpring1Service);
    }
    @Override
    public void afterPropertiesSet() {
        System.out.println("3----属性注入后执行初始化,InitializingBean----beanName istestSpring2Service"+this.testSpring1Service);
    }



    @Override
    public void setName(String name) {
        System.out.println("1----BeanNameAware 已经回调----beanName istestSpring2Service"+this.testSpring1Service);
    }
}
