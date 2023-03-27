package com.example.springtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Resource;

@SpringBootApplication
public class SpringTestApplication {

    @Resource
    private ApplicationContext applicationContext;


    public static void main(String[] args) {
//        SpringApplication springApplication = new SpringApplication(SpringTestApplication.class);
//        springApplication.setWebApplicationType(WebApplicationType.SERVLET);
//        springApplication.run(args);
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringTestApplication.class, args)) {
            System.out.println(applicationContext);
//            TestSpring11Service helloService = applicationContext.getBean(TestSpring11Service.class);
//            helloService.printAutowiredField();
        }
    }

}
