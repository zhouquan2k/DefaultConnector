package app.ctiServer.connector.protocol.socket;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import app.ctiServer.connector.protocol.socket.ProtocolDecoder;
import app.ctiServer.connector.protocol.socket.ProtocolEncoder;
import app.ctiServer.connector.protocol.socket.ProtocolFrame;
import app.ctiServer.connector.protocol.socket.ProtocolMgr;

import component.cti.Constants.DeviceType;
import component.cti.model.IConnection;
import component.cti.model.IDevice;
import component.cti.protocol.Session;
import component.cti.rtstat.IRealtimeStat;
import component.cti.server.data.SConnection;
import component.cti.server.data.SQueue;
import component.cti.server.data.SStation;
import component.resource.ResourceMgr;
import component.session.SessionApi;
import component.util.DefaultEvent;
import component.util.Event;
import component.util.Future;
import component.util.SyncMode;
import component.util.UserException;
import component.util.Util;
import component.util.impl.DefaultFuture;


class MyEvent extends DefaultEvent implements Event 
{
	private static final long serialVersionUID = 1L;
	
	/*
	ProtocolFrame event;
	public MyEvent(ProtocolFrame event)
	{
		super(event.getName(),null);//TODO eventSrc
		this.event=event;
		Map<String,String> props=event.getEventProperties();
		for (Map.Entry<String, String> e:props.entrySet())
		{
			String key=e.getKey();
			String value=e.getValue();
			this.setProperty(key, value);
		}
	}
	*/
	
	static Event getEvent(ProtocolFrame event) throws Throwable
	{
		return event.getEvent();
	}
}



class ClientHandler extends SimpleChannelHandler 
{
	String sessionId;
	String deviceId;
	
	SocketCTIAPIFactory mainObj;
	
	ClientHandler(SocketCTIAPIFactory obj)
	{
		this.mainObj=obj;
	}
	
	//got response packet
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
    {
    	try
    	{
    	ProtocolFrame frame = (ProtocolFrame) e.getMessage();
    	ProtocolFrame.FrameType frameType=frame.getFrameType();
    	if (frameType==ProtocolFrame.FrameType.Response||frameType==ProtocolFrame.FrameType.ExceptionResponse)
    	{
    		Transaction t=mainObj.pendingRequests.get(""+frame.getInvokeId());
    		synchronized (t)
    		{
    			t.response=frame;
    			t.notifyAll();
    		}
    	}
    	else if (frameType==ProtocolFrame.FrameType.Event)
    	{
    		//Event 
    		//final MyEvent event=new MyEvent(frame);
    		final Event event=MyEvent.getEvent(frame);
    		try
    		{
    			//sync or async
    			mainObj.eventExecutor.execute(new Runnable()
    			{
					public void run()
					{
						//find the session obj according to sessionId in event 
						//call session.eventListener.onEvent()
						mainObj.dispatchEvent(event);
					}	
    			});
    			
    		}
    		catch (Throwable ee)
    		{
    			Util.error(this, ee,"dispatch event fail");
    		}
    	}
    	else
    		Util.error(this,null,"invalid frameType:"+frameType);
    	}
    	catch (Throwable ee)
    	{
    		Util.error(this,ee,null);
    	}
       
    }
    
 
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
  	
    }
	
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}

class SessionDeserializer implements JsonDeserializer<Session>,JsonSerializer<Session>
{
	SocketCTIAPIFactory factory;
	SessionDeserializer(SocketCTIAPIFactory factory)
	{
		this.factory=factory;
	}
	@Override
	public JsonElement serialize(Session session, Type arg1,
			JsonSerializationContext context) {
		try
		{
			JsonObject o = new JsonObject();
			o.addProperty("sessionId", session.getId());
			return o;
		}
		catch (Throwable e)
		{
			Util.error(this, e, "json serialize");
		}
		return null;
	}
	@Override
	public Session deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObj = json.getAsJsonObject();
		String sessionId=jsonObj.get("sessionId").getAsString();
		return factory.getSession(sessionId);
	}
}

