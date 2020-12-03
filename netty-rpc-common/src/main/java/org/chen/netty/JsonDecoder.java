package org.chen.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.List;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 20:10
 */
public class JsonDecoder extends LengthFieldBasedFrameDecoder {

    public JsonDecoder() {
        super(Integer.MAX_VALUE, 0, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
        if(byteBuf == null){
            return null;
        }
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        // 这里的解析小技巧
        Object obj = JSONObject.parse(bytes);
        return obj;
    }
}