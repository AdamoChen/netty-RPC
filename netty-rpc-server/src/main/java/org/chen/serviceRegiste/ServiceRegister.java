package org.chen.serviceRegiste;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.chen.netty.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServiceRegister {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegister.class);

    @Autowired
    private RpcServer rpcServer;

    @Value("${service.discovery.addr:localhost:8848}")
    private String serveAddr;

    @PostConstruct
    public void registerService(){
        try {
            ServiceInstance serviceInstance = rpcServer.init();
            if(serviceInstance != null) {
                NamingService naming = NamingFactory.createNamingService(serveAddr);
                naming.registerInstance(serviceInstance.getServiceName(), serviceInstance.getIp(), serviceInstance.getPort());
                log.info("[{}]服务注册完成", serviceInstance.getServiceName());
            }
        } catch (NacosException e) {
            log.error("注册服务异常", e);
        }
    }
}
