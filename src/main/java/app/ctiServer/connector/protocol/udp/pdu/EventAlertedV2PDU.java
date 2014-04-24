package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

public class EventAlertedV2PDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ANI;

	private String DNIS;

	public EventAlertedV2PDU() {
		// TODO Auto-generated constructor stub
	}

	public EventAlertedV2PDU(EventAlertedPDU eventPDU) {
		this.setANI(eventPDU.getANI());
		this.setCallID(eventPDU.getCallID());
		this.setDeviceID(eventPDU.getDeviceID());
		this.setDNIS(eventPDU.getDNIS());
		this.setEventID(eventPDU.getEventID());
		this.setEventName("event_AlertedV2");
		this.setMessageID(eventPDU.getMessageID());
		this.setMessageTimeStamp(eventPDU.getMessageTimeStamp());
		this.setSessionID(eventPDU.getSessionID());
		this.setUcid(eventPDU.getUcid());
	}

	private String trunkGroup;

	private String TrunkMem;

	private String ucid;

	public String getTrunkGroup() {
		return trunkGroup;
	}

	public void setTrunkGroup(String trunkGroup) {
		this.trunkGroup = trunkGroup;
	}

	public String getTrunkMem() {
		return TrunkMem;
	}

	public void setTrunkMem(String trunkMem) {
		TrunkMem = trunkMem;
	}

	@Override
	public EventAlertedV2PDU sendEventToClient(CallInfoManager callInfoManager) {

		return this;

	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
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

}
