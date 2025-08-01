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
    public static String bytes2HexString(byte[] bytes, int index) {
        if (bytes != null && bytes.length > 0 && index < bytes.length) {
            StringBuilder builder = new StringBuilder("");

            for(int i = index; i < bytes.length; ++i) {
                String hex = Integer.toHexString(bytes[i] & 255);
                if (hex.length() < 2) {
                    builder.append('0');
                }

                builder.append(hex);
            }

            return builder.toString();
        } else {
            return null;
        }
    }

    public static byte[] hexString2Bytes(String hexStr) {
        String hex = hexStr.replace("0x", "");
        StringBuffer buffer = new StringBuffer(hex);
        if (buffer.length() % 2 != 0) {
            buffer.insert(0, '0');
        }

        int size = buffer.length() / 2;
        byte[] bytes = new byte[size];

        for(int i = 0; i < size; ++i) {
            bytes[i] = (byte)Integer.parseInt(buffer.substring(i * 2, (i + 1) * 2), 16);
        }

        return bytes;
    }
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
