package org.chen.netty;

import com.adamo.service.dto.Response;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/2 20:10
 */
public class ResponseJsonDecoder extends LengthFieldBasedFrameDecoder {

    public ResponseJsonDecoder() {
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
        // 这里必须要解析成具体类的实例 否则下一步强转会失败
        return JSONObject.parseObject(bytes, Response.class, null);
    }
}