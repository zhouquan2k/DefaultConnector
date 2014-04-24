package app.ctiServer.connector.protocol.http;

import org.aopalliance.intercept.MethodInvocation;

import app.ctiServer.connector.Connector;
import app.ctiServer.connector.ConnectorMgr;
import app.ctiServer.connector.Request;
import app.ctiServer.connector.SessionInfo;
import component.util.SyncMode;
import component.util.UserException;

import ch.swissdotnet.extdirect4j.InterfaceAction;
import ch.swissdotnet.extdirect4j.WebContext;

class SessionAction extends InterfaceAction 
{
	//Map<String,Object> allInstances=new HashMap<String,Object>();
	static final String SessionActionInstance="_SessionActionInstance";
	
	private ConnectorMgr connectorMgr;
	private String objectName;
	public SessionAction(String name,final Class<?> clazz,ConnectorMgr connectorMgr) throws UserException
	{
		super(name,clazz,null);
		this.objectName=name;
		this.connectorMgr=connectorMgr;
	}	
	
	@Override
	public Object beforeInvoke(WebContext ctx) throws UserException
	{
		Object instance=ctx.request.getSession().getAttribute(SessionActionInstance+"_"+this.objectName);
		if (instance==null)
		{
			//create new object
			Connector connector=this.connectorMgr.getConnector(this.objectName);
			SessionInfo sessionInfo=new SessionInfo(SyncMode.Sync);
			connector.initSession(sessionInfo, ctx.request.getSession());
			if (sessionInfo.isValid()) 
				instance=sessionInfo.getSession();
			else //no session needed
				instance=connector.getRemoteObject(this.objectName);
			ctx.request.getSession().setAttribute(SessionActionInstance+"_"+this.objectName,instance);
		}
		return instance;
	}
	
	@Override 
	public Object afterInvoke(MethodInvocation invoke,Object ret)
	{
		Connector connector=this.connectorMgr.getConnector(this.objectName);
		Request req=new Request();
		req.method=invoke.getMethod().getName();
		req.object=this.objectName;
		
		return connector.processResult(req,ret);
	}
}
