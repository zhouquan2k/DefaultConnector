package app.ctiServer.connector.protocol.socket;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import app.ctiServer.connector.SessionInfo;
import app.ctiServer.connector.ConnectorMgr;
import app.ctiServer.connector.MethodRepository;
import app.ctiServer.connector.Protocol;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.Event;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.ExceptionResponse;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.FrameType;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.Request;
import app.ctiServer.connector.protocol.socket.ProtocolFrame.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import component.service.ServiceContext;
import component.service.ServiceInstance;
import component.session.Session;
import component.session.SessionApi;
import component.util.Helper;
import component.util.UserException;
import component.util.Util;
import component.util.WrapRuntimeException;

class ProtocolMgr implements Protocol {
	
	private int maxChannels=1000;
	private int port;
	
	public Gson gson;
	public GsonBuilder gsonBuilder=new GsonBuilder();
	
	private MethodRepository methodRepository;
	
	private int curInvokeId=0;
	//field extends from java.lang.Object which should not be serialized
	private Set<String> objectFields=new HashSet<String>();
	
	//private Map<String,SessionInfo> allSessionsByChannel=new HashMap<String,SessionInfo>();
	
	ThreadPoolExecutor executor;//thread pool used to process messages,not recv data
	
	public void setMethodRepository(MethodRepository methodRepository)
	{
		this.methodRepository=methodRepository;
	}
	
	public void setPort(int port)
	{
		this.port=port;
	}
	
	ConnectorMgr connectorMgr;
	@Override
	public void setConnectorMgr(ConnectorMgr connectorMgr)
	{
		this.connectorMgr=connectorMgr;
	}
	
	public void init()
	{
		this.executor=component.util.ThreadPoolExecutor.newThreadPoolExecutor("connector-messagehandler", 100, 200, 2000, this.maxChannels);
		
		
		for (Field f:Object.class.getDeclaredFields())
		{
			objectFields.add(f.getName());
		}
		
		gsonBuilder.setDateFormat("yyyy.MM.dd HH:mm:ss");
		gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer(this.methodRepository));
		gsonBuilder.registerTypeAdapter(Response.class, new ResponseDeserializer(this.methodRepository));
		gsonBuilder.registerTypeAdapter(Event.class, new EventDeserializer());
		// gson's serialization problem with array: using type not the object.
		gsonBuilder.registerTypeAdapter(List.class,new JsonSerializer<List<?>>(){
			@Override
			public JsonElement serialize(List<?> src, Type typeOfSrc,
					JsonSerializationContext context) {
				JsonArray a=new JsonArray();
				for (Object o:src)
				{
					a.add(context.serialize(o));
				}
				return a;				
			}
			
		});
		
		//gsonBuilder.registerTypeAdapter(Enum.class, new EnumSerializer());
		
		
		gsonBuilder.registerTypeAdapter(Session.class,new JsonSerializer<Session>(){

			@Override
			public JsonElement serialize(Session session, Type typeOfSrc,
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
			
		});
		
		gson=gsonBuilder.create();
	}
	
	@Override
	public String getLocation() {
		try
		{
			//connector:ip:port
			return  ServiceContext.getServiceContext().getLocalAddr()+":"+port;
		}
		catch (Throwable e)
		{
			throw new WrapRuntimeException(e);
		}
	}
	