class FutureDeserializer implements JsonDeserializer<Future<?>>
{
	@Override
	public Future<?> deserialize(JsonElement json, Type futureType,
			JsonDeserializationContext context) throws JsonParseException {
		ParameterizedType t=(ParameterizedType)futureType;
		Type pt=t.getActualTypeArguments()[0];
		Object o=context.deserialize(json, pt);
		Future<?> f=new DefaultFuture<Object>(o);
		return f;
	}
	
}


class OtherDeserializer implements JsonDeserializer<Object>
{
	@Override
	public Object deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException 
	{
		JsonObject jsonObj = json.getAsJsonObject();
		Class<?> cls=null;
		if (type.equals(IDevice.class))
		{
			DeviceType deviceType=Enum.valueOf(DeviceType.class, jsonObj.get("deviceType").getAsString());
			
			switch (deviceType)
			{
			case Station:
				cls=SStation.class;
				break;
			case Queue:
				cls=SQueue.class;
				break;
			}
		}
		else if (type.equals(IConnection.class))
		{
			cls=SConnection.class;
		}
		return context.deserialize(json,cls);
	}
	
}


public class SocketCTIAPIFactory
{
	ChannelFactory factory;
	ExecutorService eventExecutor;
	ExecutorService pool1;
	ExecutorService pool2;
	ClientBootstrap bootstrap;
	
	ProtocolMgr protocolMgr;
	
	
	Map<String,Transaction> pendingRequests=new HashMap<String,Transaction>();
	Map<String,SocketCTIAPIHandler> allSessions=new HashMap<String,SocketCTIAPIHandler>();
	

