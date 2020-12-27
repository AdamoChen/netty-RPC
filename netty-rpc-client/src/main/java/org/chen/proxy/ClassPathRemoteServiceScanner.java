package org.chen.proxy;

import org.chen.annotation.RemoteService;
import org.chen.surpport.ServiceNameCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * todo ccg 基于spring 扫描 很关键的一个类
 */
public class ClassPathRemoteServiceScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathRemoteServiceScanner.class);

    private Class<? extends Annotation> annotationClass;

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * 必须要重写的constructor
     * @param registry
     */
    public ClassPathRemoteServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * todo ccg 此方法很重要
     * @param basePackages
     * @return
     */
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        try {
            Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
            if (beanDefinitionHolders.isEmpty()) {
                log.warn("没有找到远程服务相关的接口！{}", Arrays.toString(basePackages));
            }else{
                // ccg 此处为把接口转换为实体描述类的关键所在 GenericBeanDefinition 需要仔细看一下
                for (BeanDefinitionHolder holder : beanDefinitionHolders) {
                    //缓存serviceName
                    RemoteService remoteService = Class.forName(holder.getBeanDefinition().getBeanClassName()).getAnnotation(RemoteService.class);
                    ServiceNameCache.addServiceName(remoteService.value());

                    GenericBeanDefinition beanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
                    // ccg 此处的代码 ???
                    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
                    beanDefinition.setBeanClass(RemoteServiceFactoryBean.class);
                    beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                    log.info("生成的beanDefinition：{}",beanDefinition);
                }
            }
            return beanDefinitionHolders;
        } catch (Exception e) {
            log.error("远程服务接口扫描异常", e);
        }
        return null;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition)  {
        // todo ccg  这个逻辑是否有必要重写
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    public void registerFilters(){
        boolean acceptAll = true;
        if(annotationClass != null){
            addIncludeFilter(new AnnotationTypeFilter(annotationClass));
            acceptAll = false;
        }

        if(acceptAll){
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }
}