	@Override
	public void start()
	{
		 // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        component.util.ThreadPoolExecutor.newThreadPoolExecutor("netty-boss", 2, 4, -1, 200),
                        component.util.ThreadPoolExecutor.newThreadPoolExecutor("netty-worker", this.maxChannels,this.maxChannels, -1, 200),
                        1000)); //max 1000 worker threads

        // Set up the pipeline factory.
        final ProtocolMgr protocolMgr=this;
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ProtocolEncoder(),
                        new ProtocolDecoder(),
                        new MessageHandler(protocolMgr));
            }
        });

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(this.port));
        
        Util.info(this, "ConnectorMgr started at port:%d", this.port);

	}
	
	/*
	SessionInfo getSessionByChannel(String channelHash,String object)
	{
		return this.allSessionsByChannel.get(channelHash);
	}
	*/
	
	/*
	Object processRequest(Request request,SessionInfo sessionInfo) throws Throwable
	{
		return connectorMgr.processRequest(request.getRequest(),sessionInfo);
	}
	*/
	Object processRequest(Request request,Channel channel) throws Throwable
	{
		return connectorMgr.processRequest(request.getRequest(),channel);
	}
	
	
	public ProtocolFrame createResponseFrame(Request request,Object returnValue)
	{
		ProtocolFrame ret=new ProtocolFrame();
		ret.version="40";
		ret.frameType=FrameType.Response;
		ret.invokeId=request.invokeId;		
		if (returnValue instanceof Throwable)
		{
			ExceptionResponse er=new ExceptionResponse();		
			ret.frameType=FrameType.ExceptionResponse;
			er.errMessage=((Throwable)returnValue).getMessage();
			if (returnValue instanceof UserException)
			{
				UserException ue=(UserException)returnValue;
				er.errCode=ue.getExceptionName();
			}
			
			ret.packet=er;
		}
		else
		{
			Response r=new Response();
			r.ret=returnValue;
			ret.packet=r;
		}
		ret.packet.invokeId=ret.invokeId;
		ret.packet.object=request.object;
		ret.packet.method=request.method;
		return ret;
	}
	
	public ProtocolFrame createEventFrame(String name,component.util.Event _event)
	{
		ProtocolFrame ret=new ProtocolFrame();
		ret.version="40";
		ret.frameType=FrameType.Event;
		ret.invokeId=0;
		Event event=new Event(_event);
		ret.packet=event;
		event.method=name;
		return ret;
	}
	

	
	//for test client
	public ProtocolFrame createRequestFrame(String object,String name,Object[] params)
	{
		ProtocolFrame ret=new ProtocolFrame();
		ret.version="40";
		ret.frameType=FrameType.Request;
		ret.invokeId=++curInvokeId;
		if (curInvokeId>=9999999) curInvokeId=0; //TODO invokeId too short
		Request req=new Request();
		ret.packet=req;
		req.invokeId=ret.invokeId;
		req.object=object;
		req.method=name;
		req.params=params;
		return ret;
	}
		
	byte[] toBytes(ProtocolFrame frame) throws UserException
	{
		try
		{
			return frame.toBytes(this.gson);
		}
		catch (Throwable e)
		{
			Util.throwUserException(e,"");
		}
		return null;
	}
	
	ProtocolFrame fromBytes(byte[] _header,byte[] _body) throws UserException
	{
		ProtocolFrame ret=null;
		try
		{
		ret=new ProtocolFrame();
		String header=new String(_header);
		
		int pos=ProtocolFrame.FrameMark.length();
		ret.version=header.substring(pos,pos+2);
		ret.frameType=FrameType.parse(header.substring(pos+2,pos+4));
		ret.length=_body.length;
		ret.invokeId=Integer.parseInt(header.substring(pos+12,pos+20));
	
		//name & params
		String content=new String(_body);
		Util.trace(ret, "deserialize json:(%d)  %s %d",ret.invokeId,content,ret.length);
		/*
		String[] params=content.split("\r\n");
		ret.name=params[0];
		ret.params=new String[params.length-1];
		for (int i=1;i<params.length;i++) ret.params[i-1]=params[i];
		*/
		if (ret.frameType==FrameType.Request)
		{
			ret.packet=gson.fromJson(content, Request.class);			
		}
		else if (ret.frameType==FrameType.Event)
		{
			ret.packet=gson.fromJson(content, Event.class);
		}
		else if (ret.frameType==FrameType.ExceptionResponse)
		{
			ret.packet=gson.fromJson(content, ExceptionResponse.class);
		}
		else 
		{
			ret.packet=gson.fromJson(content, Response.class);
		}
		ret.packet.invokeId=ret.invokeId;
		}
		catch (Throwable e)
		{
			Util.error("app.cti.connector.ProtocolFrame", e, "fromBytes fail");
			Util.throwUserException(e, "fromBytes fail");
		}
		return ret;
	}

	@Override
	public void sendEvent(SessionInfo _session,component.util.Event e) {
		
		ProtocolFrame event=this.createEventFrame(e.getEventName(),e);
		
		SessionInfo session=(SessionInfo)_session;
		if (session==null) //session already closed
			Util.error(this,null,"invalid session:%s when dispatching event","null");
		else
		{
			Channel channel=(Channel)session.getProtocolContext();
			//dispatch to clients accoring to
			if (session.isValid()&&channel!=null&& channel.isWritable())
			{
				Util.trace(this, "send event to %s :%s",session.getId(), e);
				channel.write(event);
			}
			else Util.trace(this,"event not sended for channel inactive");
		}
		
	}

	@Override
	public void sendConfirmation(SessionInfo _session,app.ctiServer.connector.Request req,Object ret) {
		ProtocolFrame response=this.createResponseFrame((Request)req.tag,ret);
		SessionInfo session=(SessionInfo)_session;
		Channel channel=(Channel)session.getProtocolContext();
		if (session.isValid()&&channel.isWritable())
			channel.write(response);
		
	}

	@Override
	public void snapshot() {
		// TODO Auto-generated method stub
		
	}

	void closeChannel(Channel channel)
	{
		connectorMgr.onProtocolContextDestroy(channel);
	}

	@Override
	public void onSessionClosed(SessionInfo session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dependentServiceAvailable(String dependencyId,
			ServiceInstance instance) {
		// TODO Auto-generated method stub
		
	}
}




