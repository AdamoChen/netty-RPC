package org.chen.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
    ;
    private final Map<String, Object> serviceMap = new HashMap<>(16);

   // @Autowired
   // private DiscoveryClient discoveryClient;

    @Value("${rpc.server.port:8888}")
    private String RPCPort;

    public RpcServer() {}

    public void init(){
        initService();
        bootNettyServer();
    }

    private void bootNettyServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        // todo 这个共享问题已经出现了
        ChannelHandler serverHandler = new ServerHandler(serviceMap);
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

                               pipeline.addLast(new JsonEncoder())
                                       .addLast(new RequestJsonDecoder())
                                       .addLast(serverHandler);
                           }
                       });

               ChannelFuture future = serverBootstrap.bind(Integer.valueOf(RPCPort)).sync();
               log.info("RPC 服务端启动~~~ 端口：{}",RPCPort);
               future.channel().closeFuture().sync();
           } catch (Exception e) {
               log.error("rpc 服务启动失败 {}", e);
           }
       }).start();

    }

    private void initService() {
        // todo 后面需要找开注释
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RemoteService.class);
        beans.values().stream().forEach(b -> {
            Class<?> clazz = b.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {
                serviceMap.put(i.getName(), b);
                log.info("初始化服务接口-{}, 实例-{}", i.getName(), b);
            });
        });
        log.info("所有服务接口已加载完成");
    }

    // todo test
    public static void main(String[] args) {
        RpcServer server = new RpcServer();
        server.RPCPort = "8888";
        server.init();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }


}