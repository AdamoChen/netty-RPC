package org.chen.proxy;

import org.chen.annotation.RemoteService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * RemoteService 注解beanDefinition后处理器
 */
@Component
public class RemoteServiceDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    // todo ccg
    private String remoteServicePackage = "com.adamo.service";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        ClassPathRemoteServiceScanner scanner = new ClassPathRemoteServiceScanner(beanDefinitionRegistry);

        scanner.setAnnotationClass(RemoteService.class);
        scanner.registerFilters();
        //todo ccg 一个思考 如果此方法依赖一个bean 会怎样
        scanner.scan(StringUtils.tokenizeToStringArray(remoteServicePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 不做任何处理
    }
}