/*
class EnumSerializer implements JsonSerializer<Enum>
{
	@Override
	public JsonElement serialize(Enum src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}
	
}
*/

class RequestDeserializer implements JsonDeserializer<ProtocolFrame.Packet>
{
	//cache
	Map<String,List<Type>> typeCache=new HashMap<String,List<Type>>();
	MethodRepository methodRepository;
	
	RequestDeserializer(MethodRepository methodRepository)
	{
		this.methodRepository=methodRepository;
	}
	@Override
	public ProtocolFrame.Packet deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		try
		{
			final JsonObject jsonObj = json.getAsJsonObject();
			
			ProtocolFrame.Request req=new ProtocolFrame.Request();
			if (jsonObj.has("method"))
				req.method= jsonObj.get("method").getAsString();
			else 
				req.method=jsonObj.get("name").getAsString();
			if (jsonObj.has("object"))
				req.object=jsonObj.get("object").getAsString();		
			else 
				req.object="cti";//TODO
			final JsonElement data = jsonObj.get("params");
			final JsonArray dataArray = (data==null||data.isJsonNull()) ? new JsonArray() : data.getAsJsonArray();
			
			List<Type> types=this.typeCache.get(req.object+"."+req.method);
			if (types==null)
			{
				//TODO get method from connector
				//Method method=Helper.getMethod(Session.class, req.method, null);
				//if (method==null) method=Helper.getMethod(SessionApi.class, req.method, null);
				Method method=this.methodRepository.getMethod(req.object, req.method);
								
				Util.check(method!=null,"invalid method name:%s",req.method);
				types=new Vector<Type>();
				Class<?>[] paramTypes=method.getParameterTypes();
				for (Class<?> c:paramTypes)
					types.add(c);
				this.typeCache.put(req.object+"."+req.method, types); 	
			}
	        
			
			List<Object> a=new Vector<Object>();
			Util.check(dataArray.size()==types.size(),"param count mismatch:%s, %d<>%d",req.method,types.size(),dataArray.size());
			for (int i = 0; i < dataArray.size(); i++) {
		        //Type t=gParamTypes[i];
		        a.add(context.deserialize(dataArray.get(i), types.get(i)));
			}
			req.params=a.toArray(new Object[0]);
			
			return req;
		}
		catch (Throwable e)
		{
			Util.error(this,e,"request json parse fail:%s",json.toString());
			throw new JsonParseException(e);
		}
	}
}

class ResponseDeserializer implements JsonDeserializer<ProtocolFrame.Packet>,JsonSerializer<ProtocolFrame.Packet>
{
	//cache
	Map<String,Type> typeCache=new HashMap<String,Type>();
	MethodRepository methodRepository;
	
	ResponseDeserializer(MethodRepository methodRepository)
	{
		this.methodRepository=methodRepository;
	}
	
