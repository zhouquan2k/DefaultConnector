package app.ctiServer.connector.protocol.socket;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;


import component.util.Util;

class ProtocolEncoder extends SimpleChannelHandler
{
	private ProtocolMgr protocolMgr;
	 public void writeRequested(ChannelHandlerContext ctx, MessageEvent  e) 
	 {
		 try
	     {
		 //Util.trace(this,"1"+e.hashCode());
		 if (e.getMessage() instanceof ProtocolFrame)
		 {
	        ProtocolFrame frame = (ProtocolFrame) e.getMessage();
	        	    
	        
	        	if (this.protocolMgr==null) this.protocolMgr=Util.getBean("protocol",ProtocolMgr.class);
	        	byte[] bytes=this.protocolMgr.toBytes(frame);
	        	
	        	//ProtocolDecoder.traceBytes(bytes, "send buffer");
	        	ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);
	        	
	        	//Util.trace(this,"2"+e.hashCode());
		        Channels.write(ctx, e.getFuture(), buf);
	        
	        //Util.trace(this,"3"+e.hashCode());
		 }
		 else 
		 {
			 Util.check(e.getMessage() instanceof String,"invalid encode message:"+e.getMessage());
			 String content=(String)e.getMessage();
			 Channels.write(ctx, e.getFuture(),ChannelBuffers.wrappedBuffer(content.getBytes()));		 
	     }
	     }
	     catch (Throwable ee)
	     {
	    	 Util.error(this,ee,"channel write");
	     }
	 }
}
