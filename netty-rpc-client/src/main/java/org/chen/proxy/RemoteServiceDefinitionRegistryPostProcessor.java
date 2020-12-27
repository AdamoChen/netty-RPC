package org.chen.proxy;

import org.chen.annotation.RemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * RemoteService 注解beanDefinition后处理器
 */
@Component
public class RemoteServiceDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(RemoteServiceDefinitionRegistryPostProcessor.class);

    //private Environment environment;

    /**
     * 不能用@Value注入 因为在接口被调用时还没有到set值的时间段 值为null
     */
    private String remoteServicePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if(remoteServicePackage == null || "".equals(remoteServicePackage)){
            log.warn("未指定外部远程服务接口包扫描路径，无法使用远程接口。");
            return;
        }
        ClassPathRemoteServiceScanner scanner = new ClassPathRemoteServiceScanner(beanDefinitionRegistry);

        scanner.setAnnotationClass(RemoteService.class);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(remoteServicePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 不做任何处理
    }

    @Override
    public void setEnvironment(Environment environment) {
        //this.environment = environment;
        this.remoteServicePackage = environment.getProperty("remote.service.package.scanner");
    }

}
