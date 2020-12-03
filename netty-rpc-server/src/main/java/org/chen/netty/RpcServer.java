package org.chen.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.chen.annotation.RemotServcie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    private final Map<Class, Object> serviceMap = new HashMap<>(16);

   // @Autowired
   // private DiscoveryClient discoveryClient;

    public RpcServer() {}

    public void start(){
        initService();
        bootNettyServer();
    }

    private void bootNettyServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        // todo
        ChannelHandler serverHander = null;
       new Thread(()->{
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

                           pipeline.addLast(new JsonEncoder())
                                   .addLast(new JsonDecoder())
                                   .addLast(serverHander);
                       }
                   });

       }).start();



    }

    private void initService() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RemotServcie.class);
        beans.keySet().stream().forEach(b -> {
            Class<?> clazz = b.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {
                serviceMap.put(i, b);
                log.info("初始化服务接口-{}, 实例-{}", i, b);
            });
        });
        log.info("所有服务接口已加载完成-{}", serviceMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}