	@Override
	public ProtocolFrame.Packet deserialize(JsonElement json, Type _type,
			JsonDeserializationContext context) throws JsonParseException {
		try
		{
			final JsonObject jsonObj = json.getAsJsonObject();
			ProtocolFrame.Response r=new ProtocolFrame.Response();			
			r.method= jsonObj.get("method").getAsString();
			r.object= jsonObj.get("object").getAsString();
			
			Type type=this.typeCache.get(r.object+"."+r.method);
			if (type==null)
			{
				Method method=null;
				if (this.methodRepository!=null) method=this.methodRepository.getMethod(r.object, r.method);
				if (method==null) method=Helper.getMethod(SessionApi.class,r.method,null);
				Util.check(method!=null,"invalid method %s.%s when deserialize Response",r.object,r.method);
				type=method.getGenericReturnType();
				this.typeCache.put(r.object+"."+r.method, type);
			}
			JsonElement ret = jsonObj.get("ret");
			r.ret=context.deserialize(ret, type);
			return r;
		}
		catch (Throwable e)
		{
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(ProtocolFrame.Packet packet, Type typeOfSrc,
			JsonSerializationContext context) {
		try
		{
			ProtocolFrame.Response r=(ProtocolFrame.Response)packet;
			JsonObject o = new JsonObject();
			o.addProperty("method",r.method);
			o.addProperty("invokeId",r.invokeId);
			o.addProperty("object",r.object);
			JsonElement e=null;
			if (r.ret instanceof Session) e=context.serialize(r.ret,Session.class);			
			else if (r.ret instanceof Exception) e=new JsonPrimitive(r.ret.toString());
			else e=context.serialize(r.ret);
			o.add("ret", e);
			return o;
		}
		catch (Throwable e)
		{
			Util.error(this, e, "json serialize");
		}
		return null;
	}
}

class EventDeserializer implements JsonDeserializer<ProtocolFrame.Packet>,JsonSerializer<ProtocolFrame.Packet>
{	
	@Override
	public ProtocolFrame.Packet deserialize(JsonElement json, Type _type,
			JsonDeserializationContext context) throws JsonParseException {
		try
		{
			final JsonObject jsonObj = json.getAsJsonObject();
			
			String eventName=jsonObj.get("name").getAsString();
			
			JsonObject props=jsonObj.getAsJsonObject("properties");
			//Map<String,String> properties=context.deserialize( jsonObj.get("properties"), new TypeToken<Map<String, String>>(){}.getType());
			String eventClassName=(eventName.indexOf('.')>=0)?eventName:"component.cti.event."+eventName.substring(0,1).toUpperCase()+eventName.substring(1)+"Event";			
			@SuppressWarnings("unchecked")
			Class<component.util.DefaultEvent> cls=(Class<component.util.DefaultEvent>)Class.forName(eventClassName);
			Constructor<?> c=cls.getDeclaredConstructor();
			c.setAccessible(true);
			component.util.DefaultEvent e=(component.util.DefaultEvent)c.newInstance();
			Map<String,Field> fieldMap=new HashMap<String,Field>();
			for (Field f:cls.getFields()) 
				fieldMap.put(f.getName(), f);
			
			e.setEventName(eventName);
			for (Entry<String,JsonElement> et:props.entrySet())
			{
				
				if (fieldMap.containsKey(et.getKey()))
				{
					Field f=fieldMap.get(et.getKey());
					f.setAccessible(true);
					Type t=f.getType();
					if (t.equals(List.class))
                	{
						 t= (ParameterizedType)(f.getGenericType());	                		
                	}
					Object value=context.deserialize(props.get(f.getName()),t);
					f.set(e,value);					
				}
				else
				{
					e.setProperty(et.getKey(),context.deserialize(et.getValue(),String.class));
				}
						
			}
			return new ProtocolFrame.Event(e);
		}
		catch (Throwable e)
		{
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(ProtocolFrame.Packet src, Type typeOfSrc,
			JsonSerializationContext context) {
		try
		{
			component.util.Event e=((ProtocolFrame.Event)src).event;
			JsonObject o = new JsonObject();			
			JsonObject props = new JsonObject();		
			o.addProperty("name",e.getEventName());
			if (e.getEventSrc()!=null) o.addProperty("source",e.getEventSrc().toString());
			if (e.getEventTime()!=null) o.addProperty("timestamp",Util.longTimeFormat.format(e.getEventTime()));
			o.add("properties", props);
			Field[] fs=e.getClass().getFields();
			//fields
			for (Field f:fs)
			{
				try
				{
					Object v=f.get(e);
					if (v!=null)
					{			
						props.add(f.getName(),context.serialize(v));
					}
				}
				catch (Throwable ee)
				{
					Util.error(this,ee, "get field value fail:%s", f.getName());
				}
			}
			//dynamic properties
			for (String key:e.getPropertyNames())
			{
				Object value=e.getProperty(key);
				if (value!=null) props.addProperty(key,value.toString());
				
			}
			//props.addProperty("_sessionId",sessionId);
			
								
			return o;
		}
		catch (Throwable e)
		{
			Util.error(this, e, "json serialize");
		}
		return null;
	}
	
	
}
