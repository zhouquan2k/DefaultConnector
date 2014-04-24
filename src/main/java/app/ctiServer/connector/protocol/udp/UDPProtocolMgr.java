package app.ctiServer.connector.protocol.udp;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import component.cti.Constants.AgentAutoWorkMode;
import component.cti.Constants.DeviceType;
import component.cti.event.DeliveredEvent;
//import component.cti.event.EstablishedEvent;
import component.cti.event.SnapshotEvent;
import component.cti.event.SystemEvent;
import component.cti.event.SystemEvent.SystemEventType;
import component.cti.protocol.Session;
import component.cti.protocol.SetAgentAutoWorkModeRequest;
import component.service.RunMode;
import component.service.ServiceContext;
import component.service.ServiceInstance;
import component.util.Event;
import component.util.SyncMode;
import component.util.Util;
import component.util.WrapRuntimeException;
import app.ctiServer.connector.SessionInfo;
import app.ctiServer.connector.ConnectorMgr;
import app.ctiServer.connector.MethodRepository;
import app.ctiServer.connector.Protocol;
import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentStateChangedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAlertedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAlertedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredV2PDU;
//import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredV3PDU;
//import app.ctiServer.connector.protocol.udp.pdu.EventEstablishedPDU;
//import app.ctiServer.connector.protocol.udp.pdu.EventEstablishedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryGroupInfoV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseSetAgentStatePDU;

class UDPContext {
	DatagramChannel channel;

	String sessionID;

	InetSocketAddress remotAddress;

	int aliveValue = 20;

     UDPContext() {
	}
	
	UDPContext(DatagramChannel channel) {
		this.channel = channel;
	}
}

public class UDPProtocolMgr implements Protocol {

	private int maxChannels = 1000;

	private int port;

	private String agentWorkMode;

	private Map<String, UDPContext> allUDPContexts = new HashMap<String, UDPContext>();

	private Map<String, String> sessionID_ClientID = new HashMap<String, String>();

	private Map<String, RequestPDU> requestMaps = new HashMap<String, RequestPDU>();

	ThreadPoolExecutor executor;
   
	public static final String DEFAULT_CONTEXT = "_defaultSession";
	 
	private ConnectorMgr connectorMgr;

	private MethodRepository methodRepository;

	private CTIEventCallBack proc;

	public CallInfoManager callInfoManager = null;

	private Map<String, ResponseQueryGroupInfoV2PDU> groupInfoV2PDUs = new HashMap<String, ResponseQueryGroupInfoV2PDU>();

	public boolean cti_available = true;
	
	public void init() {
		this.executor = component.util.ThreadPoolExecutor
				.newThreadPoolExecutor("connector-UDPRequesthandler", 100, 200,
						2000, this.maxChannels);
		callInfoManager = CallInfoManager.getInstance();
	}

	@Override
	public String getLocation() {
		try {
			// connector:ip:port
			return ServiceContext.getServiceContext().getLocalAddr() + ":"
					+ port;
		} catch (Throwable e) {
			throw new WrapRuntimeException(e);
		}
	}

	public void setMethodRepository(MethodRepository methodRepository) {
		this.methodRepository = methodRepository;
	}

	@Override
	public void setConnectorMgr(ConnectorMgr connectorMgr) {
		this.connectorMgr = connectorMgr;

	}

	public MethodRepository getMethodRepository() {
		return methodRepository;
	}

	public String getAgentWorkMode() {
		return agentWorkMode;
	}

	public void setAgentWorkMode(String agentWorkMode) {
		this.agentWorkMode = agentWorkMode;
	}

	@Override
	public void start() {
		DatagramChannelFactory udpChannelFactory = new NioDatagramChannelFactory(
				component.util.ThreadPoolExecutor.newThreadPoolExecutor(
						"udp-worker", 2, 4, -1, 200), 100);
		final UDPProtocolMgr protocolMgr = this;
		ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(
				udpChannelFactory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {

				return Channels.pipeline(new UDPDecoder(), new UDPEncoder(),
						new UDPHandler(protocolMgr));
			}
		});
		bootstrap.bind(new InetSocketAddress(port));

