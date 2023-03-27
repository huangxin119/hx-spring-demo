package com.example.springtest.service;



import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.Random;


@Component("testSpring1Service")
public class TestSpring11Service implements TestSpring1Interface, BeanNameAware, InitializingBean  {
    /**
     * 依赖注入属性
     */
    @Resource
    private TestSpring2Interface testSpring2Service;

    private String beanName;

    private Integer randomNum;

    public TestSpring11Service(){
        System.out.println("TestSpring1Service实例化");
    }

    /**
     * 测试属性是否注入
     */
    public void printAutowiredField(){
        System.out.println(testSpring2Service);
    }




    @Override
    public void afterPropertiesSet() {
        this.randomNum = new Random().nextInt(1000);
        System.out.println("3----属性注入后执行初始化,randomNum:"+this.testSpring2Service);
    }



    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("1----beanName 已经回调"+this.testSpring2Service);
    }




}
