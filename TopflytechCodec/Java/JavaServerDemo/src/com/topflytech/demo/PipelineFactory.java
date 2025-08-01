package com.topflytech.demo;


import io.netty.channel.ChannelInitializer;

/**
 * @author jerry.chen
 * @version 1.0.0
 * @since 2014-01-22 AM 10:27
 */
public interface PipelineFactory {

    ChannelInitializer createInitializer();

}




