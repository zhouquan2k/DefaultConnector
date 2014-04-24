package app.ctiServer.connector.protocol.socket;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import component.util.Util;


class ProtocolFrame
{
	
	public enum FrameType {
		Request("01"),Response("10"),ExceptionResponse("11"),Event("99");
		
		private String value;  
	    FrameType(String value)
		{
			this.value=value;
		}
		public String getValue()
		{
			return this.value;
		}
		
		static Map<String,FrameType> map;
		public static FrameType parse(String value)
		{
			if (map==null) 
			{
				map=new HashMap<String,FrameType>(); 
				for (FrameType v:FrameType.values())
					map.put(v.value, v);
			}
			return map.get(value);
		}
	};

	static final String FrameMark="CT";
	//static final int MaxFrameSize=1024;
	static final int HeaderSize=64;//redundancy
	static DecimalFormat lengthFormat=new DecimalFormat("00000000");
	static DecimalFormat invokeIdFormat=new DecimalFormat("00000000");
	
	//header 
	String version;
	FrameType frameType;
	int length;
	int invokeId;
	
	//content
	Packet packet;
	
	public FrameType getFrameType()
	{
		return frameType;
	}
	
	public String getName()
	{
		return packet.method;
	}
	
	public Object[] getParams()
	{
		return ((Request)packet).params;
	}
	
	public int getInvokeId()
	{
		return invokeId;
	}
	
	
	static int  getBodyLength(byte[] _header) throws Exception
	{
		String header=new String(_header);
		Util.check(header.startsWith(ProtocolFrame.FrameMark),"invalid frame marker");
		int bodySize=Integer.parseInt(header.substring(ProtocolFrame.FrameMark.length()+4,ProtocolFrame.FrameMark.length()+12));
		return bodySize;
	}
	
	byte[] toBytes(Gson gson) throws Throwable
	{
		/*
		String content=this.name+"\r\n";
		if (this.params!=null)
		for (int i=0;i<this.params.length;i++)
			content+=this.params[i]+"\r\n";
		byte[] c=content.getBytes();
		
		*/
		String content=gson.toJson(packet);
		Util.trace(this, "serialize json:%s", content);
		
		byte[] c=content.getBytes("utf-8");
		this.length=c.length;
		
		int size=HeaderSize+this.length;
		byte[] ret=new byte[size];
		
		System.arraycopy(c, 0, ret, HeaderSize, this.length);		
		String str=FrameMark+this.version+this.frameType.getValue()+lengthFormat.format(this.length)+invokeIdFormat.format(this.invokeId)+"\r\n";
		byte[] header=str.getBytes("utf-8");
		
		assert header.length<=HeaderSize;
		
		System.arraycopy(header, 0, ret, 0, header.length);
		
		ret[HeaderSize-2]=13;
		ret[HeaderSize-1]=10;		
		
		return ret;
	}
	
	public Object getResponse()
	{
		assert this.frameType==FrameType.Response||this.frameType==FrameType.ExceptionResponse;
		return (this.frameType==FrameType.Response)?
			((Response)this.packet).ret : ((ExceptionResponse)this.packet).errMessage;
	}
	
	/*
	public Map<String,String> getEventProperties()
	{
		return ((Event)this.packet).properties;
	}
	*/
	
	public component.util.Event getEvent()
	{
		assert this.frameType==FrameType.Event;
		return ((Event)this.packet).event;
	}

	
	public static class Packet
	{
		int invokeId;
		String object;
		String method;
	}
	public static class Request extends Packet  
	{
		Object[] params;
		
		app.ctiServer.connector.Request getRequest()
		{
			app.ctiServer.connector.Request r=new app.ctiServer.connector.Request();
			r.object=this.object;
			r.method=this.method;
			r.params=this.params;
			r.invokeId=""+this.invokeId;
			r.tag=this;
			return r;
		}
	}

	public static class Response extends Packet
	{
		Object ret;
	}

	public static class ExceptionResponse extends Packet
	{
		public String errCode;
		public String errMessage;
	}

	public static class Event extends Packet
	{
		//Map<String,String> properties;
		component.util.Event event;
		Event(component.util.Event event)
		{
			this.event=event;
		}
	}

}
