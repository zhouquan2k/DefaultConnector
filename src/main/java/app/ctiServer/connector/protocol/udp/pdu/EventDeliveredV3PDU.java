package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

@Deprecated
public class EventDeliveredV3PDU extends EventPDU {

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

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public EventDeliveredV3PDU(EventDeliveredV2PDU deliveredV2PDU) {
		this.setANI(deliveredV2PDU.getANI());
		this.setCallID(deliveredV2PDU.getCallID());
		this.setCallType(deliveredV2PDU.getCallType());
		this.setDeviceID(deliveredV2PDU.getDeviceID());
		this.setDNIS(deliveredV2PDU.getDNIS());
		this.setEventID(deliveredV2PDU.getEventID());
		this.setEventName(deliveredV2PDU.getEventName());
		this.setMessageID(deliveredV2PDU.getMessageID());
		this.setMessageTimeStamp(deliveredV2PDU.getMessageTimeStamp());
		this.setSessionID(deliveredV2PDU.getSessionID());
		this.setSplit(deliveredV2PDU.getSplit());
		this.setTrunkGroup(deliveredV2PDU.getTrunkGroup());
		this.setTrunkMember(deliveredV2PDU.getTrunkMember());
		this.setUui(deliveredV2PDU.getUui());
		this.setVdn(deliveredV2PDU.getVdn());
		this.setUcid(this.getUcid());
	}

	public EventDeliveredV3PDU() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public EventDeliveredV3PDU sendEventToClient(CallInfoManager callInfoManager) {

		return this;

	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
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

}