		proc = new CTIEventCallBack(this);
	}

	@Override
	public void sendEvent(SessionInfo session, Event event) {
       if(event instanceof SystemEvent){
    	   SystemEvent systemEvent = (SystemEvent)event;
    	   if(systemEvent.type == SystemEventType.SystemError)
    	      cti_available = false;
    	   else if(systemEvent.type == SystemEventType.SystemOk)
    		  cti_available = true;
    	   return;
       }
		if ("snapshot".equalsIgnoreCase(event.getEventName())) {
			SnapshotEvent snapshotEvent = (SnapshotEvent) event;
			ResponseQueryGroupInfoV2PDU queryGroupInfoV2PDU = new ResponseQueryGroupInfoV2PDU(
					snapshotEvent);
			groupInfoV2PDUs.put(queryGroupInfoV2PDU.getGroupID(),
					queryGroupInfoV2PDU);
			return;
		}
		try {
			if (session == null) {// session already closed{
				Util.error(this, null,
						"invalid session:%s when dispatching event %s", "null",
						event.getEventName());
			} else {
				UDPContext context = (UDPContext) session.getProtocolContext();
				EventPDU eventPDU = proc.packagEventPDU(event);

				eventPDU = eventPDU.sendEventToClient(callInfoManager);
				DatagramChannel channel = context.channel;
				// dispatch to clients accoring to
				if (session.isValid() && channel != null
						&& channel.isWritable()) {
					Util.trace(this, "send event to %s :%s", session.getId(),
							event);
					this.sendEventPDU(channel, eventPDU, event, session);

				} else
					Util.trace(this, "event not sended for channel inactive");
			}

		} catch (Throwable e) {
		}
	}

	@Override
	public void sendConfirmation(SessionInfo sesion, Request req, Object ret) {
		try {
			ResponsePDU responsePDU = null;	
			if (req != null) {
				if(ret instanceof Throwable){
					Throwable e = (Throwable)ret;
					if(e.getMessage().contains("Connection refused: connect")){
						cti_available = false;
						return;
					}
			        if(e.getMessage().contains("invalid session") || e.getMessage().contains("session invalid")){
			        	String clientID = sessionID_ClientID.get(sesion.getId());
			        	allUDPContexts.remove(clientID);
			        	sessionID_ClientID.remove(sesion.getId());
			        	return;
			        }
				}
				responsePDU = proc.packagResponsePDU(ret, req);
				UDPContext context = (UDPContext) sesion.getProtocolContext();
				if (responsePDU != null && context != null && sesion.isValid()
						&& context.channel != null
						&& context.channel.isWritable())
					context.channel.write(responsePDU, context.remotAddress);
				if (responsePDU instanceof ResponseSetAgentStatePDU) {
					ResponseSetAgentStatePDU responseSetAgentState = (ResponseSetAgentStatePDU) responsePDU;
					EventPDU agent_eventPDU = proc
							.convertResponse2Event(responseSetAgentState);
					agent_eventPDU = agent_eventPDU
							.sendEventToClient(callInfoManager);
					context.channel.write(agent_eventPDU, null);
				}
			}
		} catch (Throwable e) {
			Util.error(this,e.getCause(), e.getMessage(), "");
		}
	}

	@Override
	public void snapshot() {
		// TODO Auto-generated method stub

	}

	public Map<String, UDPContext> getAllUDPContext() {
		return allUDPContexts;
	}

	public InetSocketAddress getSocketAddressBySessionID(String sessionID) {
		String clientID = sessionID_ClientID.get(sessionID);
		if (clientID != null) {
			UDPContext context = allUDPContexts.get(clientID);
			if (context != null)
				return context.remotAddress;
		}
		return null;
	}

	public UDPContext getUDPConextByClientID(String clientID) {
		return this.allUDPContexts.get(clientID);
	}

	UDPContext addSession(DatagramChannel channel, String clientID,
			InetSocketAddress remotAddress) throws Throwable {
		UDPContext context = new UDPContext(channel);
		Session session = sessionCreated(context, clientID);
		if(session != null){
		context.sessionID = session.getId();
		context.remotAddress = remotAddress;
		synchronized (this.allUDPContexts) {
			sessionID_ClientID.put(session.getId(), clientID);
			this.allUDPContexts.put(clientID, context);
		}
		return context;
		}else {
			return null;
		}
	}

	public Session sessionCreated(UDPContext context, String clientID)
			throws Throwable {
		if (context != null) {
			Request request = new Request();
			request.params = new String[] { null, new Date().toString(),
					clientID, "Async" };
			request.object = "cti";
			request.method = "initSession";
			Object result = this.processRequest(request, context);
			if (result instanceof Session) {
				Util.trace(this, "Session created successfully.", "");
				return (Session) result;
			} else if (result instanceof Throwable) {
				Util.trace(this, "Session created failed.exception exists", "");
				Throwable e = (Throwable) result;
				Util.error(this, e, "");
				ResponseFailurePDU failure = new ResponseFailurePDU();
				failure.setFailCode(UDPConstants.REQ_CTI_FAILURE);
				failure.setFailDesc(e.getMessage());
				failure.setClientID(clientID);
				if (context.channel != null && context.channel.isWritable())
					context.channel.write(failure, context.remotAddress);
				return null;
			} else {
				Util.trace(this, "Session created failed.", "");
				return null;
			}
		} else {
			Util.trace(this, "Session created failed.context is null", "");
			return null;
		}
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ConnectorMgr getConnectorMgr() {
		return connectorMgr;
	}

	public Map<String, RequestPDU> getRequestMaps() {
		return requestMaps;
	}

	public void setRequestMaps(Map<String, RequestPDU> requestMaps) {
		this.requestMaps = requestMaps;
	}

	Object processRequest(Request request, Object protocolContext)
			throws Throwable {
		return connectorMgr.processRequest(request, protocolContext);
	}

	private void sendEventPDU(DatagramChannel channel, EventPDU eventPDU,
			Event e, SessionInfo sessionInfo) throws Throwable {
		channel.write(eventPDU, null);
		if (eventPDU instanceof EventAlertedPDU) {
			DeliveredEvent event = (DeliveredEvent) e;
			EventAlertedV2PDU eV2pdu = new EventAlertedV2PDU(
					(EventAlertedPDU) eventPDU);
			eV2pdu.setTrunkGroup(event.trunkGroup != null ? event.trunkGroup
					: "");
			eV2pdu.setTrunkMem(event.trunkMember != null ? event.trunkMember
					: "");
			eV2pdu.setUcid(event.callId);
			eV2pdu = eV2pdu.sendEventToClient(callInfoManager);
			channel.write(eV2pdu, null);
//		} else if (eventPDU instanceof EventEstablishedPDU) {
//			EstablishedEvent event = (EstablishedEvent) e;
//			EventEstablishedV2PDU establishedV2PDU = new EventEstablishedV2PDU(
//					(EventEstablishedPDU) eventPDU);
//			establishedV2PDU.setDeviceID(event.srcDeviceId);
//			establishedV2PDU.setUcid(event.contactId);
//			establishedV2PDU = establishedV2PDU
//					.sendEventToClient(callInfoManager);
//			channel.write(establishedV2PDU, null);
		} else if (eventPDU instanceof EventDeliveredV2PDU) {
		//	DeliveredEvent event = (DeliveredEvent) e;
			EventDeliveredV2PDU deliveredv2Event = (EventDeliveredV2PDU) eventPDU;
//			EventDeliveredV3PDU deliveredV3PDU = new EventDeliveredV3PDU(
//					deliveredv2Event);
//			deliveredV3PDU.setUcid(event.contactId);
//			deliveredV3PDU = deliveredV3PDU.sendEventToClient(callInfoManager);
//			channel.write(deliveredV3PDU, null);

			EventDeliveredPDU deliveredPDU = new EventDeliveredPDU();
			deliveredPDU.setMessageID(deliveredPDU.getMessageID());
			deliveredPDU.setEventName("event_Delivered");
			deliveredPDU.setMessageID(deliveredv2Event.getMessageID());
			deliveredPDU.setSessionID(deliveredv2Event.getSessionID());
			deliveredPDU.setCallID(deliveredv2Event.getCallID());
			deliveredPDU.setDeviceID(deliveredv2Event.getDeviceID());
			deliveredPDU.setEventID(deliveredv2Event.getEventID());
			deliveredPDU.setANI(deliveredv2Event.getANI());
			deliveredPDU.setDNIS(deliveredv2Event.getDNIS());
			deliveredPDU.setCallType(deliveredv2Event.getCallType());
			deliveredPDU.setUui(deliveredv2Event.getUui());
			deliveredPDU.setUcid(deliveredv2Event.getUcid());
			deliveredPDU.setMessageTimeStamp(deliveredv2Event
					.getMessageTimeStamp());
			deliveredPDU = deliveredPDU.sendEventToClient(callInfoManager);
			channel.write(deliveredPDU, null);
		}
		if (e.getEventName().equals("agentLoggedOn")) {
			Request request = new Request();
			EventAgentStateChangedV2PDU loginEvent = (EventAgentStateChangedV2PDU) eventPDU;
			SetAgentAutoWorkModeRequest agentAutoWork = new SetAgentAutoWorkModeRequest();
			agentAutoWork.agentId = loginEvent.getAgentID();
			if ("Auto_In".equalsIgnoreCase(this.getAgentWorkMode())) {
				agentAutoWork.autoWorkMode = AgentAutoWorkMode.AutoIn;
			} else {
				agentAutoWork.autoWorkMode = AgentAutoWorkMode.ManualIn;
			}
			agentAutoWork.setSyncMode(SyncMode.Sync);
			request.params = new SetAgentAutoWorkModeRequest[] { agentAutoWork };
			request.invokeId = sessionInfo.getId() + "_"
					+ "setAgentAutoWorkMode";
			request.method = "setAgentAutoWorkMode";
			request.object = "cti";
			this.processRequest(request, sessionInfo.getProtocolContext());
		}
	}

	public Map<String, ResponseQueryGroupInfoV2PDU> getGroupInfoV2PDUs() {
		return groupInfoV2PDUs;
	}

	@Override
	public void onSessionClosed(SessionInfo session) {

		String clientID = sessionID_ClientID.get(session.getId());
		if(clientID != null){
		Set<String> sqNums = requestMaps.keySet();
		synchronized (allUDPContexts) {
			sessionID_ClientID.remove(session.getId());
			allUDPContexts.remove(clientID);
			RequestPDU pdu = null;
			for (String sqNo : sqNums) {
				pdu = requestMaps.get(sqNo);
				if (pdu != null && clientID.equals(pdu.getClientID()))
					requestMaps.remove(sqNo);
			}
		}
		sqNums.clear();
		Util.trace(this, "UDPContext removed", "");
		}

	}

	@Override
	public void dependentServiceAvailable(String dependencyId,
			ServiceInstance instance) {
		cti_available = true;
		if ("cti".equalsIgnoreCase(dependencyId)) {
			try {
				if (RunMode.WarmStandby != instance.runMode) {
					allUDPContexts.clear();
					requestMaps.clear();
					sessionID_ClientID.clear();
				}
				this.queryGroupInfos();
				Util.trace(this, "query groupInfos", "");
			} catch (Throwable e) {
				Util.error(this, e.getCause(), "can't query groupInfos", "");
			}
		}

	}

	@SuppressWarnings("unchecked")
	public void queryGroupInfos() throws Throwable {
		Request request = new Request();
		request.method = "queryDeviceIdsByType";
		request.object = "cti";
		request.params = new DeviceType[] { DeviceType.Queue };
		
		Object result = this.processRequest(request, DEFAULT_CONTEXT);
		if (result instanceof List) {
			List<String> deviceIDs = (List<String>) result;
			for (int i = 0; i < deviceIDs.size(); i++) {
				request.method = "monitorDevice";
				request.params = new String[] { deviceIDs.get(i) };
				request.object = "cti";
				this.processRequest(request, DEFAULT_CONTEXT);
			}
		}
	}

	public Map<String, String> getSessionID_ClientID() {
		return sessionID_ClientID;
	}
	
	

}
