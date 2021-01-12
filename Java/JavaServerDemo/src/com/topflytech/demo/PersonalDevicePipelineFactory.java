package com.topflytech.demo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Created by admin on 2017/3/1.
 */
public class PersonalDevicePipelineFactory implements  PipelineFactory {
    private final int availableProcessors;
    private EventExecutorGroup executors;

    public PersonalDevicePipelineFactory() {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        executors = new DefaultEventExecutorGroup(availableProcessors);
    }
    @Override
    public ChannelInitializer createInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                PersonalDecoder decoder = new PersonalDecoder();
                ObjectAutowireFactory.autoWireFactory().autowire(decoder);
                pipeline.addLast("decoder", decoder);
                final PersonalHandler handler = new PersonalHandler();
                ObjectAutowireFactory.autoWireFactory().autowire(handler);
                pipeline.addLast(executors,"handler", handler);
            }

        };
    }
}
