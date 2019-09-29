package com.example.coap.client;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyClient {

    private Bootstrap bootstrap;
    private ChannelFutureListener channelFutureListener = null;
    private Channel channel;
    private String inetHost;
    private int inetPort;

    public NettyClient(String host, int port) {
        this.inetHost = host;
        this.inetPort = port;
    }

    public void start() {
        bootstrap = new Bootstrap();
        final NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_TIMEOUT, 5 * 1000)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast("Idle", new IdleStateHandler(5, 0, 10, TimeUnit.SECONDS));
                        //ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ClientChannelInboundHandler());
                    }
                });

        channel = bootstrap.connect(inetHost, inetPort)
                .addListener(channelFutureListener = new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        System.out.println("operationComplete: " + future.isSuccess());
                        if (future.isSuccess()) {
                            future.channel().eventLoop().scheduleWithFixedDelay(new Runnable() {
                                @Override
                                public void run() {
                                    send(channel);
                                }
                            }, 0, 5, TimeUnit.SECONDS);
                        } else {
                            //  3秒后重新连接
                            future.channel().eventLoop().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    doConnect();
                                }
                            }, 3, TimeUnit.SECONDS);
                        }
                    }
                }).channel();


        //close(group);
        // send(channel);
    }

    public void close(final NioEventLoopGroup group) {
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                group.shutdownGracefully();
            }
        });
    }

    public Channel getChannel() {
        return channel;
    }

    private void send(Channel channel) {
        if (channel.isActive()) {
            String msg = new Date() + ": hello world!";
            System.out.println(">>> " + msg);
            channel.writeAndFlush(msg)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            System.out.println(">>> " + future.isSuccess());
                        }
                    });
        }
    }

    //  连接到服务端
    public void doConnect() {
        ChannelFuture future = bootstrap.connect(inetHost, inetPort);
        future.addListener(channelFutureListener);
        channel = future.channel();
    }
}
