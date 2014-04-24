package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

public class EventAgentStateChangedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String agentID;

	private String agentState;

	private String agentDevice;

	private String agentName;

	private String agentPwd;

	private String agentType;

	private String skillGroup;

	private String client_IP;

	private String client_Port;

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

	public String getAgentPwd() {
		return agentPwd;
	}

	public void setAgentPwd(String agentPwd) {
		this.agentPwd = agentPwd;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	public String getSkillGroup() {
		return skillGroup;
	}

	public void setSkillGroup(String skillGroup) {
		this.skillGroup = skillGroup;
	}

	public String getClient_IP() {
		return client_IP;
	}

	public void setClient_IP(String clientIP) {
		client_IP = clientIP;
	}

	public String getClient_Port() {
		return client_Port;
	}

	public void setClient_Port(String clientPort) {
		client_Port = clientPort;
	}

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

	@Override
	public EventAgentStateChangedPDU sendEventToClient(
			CallInfoManager callInfoManager) {

		return this;
	}

}
