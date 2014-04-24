package app.ctiServer.connector.protocol.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ctiServer.connector.Connector;
import app.ctiServer.connector.ConnectorMgr;

import ch.swissdotnet.extdirect4j.AbstractAction;
import component.service.ServiceInstance;
import component.util.UserException;
import component.web.ExtActionScaner;

class ConnectorActionScaner implements ExtActionScaner
{
	private ConnectorMgr connectorMgr;
	private Map<String,AbstractAction> actions=new HashMap<String,AbstractAction>();
	
	public void setConnectorMgr(ConnectorMgr connectorMgr)
	{
		this.connectorMgr=connectorMgr;
	}
	
	public void init() throws UserException
	{
		for (Connector connector:connectorMgr.getConnectors())
		{
			List<String> objects=connector.getSupportedObjects();
			for (String object:objects)
			{
				actions.put(object,new SessionAction(object,connector.getRemoteInterface(object),this.connectorMgr));
			}
		}
	}
	
	@Override
	public Collection<AbstractAction> getActions() throws UserException 
	{					
		return actions.values();
	}
	
	//TODO
	public void dependentServiceAvailable(String dependencyId,ServiceInstance instance) 
	{
		/*
		Connector connector=connectorMgr.getConnector(dependencyId);
		Object object=connector.getRemoteObject(dependencyId);
		AbstractAction action=this.actions.get(dependencyId);
		action.setInstance(object);
		*/
	}
}
