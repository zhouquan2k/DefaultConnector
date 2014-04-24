package app.ctiServer.connector.protocol.socket;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.jboss.netty.channel.Channel;


import app.ctiServer.connector.protocol.socket.ProtocolFrame;
import app.ctiServer.connector.protocol.socket.ProtocolMgr;

//import component.util.EventListener;
import component.util.RemoteEventListener;
import component.util.Util;


class Transaction
{
	ProtocolFrame request;
	ProtocolFrame response;
	Channel channel;
	Transaction(Channel channel,ProtocolFrame request)
	{
		this.channel=channel;
		this.request=request;
	}
}


//for one session
public class SocketCTIAPIHandler implements InvocationHandler //,EventCallback
{
	//ExecutorService pool=Executors.newCachedThreadPool();
	
	Channel channel;
	
	RemoteEventListener eventListener;
	//EventListener eventListener;
	SocketCTIAPIFactory factory;
		
	String sessionId;
	String object="cti";
	ProtocolMgr protocolMgr;
	
	
	//Protocol API
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		Util.trace(this,"invoking method: %s (%s)",method.getName(),args!=null?Arrays.asList(args):"");
		if (method.getName().equals("getId")) return this.sessionId;
		
		if (method.getName().equals("setEventListener"))
		{
			this.eventListener=(RemoteEventListener)args[0];
			return null;
		}
		//communication with DefaultConnector using socket
		ProtocolFrame request=this.protocolMgr.createRequestFrame(object,method.getName(),args);
		
		Transaction t=new Transaction(channel,request);
		factory.pendingRequests.put(""+request.getInvokeId(), t);
		
		//this.response=null;
		this.channel.write(request);
		
		ProtocolFrame response=null;
		
		//Sync wait for response
		synchronized (t)
		{
			if (t.response==null) t.wait(5000);
			response=t.response;
		}
		Util.check(response!=null,"wait for response timeout:"+method.getName()+" ("+request.getInvokeId()+") :"+((args!=null)?Arrays.asList(args):""));
		
		if (response.getFrameType()==ProtocolFrame.FrameType.ExceptionResponse)
		{
			throw new Exception((String)response.getResponse());
		}
		
		
		Type returnType=method.getReturnType();
		
		/*
		if (returnType.equals(Session.class))
		{
			//TODO
			return factory.getSession((String)response.getResponse()); 
		}
		else if (returnType.equals(Future.class))
		{
			returnType=method.getGenericReturnType();
		}
		*/
		
		
		Object ret=null;
		if (!returnType.equals(Void.TYPE))
		{
			ret=response.getResponse();	
		}
		if (ret instanceof Proxy)
		{
			((SocketCTIAPIHandler)(Proxy.getInvocationHandler(ret))).channel=this.channel;
		}
		
		return ret;
	}
	
	void exit()
	{
		channel.close();
		channel.getCloseFuture().awaitUninterruptibly();
	}

	/*
	public void onEvent(Event event)
	{
		try
		{
			if (event.getEventName().equals("instanceFailed"))
			{
				util.warn(this, "dependent connector failed:"+event.getProperty("instanceId")+",try to reconnect...");
				//TODO reconnect
				exit();
				init();
				this.invoke(null, util.getMethod(CTIAPI.class, "reconnectSession", null),new String[]{this.sessionId,null});				
			}
		}
		catch (Throwable e)
		{
			util.error(this, e);
		}
	}
	*/
}
