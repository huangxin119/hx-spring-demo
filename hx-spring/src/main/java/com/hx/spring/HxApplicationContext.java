package com.hx.spring;



import com.hx.spring.anation.Autowired;
import com.hx.spring.anation.Component;
import com.hx.spring.anation.ComponentScan;
import com.hx.spring.anation.Scope;
import com.hx.spring.api.BeanFactory;
import com.hx.spring.api.BeanNameAware;
import com.hx.spring.api.BeanPostProcessor;
import com.hx.spring.api.InitializingBean;

import javax.naming.spi.ObjectFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文对象
 */
public class HxApplicationContext {
    //配置类
    private Class configClass;
    //bean单例池一级缓存，存储单例bean
    private Map<String,Object> singletonBeanMap = new ConcurrentHashMap<>();
    //bean单例池二级缓存，存储未完成状态的单例bean,解决并发获取bean性能的问题
    private Map<String,Object> earlySingletonBeanMap = new HashMap<>();
    //bean单例池三级缓存，存储需要aop处理的单例bean,优雅解决循环依赖中的aop功能
    private Map<String, BeanFactory> factorySingletonBeanMap = new HashMap<>();
    //beanDefinitionMap，存储所有需要初始化bean的类信息
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * c传入配置类，构建spring上下文
     * @param configClass
     * @throws ClassNotFoundException
     */
    public HxApplicationContext(Class configClass) throws ClassNotFoundException {
        this.configClass = configClass;
        singletonBeanMap = new ConcurrentHashMap<>();
        beanDefinitionMap = new ConcurrentHashMap<>();
        //扫描文件生成BeanDefinition,注册BeanPostProcessor处理器
        scan(configClass);
        //注册beanPostProcessorList
        //生成单例Bean
        for(Map.Entry<String,BeanDefinition> entry:beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if("singleton".equals(beanDefinition.getScope())){
                createBean(beanDefinition,beanName);
            }
        }


    }

