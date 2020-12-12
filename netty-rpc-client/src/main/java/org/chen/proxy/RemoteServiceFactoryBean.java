package org.chen.proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * todo ccg 未完成内容
 */
@Component
public class RemoteServiceFactoryBean<T> implements FactoryBean<T> {

    private Class<?> rpcInterface;

    public RemoteServiceFactoryBean() {}

    /**
     * ccg
     * @param rpcInterface
     */
    public RemoteServiceFactoryBean(Class<?> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @Autowired
    RpcInvocationHandler rpcInvocationHandler;

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(),
                new Class<?>[]{rpcInterface}, rpcInvocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }

}
