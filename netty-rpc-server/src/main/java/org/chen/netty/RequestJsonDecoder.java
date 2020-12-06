package org.chen.netty;

import com.adamo.service.dto.Request;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 20:10
 */
public class RequestJsonDecoder extends LengthFieldBasedFrameDecoder {

    public RequestJsonDecoder() {
        // todo 这里的代码需要再掌握一下， 包括所有编解器相关的内容
        super(Integer.MAX_VALUE, 0, 4, 0, 4);
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
        return JSONObject.parseObject(bytes, Request.class, null);
    }
}