package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationContext 提供者
 * 用于在非Spring管理的类中获取Bean
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
    
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }
}
