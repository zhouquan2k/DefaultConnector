package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.util.Util;

public class ResponseSetAgentStatePDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String agentID;

	private String agentName;

	private String agentPwd;

	private String skillGroup;

	private String agentState;

	private String agentType;

	private String client_IP;

	private String client_Port;

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

	public String getSkillGroup() {
		return skillGroup;
	}

	public void setSkillGroup(String skillGroup) {
		this.skillGroup = skillGroup;
	}

	public String getAgentState() {
		return agentState;
	}

	public void setAgentState(String agentState) {
		this.agentState = agentState;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
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

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	@Override
	public void responseToClient(UDPHandler udpHandler, String clientID) {

		Util.trace(this, "Response setAgentState ... " + this.messageID);

		if (udpHandler != null) {

			udpHandler.write(this, clientID);
			Util.trace(this,
					"SetAgentState response ok . " + this.getClientID());
		} else {
			Util.warn(this, "SetAgentState clientSession is null. ");
		}
	}

}
