package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

public class EventDeliveredV2PDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ANI;

	private String DNIS;

	private String callType;

	private String uui;

	private String trunkGroup;

	private String trunkMember;

	private String split;

	private String vdn;

	private String ucid;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getANI() {
		return ANI;
	}

	public void setANI(String aNI) {
		ANI = aNI;
	}

	public String getDNIS() {
		return DNIS;
	}

	public void setDNIS(String dNIS) {
		DNIS = dNIS;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getUui() {
		return uui;
	}

	public void setUui(String uui) {
		this.uui = uui;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getVdn() {
		return vdn;
	}

	public void setVdn(String vdn) {
		this.vdn = vdn;
	}

	public String getTrunkGroup() {
		return trunkGroup;
	}

	public void setTrunkGroup(String trunkGroup) {
		this.trunkGroup = trunkGroup;
	}

	public String getTrunkMember() {
		return trunkMember;
	}

	public void setTrunkMember(String trunkMember) {
		this.trunkMember = trunkMember;
	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	@Override
	public EventDeliveredV2PDU sendEventToClient(CallInfoManager callInfoManager) {

		return this;

	}

}
