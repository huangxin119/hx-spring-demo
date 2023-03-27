package com.hx.test.service;

import com.hx.spring.anation.Autowired;
import com.hx.spring.anation.Component;
import com.hx.spring.api.BeanNameAware;
import com.hx.spring.api.InitializingBean;

import java.util.Random;


@Component("testSpring1Service")
public class TestSpring1Service implements BeanNameAware, InitializingBean, TestSpring1Interface {
    /**
     * 依赖注入属性
     */
    @Autowired
    private TestSpring2Interface testSpring2Service;

    private String beanName;

    private Integer randomNum;

    public TestSpring1Service(){
        System.out.println("TestSpring1Service实例化");
    }

    /**
     * 测试属性是否注入
     */
    public void printAutowiredField(){
        System.out.println(testSpring2Service);
    }

    @Override
    public void setName(String name) {
        this.beanName = name;
        System.out.println("1----beanName 已经回调"+beanName);
    }


    @Override
    public void afterPropertiesSet() {
        this.randomNum = new Random().nextInt(1000);
        System.out.println("3----属性注入后执行初始化,randomNum:"+randomNum);
    }

}
