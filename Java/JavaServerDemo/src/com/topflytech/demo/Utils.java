package com.topflytech.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by admin on 2017/3/1.
 */
public class Utils {

    public interface WriteListener {
        void messageRespond(boolean success);
    }

    public static void write(Channel chl, final ByteBuf buf, final WriteListener listener) {
        if (chl != null && chl.isActive()){
            chl.writeAndFlush(buf).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (listener != null) {
                        listener.messageRespond(future.isSuccess());
                    }
                }
            });
        }
    }

    //use
    public static void write(Channel chl, final byte[] bytes, final WriteListener listener) {
        final ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        try {
            write(chl,buf,listener);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void write(Channel chl, Object object, final WriteListener listener) {
        if (chl != null && chl.isActive()){
            chl.writeAndFlush(object).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (listener != null) {
                        listener.messageRespond(future.isSuccess());
                    }
                }
            });
        }
    }
}
