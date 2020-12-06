package org.chen.netty;

import com.adamo.service.dto.Response;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.chen.constant.StatusCodeEnum;
import com.adamo.service.dto.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

    private Map<String, Object> serviceMap;

    public ServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
        Request msg = (Request) obj;
        Response response = new Response(msg.getRequestId(), msg.isHeartBeat());
        if (!msg.isHeartBeat()) {
            Object service = serviceMap.get(msg.getClassFullName());
            if(service != null){
                Class<?> serviceClass = service.getClass();
                Method serviceMethod = serviceClass.getMethod(msg.getMethodName(), msg.getArgsType());
                serviceMethod.setAccessible(true);
                // todo ccg 注意的地方 参数类型的问题
                Object result = serviceMethod.invoke(service, msg.getArgs());
                response.setCode(StatusCodeEnum.SUCCESS.code);
                response.setData(JSONObject.toJSONString(result));
            }else{
                log.error("未找服务：{}", msg.getClassFullName());
                //throw new Exception("未找服务：" + msg.getClassFullName());
                response.setCode(StatusCodeEnum.SUCCESS.code);
                response.setErrorMsg("未找到服务："+ msg.getClassFullName() + "#" + msg.getMethodName());
            }
        }else{
            log.info("客户端心跳：{}", ctx.channel().remoteAddress());
            response.setCode(StatusCodeEnum.SUCCESS.code);
            response.setData("response of heart beat");
        }
        ctx.writeAndFlush(response);
    }
}
