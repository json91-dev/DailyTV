package com.example.user.dailytv.NettyChatting;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientHandler extends  SimpleChannelInboundHandler<String> {

	@Override
	public void channelRead0(ChannelHandlerContext arg0,String arg1) throws Exception{
	
		System.out.println(arg1);
	}

}
