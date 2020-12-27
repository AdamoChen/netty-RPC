package org.chen.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.chen.annotation.RemoteService;
import org.chen.serviceRegiste.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 10:47
 */
@Component
public class RpcServer implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    private ApplicationContext applicationContext;

    /**
     * 远程服务map
     */
    private final Map<String, Object> remoteServiceMap = new HashMap<>(16);

    @Value("${rpc.server.port:8888}")
    private String serviceProviderPort;

    public RpcServer() {}

    public ServiceInstance init(){
        try {
            String serviceName = initService();
            bootNettyServer();
            ServiceInstance instance = new ServiceInstance();
            instance.setServiceName(serviceName);
            instance.setPort(Integer.valueOf(serviceProviderPort));
            instance.setIp(InetAddress.getLocalHost().getHostAddress());
            return instance;
        } catch (UnknownHostException e) {
            log.error("init 异常", e);
            return null;
        }
    }

    private void bootNettyServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        // ccg 注意handler的共享问题
        ChannelHandler serverHandler = new ServerHandler(remoteServiceMap);
       new Thread(()->{
           try {
               ServerBootstrap serverBootstrap = new ServerBootstrap();
               serverBootstrap.group(bossGroup, workGroup)
                       .channel(NioServerSocketChannel.class)
                       .option(ChannelOption.SO_BACKLOG, 1024)
                       .childOption(ChannelOption.SO_KEEPALIVE, true)
                       .childOption(ChannelOption.TCP_NODELAY, true)
                       .childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           protected void initChannel(SocketChannel ch) throws Exception {
                               ChannelPipeline pipeline = ch.pipeline();
                               pipeline.addLast(new IdleStateHandler(0,0,60))
                                       .addLast(new JsonEncoder())
                                       .addLast(new RequestJsonDecoder())
                                       .addLast(serverHandler);
                           }
                       });

               ChannelFuture future = serverBootstrap.bind(Integer.valueOf(serviceProviderPort)).sync();
               log.info("RPC 服务端启动~~~ 端口：{}", serviceProviderPort);
               future.channel().closeFuture().sync();
           } catch (Exception e) {
               log.error("rpc 服务启动失败 {}", e);
           }
       }).start();

    }

    /**
     * 初始化 注解为远程服务的接口
     */
    private String initService() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RemoteService.class);
        // ccg 需要ip 端口能可以几个channel 应该只能是一个
        String serviceName = null;
        for (Object b : beans.values()) {
            Class<?> clazz = b.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> i : interfaces) {
                if (serviceName == null) {
                    //拿到RemoteService 注解值 serviceName
                    serviceName = i.getAnnotation(RemoteService.class).value();
                }

                // className -> instance
                remoteServiceMap.put(i.getName(), b);
                log.info("初始化服务接口-{}, 实例-{}", i.getName(), b);
            }
        }
        log.info("所有服务接口已加载完成");
        return serviceName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}