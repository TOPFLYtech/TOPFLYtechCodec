package com.topflytech.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * 对象自动装配
 * @author zhenhui.chen
 * @version 1.0.0
 * @since 2013-04-02
 */
public class ObjectAutowireFactory implements BeanFactoryAware {

    private AutowireCapableBeanFactory factory = null;
    private static class SingletonHolder {
        static ObjectAutowireFactory instance = new ObjectAutowireFactory();
    }

    /**
     * get object of  ObjectAutowireFactory
     * @return
     */
    public static ObjectAutowireFactory autoWireFactory() {
        return SingletonHolder.instance;
    }

    /**
     * 创建对象并且完成新实例的自动装配
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T createAndAutowire(Class<T> clazz) {
        T instance = null;

        try {
            instance = clazz.newInstance();
            autowire(instance);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return instance;
    }

    /**
     *
     * @param object 要装配的对象
     */
    public void autowire(Object object) {
        if (factory != null) {
            factory.autowireBeanProperties(object, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        } else {
            System.out.println("No " + AutowireCapableBeanFactory.class.getName() + " has been defined. Autowiring will not work.");
        }
    }

    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.factory = (AutowireCapableBeanFactory) factory;
    }
}