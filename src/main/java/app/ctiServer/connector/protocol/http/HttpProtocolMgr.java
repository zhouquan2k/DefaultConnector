package app.ctiServer.connector.protocol.http;


import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import component.service.ServiceContext;
import component.service.ServiceInstance;
import component.util.Event;
import component.util.EventListener;
import component.util.Util;
import component.util.WrapRuntimeException;

import app.ctiServer.connector.SessionInfo;
import app.ctiServer.connector.ConnectorMgr;
import app.ctiServer.connector.MethodRepository;
import app.ctiServer.connector.Protocol;
import app.ctiServer.connector.Request;

public class HttpProtocolMgr implements Protocol {
	
	private MethodRepository methodRepository;
	private int port;
	public Gson gson;
	
	public static final String DEFAULT_CONTEXT = "_defaultSession";

	public void setMethodRepository(MethodRepository methodRepository) {
		this.methodRepository = methodRepository;
	}

	public MethodRepository getMethodRepository() {
		return methodRepository;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void init(){
		gson = new Gson();
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
	public void sendConfirmation(SessionInfo sesion, Request req,
			Object ret) {
		
	}

	@Override
	public void sendEvent(SessionInfo sessionInfo, Event event) {
		HttpSession session=(HttpSession)sessionInfo.getProtocolContext();		
		EventListener el=(EventListener)session.getAttribute("_client");
		if (el!=null) 
			el.onEvent(event);
	}

	private ConnectorMgr connectorMgr;
	@Override
	public void setConnectorMgr(ConnectorMgr connectorMgr) {
		this.connectorMgr = connectorMgr;
	}

	@Override
	public void start() {
	Util.getBeanFactory().getBean("HttpConnectorAction");
	   Util.getBeanFactory().getBean("component.web.WebServer");
		
	}
	
	public Request fromJson(Request req,Method method){
		Class<?>[] cls = method.getParameterTypes();
		for (int i = 0; i < cls.length; i++) {
			req.params[i] = gson.fromJson(req.params[i].toString(), cls[i]);
		}
		return req;
	}
	
	public String processRequest(Request request){
		String res = null;
		try {
			request = this.fromJson(request, connectorMgr.getMethod(request.object, request.method));
			Object result = connectorMgr.processRequest(request, DEFAULT_CONTEXT);
			if(result instanceof Throwable) res = ((Throwable)result).getMessage();
			else res = gson.toJson(result);
		} catch (Throwable e) {
			res = e.getMessage();
		}
		return res;
	}

	@Override
	public void snapshot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionClosed(SessionInfo session) {
		// TODO Auto-generated method stub
		
	}
	
	
	private ConnectorActionScaner actionScaner;
	public void setConnectorActionScaner(ConnectorActionScaner actionScaner)
	{
		this.actionScaner=actionScaner;
	}

	@Override
	public void dependentServiceAvailable(String dependencyId,
			ServiceInstance instance) {
		this.actionScaner.dependentServiceAvailable(dependencyId,instance);
		
	}
}
