package org.chen.serviceDiscovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import org.chen.netty.RemoteProcedureCallClient;
import org.chen.surpport.ServiceNameCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);

    @Value("${service.discovery.addr:localhost:8848}")
    private String serveAddr;

    @Autowired
    private RemoteProcedureCallClient callClient;

    @PostConstruct
    private void registerService(){
        try {
            NamingService naming = NamingFactory.createNamingService(serveAddr);
            ServiceNameCache.getServiceNames().forEach(serviceName->{
                 try {
                     log.info("注册监听[{}]服务至nacos", serviceName);
                     naming.subscribe(serviceName, event -> {
                         Map<String, List<String>> serviceMap = new HashMap<>(2);
                         if (event instanceof NamingEvent) {
                             List<String> hostPorts = new ArrayList<>();
                             ((NamingEvent) event).getInstances().forEach(e ->{
                                 hostPorts.add(e.getIp() + ":" + e.getPort());
                             });
                             serviceMap.put(serviceName, hostPorts);

                         }
                        callClient.updateServiceNameChannel(serviceMap);
                     });
                 } catch (NacosException e) {
                     log.error("注册nacos监听异常", e);
                 }
            });
        } catch (NacosException e) {
            log.error("获取ancos NamegService异常", e);
        }

    }

}
