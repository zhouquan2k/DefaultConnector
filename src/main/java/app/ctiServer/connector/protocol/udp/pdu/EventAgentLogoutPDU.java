package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.CallInfoManager;
import app.ctiServer.connector.protocol.udp.UDPUtil;

public class EventAgentLogoutPDU extends EventPDU {

	private static final long serialVersionUID = 1L;
		
	private String agentID;
	
	private String groupID;
	
	private String agentDevice;
	
	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getAgentDevice() {
		return agentDevice;
	}

	public void setAgentDevice(String agentDevice) {
		this.agentDevice = agentDevice;
	}

	@Override
	public EventAgentStateChangedV2PDU sendEventToClient(CallInfoManager callInfoManager) {
		
		EventAgentStateChangedV2PDU agentStateChangedEvent = new EventAgentStateChangedV2PDU();
		agentStateChangedEvent.setMessageID(UDPUtil.generateSequeueNO());
		agentStateChangedEvent.setEventName("event_AgentStateChangedV2_Logout");
		agentStateChangedEvent.setEventID(this.getEventID());
		agentStateChangedEvent.setSessionID(this.getSessionID());
		agentStateChangedEvent.setAgentID(this.getAgentID());
		agentStateChangedEvent.setAgentState("1");
		agentStateChangedEvent.setAgentDevice(this.getAgentDevice());
		agentStateChangedEvent.setAgentName("");
		agentStateChangedEvent.setPauseCode("");
		agentStateChangedEvent.setResValue1("");
		agentStateChangedEvent.setResValue2("");
		agentStateChangedEvent.setMessageTimeStamp(new Date());
		
		return agentStateChangedEvent;
	}
}
