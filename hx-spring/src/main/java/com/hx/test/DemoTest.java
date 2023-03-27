package com.hx.test;

import com.hx.spring.HxApplicationContext;
import com.hx.test.service.TestSpring2Interface;
import com.hx.test.service.TestSpring1Interface;

public class DemoTest {
    public static void main(String[] args) throws ClassNotFoundException {
        HxApplicationContext hxApplicationContext = new HxApplicationContext(ConfigClass.class);
//        //1.测试单例模式
//        System.out.println(hxApplicationContext.getBean("testSpring1Service"));
//        System.out.println(hxApplicationContext.getBean("testSpring1Service"));
//        //2.测试依赖注入
//        ((TestSpring1Interface)hxApplicationContext.getBean("testSpring1Service")).printAutowiredField();
//        //3.测试循环依赖
//        System.out.println(hxApplicationContext.getBean("testSpring1Service"));
//        ((TestSpring1Interface)hxApplicationContext.getBean("testSpring1Service")).printAutowiredField();
//        System.out.println(hxApplicationContext.getBean("testSpring2Service"));
        ((TestSpring2Interface)hxApplicationContext.getBean("testSpring2Service")).printAutowiredField();
        //4.测试spring的各个扩展接口
        TestSpring1Interface testSpring1Interface = (TestSpring1Interface) hxApplicationContext.getBean("testSpring1Service");

        //5.测试spring的aop实现demo
        testSpring1Interface.printAutowiredField();

        //遗留问题？这些日志你能清楚的知道每一行是有哪行代码打印的嘛？
    }
}
