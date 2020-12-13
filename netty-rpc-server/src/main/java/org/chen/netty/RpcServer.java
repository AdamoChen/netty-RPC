package org.chen.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.chen.annotation.RemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    private ApplicationContext applicationContext;

    /**
     * 远程服务map
     */
    private final Map<String, Object> remoteServiceMap = new HashMap<>(16);

   // @Autowired
   // private DiscoveryClient discoveryClient;

    @Value("${rpc.server.port:8888}")
    private String serviceProviderPort;

    public RpcServer() {}

    public void init(){
        initService();
        bootNettyServer();
        // todo 把服务注册到注册中心
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
    private void initService() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RemoteService.class);
        beans.values().stream().forEach(b -> {
            Class<?> clazz = b.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {
                remoteServiceMap.put(i.getName(), b);
                log.info("初始化服务接口-{}, 实例-{}", i.getName(), b);
            });
        });
        log.info("所有服务接口已加载完成");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    // 用于测试
    public static void main(String[] args) {
        RpcServer server = new RpcServer();
        server.serviceProviderPort = "8888";
        server.init();
    }
}