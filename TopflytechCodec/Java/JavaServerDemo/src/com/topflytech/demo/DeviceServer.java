package com.topflytech.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by admin on 2017/3/1.
 */
public class DeviceServer {
    private Class pipelineFactoryClazz;
    private EventLoopGroup bossLoopGroup;
    private EventLoopGroup workerLoopGroup;
    private ChannelGroup channelGroup;

    public DeviceServer(Class<? extends PipelineFactory> pipelineFactoryType) {
        this.pipelineFactoryClazz = pipelineFactoryType;
        this.bossLoopGroup = new NioEventLoopGroup();
        this.workerLoopGroup = new NioEventLoopGroup();
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public final void startup(int port) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossLoopGroup, workerLoopGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        PipelineFactory pipelineFactory = (PipelineFactory) pipelineFactoryClazz.newInstance();
        ChannelInitializer initializer = pipelineFactory.createInitializer();
        bootstrap.childHandler(initializer);

        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelGroup.add(channelFuture.channel());
        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    public final void shutdown() throws Exception {
        channelGroup.close();
        bossLoopGroup.shutdownGracefully();
        workerLoopGroup.shutdownGracefully();
    }
}
