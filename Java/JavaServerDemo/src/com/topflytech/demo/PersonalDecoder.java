package com.topflytech.demo;

import com.topflytech.codec.PersonalAssetMsgDecoder;
import com.topflytech.codec.entities.Message;
import com.topflytech.codec.entities.MessageEncryptType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by admin on 2017/3/1.
 */
public class PersonalDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        int readableBytes = in.readableBytes();
        if (readableBytes <= 0){
            return;
        }

        PersonalAssetMsgDecoder decoder = new com.topflytech.codec.PersonalAssetMsgDecoder(MessageEncryptType.NONE,null);
        List<Message> messageList = decoder.decode(in.readBytes(in.readableBytes()).array());
        list.addAll(messageList);
    }
}