	public void init() throws Throwable
	{	
		this.protocolMgr=Util.getBean(ProtocolMgr.class);
		eventExecutor=Executors.newSingleThreadExecutor();
		pool1=Executors.newCachedThreadPool();
		pool2=Executors.newCachedThreadPool();
		
		//init gson
		this.protocolMgr.init();
		this.protocolMgr.gsonBuilder.registerTypeAdapter(component.session.Session.class, new SessionDeserializer(this));
		this.protocolMgr.gsonBuilder.registerTypeAdapter(Future.class, new FutureDeserializer());
		
		this.protocolMgr.gsonBuilder.registerTypeAdapter(IDevice.class, new OtherDeserializer());
		this.protocolMgr.gsonBuilder.registerTypeAdapter(IConnection.class, new OtherDeserializer());
		
		this.protocolMgr.gson=this.protocolMgr.gsonBuilder.create();
		
		
		factory =
	            new NioClientSocketChannelFactory (
	                   pool1 ,pool2);
		
		bootstrap = new ClientBootstrap (factory);
		final SocketCTIAPIFactory This=this;
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
	            public ChannelPipeline getPipeline() {
	                return Channels.pipeline(
	                		new ProtocolEncoder(),
	                        new ProtocolDecoder(),
	                		new ClientHandler(This));
	                
	            }
	        });
	        
	        
		bootstrap.setOption("tcpNoDelay" , true);
		bootstrap.setOption("keepAlive", true);
		
		/*
		serviceUtil.setEventCallback(this);
		String location=serviceUtil.getServiceLocation(serviceName);
		*/
		
	}
	
	public void exit()
	{
		Util.trace(this, "SocketCTIAPIFactory exiting...");
		
		//channel.close();
		pool1.shutdownNow();
		pool2.shutdownNow();
		
		factory.releaseExternalResources();
		Util.trace(this, "SocketCTIAPIFactory exited");
	}
	
	public Object getApi(String remoteAddr,Class<?> cls,String objectName)
	{
		int pos=remoteAddr.indexOf(':');
		String addr=remoteAddr.substring(0,pos);
		int port=Integer.parseInt(remoteAddr.substring(pos+1));
		
		Util.trace(this,"connect to default-connector location:"+remoteAddr);
	        
		ChannelFuture future=bootstrap.connect (new InetSocketAddress(addr, port));
		
		Channel channel=future.getChannel();
		Util.trace(this, "connected "+remoteAddr+": "+channel);
		
		
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.object=objectName;
		handler.factory=this;
		handler.protocolMgr=this.protocolMgr;
		handler.channel=channel;
		
		Object ret=Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{cls},handler);
		return ret;
		
	}
	
	public ResourceMgr getResourceApi(String remoteAddr)
	{
		int pos=remoteAddr.indexOf(':');
		String addr=remoteAddr.substring(0,pos);
		int port=Integer.parseInt(remoteAddr.substring(pos+1));
		
		Util.trace(this,"connect to default-connector location:"+remoteAddr);
	        
		ChannelFuture future=bootstrap.connect (new InetSocketAddress(addr, port));
		
		Channel channel=future.getChannel();
		Util.trace(this, "connected "+remoteAddr+": "+channel);
		
		
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.object="res";
		handler.factory=this;
		handler.protocolMgr=this.protocolMgr;
		handler.channel=channel;
		
		ResourceMgr ret=(ResourceMgr) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{ResourceMgr.class},handler);
		return ret;
	}

	public IRealtimeStat getRTStatApi(String remoteAddr) throws UserException, RemoteException
	{
		SessionApi sessionApi=getProtocol(remoteAddr,"RTStat");
		component.session.Session session=sessionApi.initSession(null,"test", null, SyncMode.Sync);
		
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.object="RTStat";
		handler.sessionId=session.getId();
		handler.factory=this;
		handler.protocolMgr=this.protocolMgr;
		handler.channel=((SocketCTIAPIHandler)(Proxy.getInvocationHandler(sessionApi))).channel;
		
		/*
		SessionApi sessionApi=(SessionApi) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{SessionApi.class},handler);
		IRealtimeStat sessionApi.initSession(null, "test", null, SyncMode.Sync);
		*/
		this.allSessions.put(handler.sessionId, handler);
		IRealtimeStat ret=(IRealtimeStat) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{IRealtimeStat.class},handler);
		return ret;
		
	}
	
	public Session getCtiApi(String remoteAddr,String sessionDesc) throws UserException, RemoteException
	{
		SessionApi sessionApi=getProtocol(remoteAddr,"cti");
		component.session.Session session=sessionApi.initSession(null,sessionDesc, null, SyncMode.Sync);
		
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.object="cti";
		handler.sessionId=session.getId();
		handler.factory=this;
		handler.protocolMgr=this.protocolMgr;
		handler.channel=((SocketCTIAPIHandler)(Proxy.getInvocationHandler(sessionApi))).channel;
		
		this.allSessions.put(handler.sessionId, handler);
		Session ret=(Session) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{Session.class},handler);
		return ret;
	}
	//Protocol protocol;
	
	public SessionApi getProtocol(String remoteAddr,String object)
	{
		int pos=remoteAddr.indexOf(':');
		String addr=remoteAddr.substring(0,pos);
		int port=Integer.parseInt(remoteAddr.substring(pos+1));
		
		Util.trace(this,"connect to default-connector location:"+remoteAddr);
	        
		ChannelFuture future=bootstrap.connect (new InetSocketAddress(addr, port));
		
		Channel channel=future.getChannel();
		Util.trace(this, "connected "+remoteAddr+": "+channel);
		
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.channel=channel;
		handler.factory=this;
		handler.object=object;
		handler.protocolMgr=this.protocolMgr;
		
		SessionApi protocol=(SessionApi) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{SessionApi.class},handler);
		
		return protocol;
	}
	
	
	Session getSession(String sessionId)
	{
		SocketCTIAPIHandler handler=new SocketCTIAPIHandler();
		handler.factory=this;
		handler.sessionId=sessionId;
		handler.protocolMgr=this.protocolMgr;
		this.allSessions.put(sessionId, handler);
		return (Session) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{Session.class},handler);
		
	}
	
	void dispatchEvent(Event event)
	{
		String sessionId=(String)event.getProperty("_sessionId");
		SocketCTIAPIHandler session=this.allSessions.get(sessionId);
		try
		{
			if (session.eventListener!=null) session.eventListener.onEvent(event);
		}
		catch (Throwable e)
		{
			Util.error(this, e, "dispatch event fail");
		}
	}
	
}
