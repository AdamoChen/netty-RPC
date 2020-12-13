package org.chen.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 20:10
 */
public class JsonEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        byte[] bytes = JSONObject.toJSONBytes(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}