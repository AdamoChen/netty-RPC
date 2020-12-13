package org.chen.netty;

import com.adamo.service.dto.Request;
import com.adamo.service.dto.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;

@ChannelHandler.Sharable
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClientChannelHandler.class);

    private Map<String, SynchronousQueue<Response>> resultMap;

    private Map<String, Map<String, Channel>> serviceNameChannelMap;

    public ClientChannelHandler(Map<String, SynchronousQueue<Response>> resultMap,
                                Map<String, Map<String, Channel>> serviceNameChannelMap) {
        this.resultMap = resultMap;
        this.serviceNameChannelMap = serviceNameChannelMap;
    }

    public ClientChannelHandler(Map<String, SynchronousQueue<Response>> resultMap) {
        this.resultMap = resultMap;
    }

    public ClientChannelHandler() { }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        Response response = (Response) msg;
        if(response.isHeartBeat()){
            log.info("RPC服务端[{}]心跳响应: {}", ctx.channel().remoteAddress(), response.getData());
            return;
        }
        SynchronousQueue<Response> queue = resultMap.get(response.getRequestId());
        queue.put(response);
        resultMap.remove(response.getRequestId());
        // todo 后期考虑 某个id一直没有返回的异常问题 导致发送方死锁的问题
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("连接到远程服务[{}]", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        serviceNameChannelMap.values().stream()
                .filter(m -> m.get(ctx.channel().remoteAddress()) != null)
                .findFirst()
                .get()
                .remove(ctx.channel().remoteAddress());
        log.info("RPC服务[{}]关闭连接", ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        log.info("RPC客户端向[{}]发送心跳", ctx.channel().remoteAddress());
        if(evt instanceof IdleStateEvent){
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if(stateEvent.state() == IdleState.ALL_IDLE){
                Request request = new Request();
                request.setHeartBeat(true);
                ctx.writeAndFlush(request);
            }
        }
    }


}
