package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.Constants.AgentMode;
import component.cti.protocol.SetAgentStateRequest;
import component.cti.protocol.SetAgentStateRequest.AgentFunc;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestSetAgentStatePDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	static final String PENDING = "p";

	private String deviceID;

	private String agentID;

	private String agentName;

	private String agentPwd;

	private String skillGroup;

	private String agentMode;

	private String agentType;

	private String agentACDType ;

	private String agentWorkMode;

	private String client_IP;

	private String client_Port;

	private String pauseCode;

	private String pending;

	public String getAgentACDType() {
		return agentACDType;
	}

	public void setAgentACDType(String agentACDType) {
		this.agentACDType = agentACDType;
	}

	public String getAgentWorkMode() {
		return agentWorkMode;
	}

	public void setAgentWorkMode(String agentWorkMode) {
		this.agentWorkMode = agentWorkMode;
	}

	public String getPauseCode() {
		return pauseCode;
	}

	public void setPauseCode(String pauseCode) {
		this.pauseCode = pauseCode;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
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

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
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

	public String getAgentMode() {
		return agentMode;
	}

	public void setAgentMode(String agentMode) {
		this.agentMode = agentMode;
	}

	public String getPending() {
		return pending;
	}

	public void setPending(String pending) {
		this.pending = pending;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(
				this,
				"SetAgentState Request ... agentId=" + this.getAgentID()
						+ ",agentDevice=" + this.getDeviceID() + ", agentMode="
						+ this.getAgentMode() + ",agentACDType="
						+ this.getAgentACDType() + ",pausecode="
						+ this.pauseCode);

		Request request = new Request();
		request.object = "cti";
		SetAgentStateRequest agentState = new SetAgentStateRequest();
		agentState.deviceId = this.deviceID;
		agentState.agentId = this.agentID;
		agentState.group = this.skillGroup;

		agentState.password = this.agentPwd;

		if ("0".equals(this.getAgentMode())) {
			// login
			agentState.agentMode = AgentMode.NotReady;
			agentState.func = AgentFunc.Login;
		} else if ("1".equals(this.getAgentMode())) {
			// logout
			agentState.agentMode = AgentMode.Logout;
			agentState.func = AgentFunc.Logout;
		} else if ("2".equals(this.getAgentMode())) {
			// NotReady
			agentState.agentMode = AgentMode.NotReady;
			agentState.reason = this.getPauseCode();
			agentState.func = AgentFunc.SetState;
		} else if ("3".equals(this.getAgentMode())) {
			// Ready
			agentState.agentMode = AgentMode.Ready;
			agentState.func = AgentFunc.SetState;
		} else if ("4".equals(this.getAgentMode())) {
			// ACW
			agentState.agentMode = AgentMode.WorkNotReady;
			agentState.func = AgentFunc.SetState;
		} else {
			Util.trace(this, "Agent " + this.getAgentID()
					+ " Can't verify state ... " + this.agentMode);
		}
		agentState.setSyncMode(SyncMode.Async);
		request.method = "setAgentState";
		request.params = new SetAgentStateRequest[] { agentState };
		request.invokeId = this.getClientID() + "_" + this.getMessageID();
		Object result = udpHandler.processRequest(request, this.getClientID());
		if (!(result instanceof Future)) {
			if (result instanceof Throwable) {
				udpHandler.writeRspFailed(this, (Throwable)result);
			} else {
				ResponseSetAgentStatePDU response = (ResponseSetAgentStatePDU) UDPUtil
						.loadResponsePDU(this);
				udpHandler.write(response, this.getClientID());
			}

		}

	}
}
