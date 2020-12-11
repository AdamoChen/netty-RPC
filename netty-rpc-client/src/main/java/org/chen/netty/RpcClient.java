package org.chen.netty;

import com.adamo.dto.Student;
import com.adamo.dto.Teacher;
import com.adamo.service.dto.Request;
import com.adamo.service.dto.Response;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.chen.constant.StatusCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 10:47
 */
@Component
public class RpcClient{

    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);

    EventLoopGroup workerGroup = new NioEventLoopGroup(1);

    Bootstrap bootstrap = new Bootstrap();

    /**
     * serviceName -> hostPort
     */
    private Map<String, List<String>> serviceNameHostMap;

    /**
     * serviceName -> (host -> channel)
     */
    private Map<String, Map<String, Channel>> serviceNameChannelMap = new ConcurrentHashMap<>(4);

    private Map<String, SynchronousQueue<Response>> resultMap = new ConcurrentHashMap<>();

    public RpcClient(){
        initBootstrap();
    }

    private void initBootstrap() {
        // todo 未来需要测试 handler 共享相关问题
       ChannelHandler clientHandler = new ClientChannelHandler(resultMap, serviceNameChannelMap);
       bootstrap.group(workerGroup)
               .channel(NioSocketChannel.class)
               .option(ChannelOption.TCP_NODELAY, true)
               .option(ChannelOption.SO_KEEPALIVE, true)
               .handler(new ChannelInitializer<SocketChannel>() {
                   @Override
                   protected void initChannel(SocketChannel ch) throws Exception {
                       ChannelPipeline pipeline = ch.pipeline();
                       pipeline.addLast(new JsonEncoder())
                               .addLast(new ResponseJsonDecoder())
                               .addLast(clientHandler);
                   }
               });
    }

    // todo 在注册中心中被调用
    public void updateServiceNameChannel(Map<String, List<String>> map){
        if(map == null){
            log.warn("不存在任何远程服务！");
            // todo 关闭所有channel ?? 应该不需要 因为如果channel还能用则保持，如果不能使用也应该不需要再关闭
            // 目前只清除
            serviceNameChannelMap.clear();
            //workerGroup.shutdownGracefully();
        }else{
            map.forEach((serviceName, hostPorts) -> {
                if(hostPorts == null || hostPorts.size() < 1){
                    log.warn("[{}]服务未发现任何可用实例", serviceName);
                    // 关闭已存在的channel todo 同上 ??
                    serviceNameChannelMap.remove(serviceName);
                }else{
                    hostPorts.forEach((hostPort) ->{
                        Map<String,Channel> channelMap = serviceNameChannelMap.get(serviceName);
                        if(channelMap == null){
                            // 新加入的服务
                            channelMap = new HashMap<>(4);
                            channelMap.put(hostPort, getChannel(hostPort));
                            serviceNameChannelMap.put(serviceName, channelMap);
                            log.info("远程服务首次[{}]加入，主机端口[{}]", serviceName, hostPort);
                        }else{
                            // 判断对应远程主机实例是否存在
                            if(channelMap.get(hostPort) == null || !channelMap.get(hostPort).isOpen()){
                                // 新加入的服务实例
                                channelMap.put(hostPort, getChannel(hostPort));
                                log.info("远程服务[{}]加入，主机端口[{}]", serviceName, hostPort);
                            }else{
                                log.debug("host {} channel 已存在，不需要连接。");
                            }
                        }
                    });
                }
            });
        }
    }

    private Channel getChannel(String hostPort) {
        try {
            String[] hostPortArr = hostPort.split(":");
            ChannelFuture channelFuture = bootstrap.connect(hostPortArr[0], Integer.valueOf(hostPortArr[1])).sync();
            return channelFuture.channel();
        } catch (Exception e) {
            log.error("连接远程服务失败，主机端口[{}]", hostPort, e);
            return null;
        }
    }

    public Response send(Request request, String serviceName) throws Exception {
        Collection<Channel> channels = serviceNameChannelMap.get(serviceName).values();
        if (channels == null || channels.size() < 1) {
            log.error("不存在[{}]远程服务实例", serviceName);
            throw new Exception("不存在["+serviceName+"]远程服务实例");
        }
        // todo 这里的服务发现处理可能存在变动
        int max = channels.size();
        Random random = new Random();
        // 获取主机端口的模式 当前为随机 后面考虑可以定制
        Channel channel = (Channel) channels.toArray()[random.nextInt(max)];
        if(channel.isActive()){
            // 通过SynchronousQueue 变异步为同步
            SynchronousQueue<Response> queue = new SynchronousQueue<>();
            resultMap.put(request.getRequestId(), queue);
            channel.writeAndFlush(request);
            return queue.take();
        }else{
            Response response = new Response(request.getRequestId(), request.isHeartBeat());
            response.setCode(StatusCodeEnum.FAILURE.code);
            response.setErrorMsg("未连接到远程服务");
            return response;
        }
    }

    // todo test
    public static void main(String[] args) {
        RpcClient client = new RpcClient();
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        String serviceName = "service-bbb";
        String hostPort = "127.0.0.1:8888";
        list.add(hostPort);
        map.put(serviceName, list);
        // ------------
        client.updateServiceNameChannel(map);

        Request request = new Request();
        //request.setRequestId(System.currentTimeMillis()+"");
        //request.setHeartBeat(false);

        Student student = new Student();
        student.setClazzName("三班");
        student.setId(1);
        student.setName("张三");

        Teacher t = new Teacher();
        t.setClassName("三班");
        t.setName("李四");
        student.setClassTeacher(t);
        List<String> courses = new ArrayList<>();
        courses.add("语言");
        courses.add("数学");
        courses.add("天文");
        student.setCourse(courses);

        request.setRequestId(System.currentTimeMillis()+"");
        request.setArgs(new Object[]{student});
        request.setArgsType(new Class<?>[]{Student.class});
        request.setClassFullName("com.adamo.service.StudentService");
        request.setMethodName("addStudent");
        request.setHeartBeat(false);

        try {
            Response response = client.send(request, serviceName);
            System.out.println(JSONObject.toJSON(response));

            request.setMethodName("findAllStudent");
            request.setArgs(null);
            request.setArgsType(null);
            Response response2 = client.send(request, serviceName);
            System.out.println(JSONObject.toJSON(response2));


            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}