    /**
     * 根据配置类信息扫描包路径，把所有@Component的类全部加载，类信息存入beanDefinitionMap
     * @param configClass
     * @throws ClassNotFoundException
     */
    private void scan(Class configClass) throws ClassNotFoundException {
        //解析config配置,将扫描路径下所有加入@Component的类全部加载
        //1.拿到扫描路径
        ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String scanPath = componentScan.value();
        //System.out.println("扫描包路径 ---- "+scanPath);
        //2.取出扫描路径的下面所有类
        ClassLoader classLoader = HxApplicationContext.class.getClassLoader();
        //System.out.println("扫描包文件名 ---- "+scanPath.replaceAll("\\.","/"));
        URL url = classLoader.getResource(scanPath.replaceAll("\\.","/"));
        File file = new File(url.getFile());
        if(file.isDirectory()){
            File[] files = file.listFiles();
            //遍历所有class查询是否为需要装配的bean
            for (File f:files) {
                //System.out.println(f);
                String className = f.toString().substring(f.toString().indexOf("com"),f.toString().indexOf(".class")).replace("\\",".");
                //System.out.println("扫描的类名 ---- "+className);
                Class<?> fileClass = classLoader.loadClass(className);
                //将bean装配
                if(fileClass.isAnnotationPresent(Component.class)){
                    Component component = fileClass.getAnnotation(Component.class);
                    String beanName = component.value();
                    //System.out.println("beanName ---- "+beanName);
                    //先生成BeanDefinition
                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setClazz(fileClass);
                    if(fileClass.isAnnotationPresent(Scope.class)){
                        beanDefinition.setScope(fileClass.getAnnotation(Scope.class).value());
                    }else{
                        beanDefinition.setScope("singleton");
                    }
                    beanDefinitionMap.put(beanName,beanDefinition);
                }
                //如果是BeanPostProcessor处理器，初始化存入
                if(BeanPostProcessor.class.isAssignableFrom(fileClass)){
                    //1.实例化bean对象（这里可以优化为使用getBean创建）
                    try {
                        BeanPostProcessor beanPostProcessor = (BeanPostProcessor) fileClass.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(beanPostProcessor);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 利用反射创建bean对象,，注意bean创建流程！！！
     * @param beanDefinition
     * @param beanName
     * @return
     */

    public Object getSingletonBean(BeanDefinition beanDefinition,String beanName){
        //先从一级缓存取
        Object singletonObject = singletonBeanMap.get(beanName);
        if(singletonObject!=null){
            return singletonObject;
        }
        synchronized (singletonBeanMap){
            //再从二级缓存取
            singletonObject = earlySingletonBeanMap.get(beanName);
            //如果三级缓存存在
            if(singletonObject==null){
                BeanFactory singletonFactory = factorySingletonBeanMap.get(beanName);
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    earlySingletonBeanMap.put(beanName, singletonObject);
                    factorySingletonBeanMap.remove(beanName);
                }else {
                    //说明bean未被创建，进行创建流程
                    try {
                        //1.实例化bean对象
                        Object object = beanDefinition.getClazz().getDeclaredConstructor().newInstance();
                        System.out.println("实例化bean is"+object);
                        //暴露进入三级缓存，注意这里的aop流程还未执行
                        final Object instance = object;
                        factorySingletonBeanMap.put(beanName, new BeanFactory() {
                            @Override
                            public Object getObject() {
                                Object aopInstance = instance;
                                //3.检查执行aware回调
                                if(aopInstance instanceof BeanNameAware){
                                    ((BeanNameAware)aopInstance).setName(beanName);
                                }
                                //4.执行BeanPostProcessor前置处理器，注意这里可能改变instance对象，有多个BeanPostProcessor注意顺序（@Order）
                                for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                                    aopInstance = beanPostProcessor.postProcessBeforeInitialization(aopInstance,beanName);
                                }
                                //5.执行InitializingBean初始化
                                if(aopInstance instanceof InitializingBean){
                                    ((InitializingBean) aopInstance).afterPropertiesSet();
                                }
                                //6.执行BeanPostProcessor后置处理器，注意这里可能改变instance对象
                                for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                                    aopInstance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
                                }
                                return aopInstance;
                            }
                        });
                        //2.依赖注入字段值
                        for(Field field:beanDefinition.getClazz().getDeclaredFields()){
                            if (field.isAnnotationPresent(Autowired.class)) {
                                field.setAccessible(true);
                                Object autowiredBean = getBean(field.getName());
                                field.set(instance,autowiredBean);
                            }
                        }
                        Object aopInstance = getSingletonBean(beanDefinition,beanName);
                        //单例bean放入缓存map
                        if("singleton".equals(beanDefinition.getScope())){
                            singletonBeanMap.put(beanName,aopInstance);
                            earlySingletonBeanMap.remove(beanName);
                            factorySingletonBeanMap.remove(beanName);
                        }
                        return aopInstance;
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return singletonObject;
        }
    }

    public Object createBean(BeanDefinition beanDefinition,String beanName){
        if("singleton".equals(beanDefinition.getScope())){
            return getSingletonBean(beanDefinition, beanName);
        }else{
            //1.实例化bean对象
            try {
                Object instance = beanDefinition.getClazz().getDeclaredConstructor().newInstance();
                //3.检查执行aware回调
                if(instance instanceof BeanNameAware){
                    ((BeanNameAware)instance).setName(beanName);
                }
                //4.执行BeanPostProcessor前置处理器，注意这里可能改变instance对象，有多个BeanPostProcessor注意顺序（@Order）
                for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                    instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
                }
                //5.执行InitializingBean初始化
                if(instance instanceof InitializingBean){
                    ((InitializingBean) instance).afterPropertiesSet();
                }
                //6.执行BeanPostProcessor后置处理器，注意这里可能改变instance对象
                for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                    instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
                }
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 从spring容器中取出bean对象
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object getBean(String beanName) {
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if("singleton".equals(beanDefinition.getScope())){
                return getSingletonBean(beanDefinition,beanName);
            }else{
                return createBean(beanDefinition,beanName);
            }
        }
        //beanDefinitionMap不包含改类信息
        return null;
    }

}
