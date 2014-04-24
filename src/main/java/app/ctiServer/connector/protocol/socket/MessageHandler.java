package app.ctiServer.connector.protocol.socket;


import java.util.Arrays;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import app.ctiServer.connector.protocol.socket.ProtocolFrame.FrameType;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.Request;

import component.util.Future;
import component.util.Util;


class MessageProcessor implements Runnable
{
	ChannelHandlerContext ctx;
	MessageEvent e;
	MessageHandler handler;

	ProtocolMgr protocolMgr;
	MessageProcessor(ChannelHandlerContext ctx,MessageEvent e,MessageHandler handler)
	{
		this.ctx=ctx;
		this.e=e;
		this.handler=handler;
		this.protocolMgr=Util.getBean("protocol",ProtocolMgr.class);
	}
	@Override
	public String toString()
	{
		Object o=e.getMessage();
		if (o instanceof ProtocolFrame)
		{
			ProtocolFrame frame=(ProtocolFrame)e.getMessage();
			return String.format("{%s (%d) %s.%s}",frame.frameType,frame.invokeId,frame.packet.object,frame.packet.method);
		}
		return o.toString();
	}
	
	@Override
	public void run() 
	{
		//ConnectorMgr connectorMgr=this.handler.connectorMgr;		
		ProtocolFrame frame=(ProtocolFrame)e.getMessage();
		Channel channel=e.getChannel();
		Util.trace(this,"//processing request msg on channel %s...(%d) %d",channel.hashCode(),frame.invokeId,this.protocolMgr.executor.getActiveCount());
		Object result=null;
		Request request=null;
		try
		{
			//it must be a request frame
			Util.check(frame.frameType==FrameType.Request, "invalid frameType:%s", frame.frameType);
			request=(Request)frame.packet;
			//SessionInfo si=protocolMgr.connectorMgr.getSessionByChannel(""+channel.hashCode(),request.object); //si is never null,binded by ChannelConnected
			//Util.trace(this,"(%d) session:%s executing request:%s.%s (%d)",frame.invokeId,si!=null?si.getId():"-",request.object,request.method,this.protocolMgr.executor.getActiveCount());
			
			Util.trace(this,"(%d) executing request:%s.%s (%d)",frame.invokeId,request.object,request.method,this.protocolMgr.executor.getActiveCount());
			result=protocolMgr.processRequest(request,channel);
									
		}
		catch (Throwable ee)
		{
			ee.printStackTrace();
			result=ee;
		}
		
		if (!(result instanceof Future))
		{			
			ProtocolFrame response=this.protocolMgr.createResponseFrame(request,result);
			String str=null;
			if (result instanceof Throwable)
				str="fail:"+((Throwable) result).getMessage();
			else 
				str=result!=null?result.toString():"@null";
			Util.trace(this, "(%d) executed request sync: %s.%s [%s] :%s",
					frame.invokeId,request.object,request.method,Arrays.asList(request.params),str);
			if (channel.isWritable()) channel.write(response);
			
		}

		
		if (request.method.equals("close"))
		{
			Util.trace(this,"closing channel..%d",channel.hashCode());
			channel.disconnect();
		}
		
		Util.trace(this,"\\\\processed request msg...(%d) %d",frame.invokeId,this.protocolMgr.executor.getActiveCount());
		//TODO this.connector.serviceUtil.setCurResourceCount(this.connector.serviceUtil.getMaxResourceCount()-connector.session2Channel.size());
		
	}
	
	
	
}

class MessageHandler extends SimpleChannelUpstreamHandler
{
	
	ProtocolMgr protocolMgr;
	
	
	public MessageHandler(ProtocolMgr protocolMgr) throws Exception
	{
		this.protocolMgr=protocolMgr;
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Util.trace(this, "netty exception:%s",Util.getErrStack(e.getCause(),5,-1));
    }
	
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,ChannelStateEvent e) throws Exception
    {
		Util.trace(this, "channel connected:%d",ctx.getChannel().hashCode());
    }
    
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,ChannelStateEvent e) throws Exception
    {
		Channel channel=e.getChannel();	
		Util.trace(this,"channel disconnected:%d,%s",channel.hashCode(),channel);
		this.protocolMgr.closeChannel(channel);			
    }

	@Override
	public void messageReceived(ChannelHandlerContext ctx,MessageEvent e) throws Exception
    {
		MessageProcessor msg=new MessageProcessor(ctx,e,this);
		Util.trace(this,"- message received: %d %s",ctx.getChannel().hashCode(),msg);
		Object o=e.getMessage();
		if (o instanceof FlashFrame)
		{
			Channel channel=e.getChannel();
			Util.trace(this, "Process policy file request ... "+channel.getRemoteAddress().toString());
			channel.write("<cross-domain-policy> <allow-access-from domain=\"*\" to-ports=\"*\"/></cross-domain-policy>\0");
			return;
		}
		
		this.protocolMgr.executor.execute(msg);		
    }
	
	
}
