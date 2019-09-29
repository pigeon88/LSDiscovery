package com.example.coap.tcp;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class SimpleChannelInitializer extends ChannelInitializer<Channel> {

    ChannelHandler[] channelHandlers;

    public SimpleChannelInitializer(ChannelHandler... channelHandlers) {
        this.channelHandlers = channelHandlers;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("Idle", new IdleStateHandler(5, 0, 10, TimeUnit.SECONDS));
        //ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        ch.pipeline().addLast(new StringEncoder());
        ch.pipeline().addLast(new StringDecoder());
        ch.pipeline().addLast(channelHandlers);
    }
}
