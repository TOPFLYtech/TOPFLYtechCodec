package com.topflytech.demo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Created by admin on 2017/3/1.
 */
public class NoObdDevicePipelineFactory implements  PipelineFactory {
    private final int availableProcessors;
    private EventExecutorGroup executors;

    public NoObdDevicePipelineFactory() {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        executors = new DefaultEventExecutorGroup(availableProcessors);
    }
    @Override
    public ChannelInitializer createInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                NoObdDecoder decoder = new NoObdDecoder();
                ObjectAutowireFactory.autoWireFactory().autowire(decoder);
                pipeline.addLast("decoder", decoder);
                final NoObdHandler handler = new NoObdHandler();
                ObjectAutowireFactory.autoWireFactory().autowire(handler);
                pipeline.addLast(executors,"handler", handler);
            }

        };
    }
}
