package com.example.demo.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by Administrator on 2017/8/2.
 */
public class SimpleChatServerHandler extends SimpleChannelInboundHandler<String>{

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     *
     * @param ctx
     * @throws Exception
     * 覆盖了 handlerAdded() 事件处理方法
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入ChannelGroup列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
        Channel incoming = ctx.channel();
        for (Channel channel : channels){
            channel.writeAndFlush("[SERVER]-"+incoming.remoteAddress()+"加入\n");
        }
        channels.add(ctx.channel());
    }

    /**
     *
     * @param ctx
     * @throws Exception
     * 覆盖了 handlerRemoved() 事件处理方法。每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
        Channel incoming = ctx.channel();
        for (Channel channel : channels){
            channel.writeAndFlush("[SERVER]-"+incoming.remoteAddress()+"离开\n");
        }
        channels.remove(ctx.channel());
    }

    /**
     *
     * @param ctx
     * @param s
     * @throws Exception
     * 覆盖了 channelRead0() 事件处理方法。每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {

        Channel incoming = ctx.channel();
        for (Channel channel : channels){
            if (channel != incoming){
                channel.writeAndFlush("["+incoming.remoteAddress()+"]"+s+"\n");

            }else{
                channel.writeAndFlush("[you]"+s+"\n");
            }
        }

    }

    /**
     *
     * @param ctx
     * @throws Exception
     * 覆盖了 channelActive() 事件处理方法。服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"在线");
    }

    /**
     *
     * @param ctx
     * @throws Exception
     * 覆盖了 channelInactive() 事件处理方法。服务端监听到客户端不活动
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"掉线");
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (7)
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
