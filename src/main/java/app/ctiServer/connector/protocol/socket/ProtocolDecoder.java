package app.ctiServer.connector.protocol.socket;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;


import component.util.Util;


class ProtocolDecoder extends FrameDecoder {
	static final Charset charset=Charset.forName("US-ASCII");
	
	static void traceBytes(byte[] bytes,String name)
	{
		System.out.println("buffer "+name+":");
		for (int i=0;i<bytes.length;i++)
			System.out.print(""+(char)(bytes[i]));
		 System.out.println("\r\n");
	}
	
	private ProtocolMgr protocolMgr;
	
	private FlashFrame flashPacketDecode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buf)
	{
		FlashFrame ret=null;
		if (buf.readableBytes()>=23)
		{
			buf.markReaderIndex();
			byte[] content = new byte[23];
			buf.readBytes(content);			
			String policyFileRequest = new String(content);
			//Util.trace(this, "policyFile : "+policyFileRequest);
			if(policyFileRequest.indexOf("<policy-file-request/>") != -1){
				ret=new FlashFrame(policyFileRequest);
			}			
			else
				buf.resetReaderIndex();
		}
		return ret;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buf) throws Exception
	{
		//special process for flash
		FlashFrame ff=flashPacketDecode(ctx,channel,buf);
		if (ff!=null) return ff;
		
		
		// Make sure if the length field was received.
	     if (buf.readableBytes() < ProtocolFrame.HeaderSize) {
	        // The length field was not received yet - return null.
	        // This method will be invoked again when more packets are
	        // received and appended to the buffer.
	        return null;
	     }

	     // The length field is in the buffer.

	     // Mark the current buffer position before reading the length field
	     // because the whole frame might not be in the buffer yet.
	     // We will reset the buffer position to the marked position if
	     // there's not enough bytes in the buffer.
	     buf.markReaderIndex();

	     // Read the length field
	 
	     //UtilStatic.instance.trace(this, "processing header...");
		 byte[] header=new byte[ProtocolFrame.HeaderSize];
		 buf.readBytes(header);
			
	     int bodySize=ProtocolFrame.getBodyLength(header);
	     
	     //System.out.println("getting frame size:"+bodySize);
	     
	     // Make sure if there's enough bytes in the buffer.
	     if (buf.readableBytes() < bodySize) {
	        // The whole bytes were not received yet - return null.
	        // This method will be invoked again when more packets are
	        // received and appended to the buffer.

	        // Reset to the marked position to read the length field again
	        // next time.
	        buf.resetReaderIndex();

	        return null;
	     }

	     // There's enough bytes in the buffer. Read it.
	     byte[] content=new byte[bodySize];
	     buf.readBytes(content);
	      
		 //traceBytes(header,"recv header");
		 //traceBytes(content,"recv body");
	     if (this.protocolMgr==null) this.protocolMgr=Util.getBean("protocol",ProtocolMgr.class);
	     return this.protocolMgr.fromBytes(header,content);

	}

}
