package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.CallInfoManager;
import app.ctiServer.connector.protocol.udp.UDPUtil;

public class EventAgentNotReadyPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String agentID;

	private String agentDevice;

	private String pauseCode;

	public String getPauseCode() {
		return pauseCode;
	}

	public void setPauseCode(String pauseCode) {
		this.pauseCode = pauseCode;
	}

	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getAgentDevice() {
		return agentDevice;
	}

	public void setAgentDevice(String agentDevice) {
		this.agentDevice = agentDevice;
	}

	@Override
	public EventAgentStateChangedV2PDU sendEventToClient(
			CallInfoManager callInfoManager) {

		EventAgentStateChangedV2PDU agentStateChangedEvent = new EventAgentStateChangedV2PDU();
		agentStateChangedEvent.setMessageID(UDPUtil.generateSequeueNO());
		agentStateChangedEvent.setEventName("event_AgentStateChangedV2_AUX");
		agentStateChangedEvent.setEventID(this.getEventID());
		agentStateChangedEvent.setSessionID(this.getSessionID());
		agentStateChangedEvent.setAgentID(this.getAgentID());
		agentStateChangedEvent.setAgentState("2");
		agentStateChangedEvent.setAgentDevice(this.agentDevice);
		agentStateChangedEvent.setAgentName("");
		agentStateChangedEvent.setPauseCode(this.pauseCode);
		agentStateChangedEvent.setResValue1("");
		agentStateChangedEvent.setResValue2("");
		agentStateChangedEvent.setMessageTimeStamp(new Date());

		return agentStateChangedEvent;
	}
}
