package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseQueryAgentStateV2PDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String agentID;

	private String agentName;

	private String groupID;

	private String agentState;

	private String agentType;

	private String client_IP;

	private String client_Port;

	private String login_Device;

	private String pauseCode;

	private String deviceState;

	private String res1;

	private String res2;

	public String getPauseCode() {
		return pauseCode;
	}

	public void setPauseCode(String pauseCode) {
		this.pauseCode = pauseCode;
	}

	public String getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(String deviceState) {
		this.deviceState = deviceState;
	}

	public String getRes1() {
		return res1;
	}

	public void setRes1(String res1) {
		this.res1 = res1;
	}

	public String getRes2() {
		return res2;
	}

	public void setRes2(String res2) {
		this.res2 = res2;
	}

	public String getLogin_Device() {
		return login_Device;
	}

	public void setLogin_Device(String loginDevice) {
		login_Device = loginDevice;
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

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
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
}
