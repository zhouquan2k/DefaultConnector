package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

public class EventAgentStateChangedV2PDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String agentID;

	private String agentState;

	private String agentDevice;

	private String agentName;

	private String pauseCode;

	private String resValue1;

	private String resValue2;

	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getAgentState() {
		return agentState;
	}

	public void setAgentState(String agentState) {
		this.agentState = agentState;
	}

	public String getAgentDevice() {
		return agentDevice;
	}

	public void setAgentDevice(String agentDevice) {
		this.agentDevice = agentDevice;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getPauseCode() {
		return pauseCode;
	}

	public void setPauseCode(String pauseCode) {
		this.pauseCode = pauseCode;
	}

	public String getResValue1() {
		return resValue1;
	}

	public void setResValue1(String resValue1) {
		this.resValue1 = resValue1;
	}

	public String getResValue2() {
		return resValue2;
	}

	public void setResValue2(String resValue2) {
		this.resValue2 = resValue2;
	}

	@Override
	public EventAgentStateChangedV2PDU sendEventToClient(
			CallInfoManager callInfoManager) {

		// do nothing.
		return null;

	}
}
