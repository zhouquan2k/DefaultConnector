package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

@Deprecated
public class EventEstablishedV2PDU extends EventPDU {

	private static final long serialVersionUID = 1L;
	private String callID;

	private String deviceID;

	private String callingDevice;

	private String calledDevice;

	private String TrunkGroup;

	private String TrunkMem;

	private String ucid;

	public EventEstablishedV2PDU() {
		// TODO Auto-generated constructor stub
	}

	public EventEstablishedV2PDU(EventEstablishedPDU establishedPDU) {
		this.setCallID(establishedPDU.getCallID());
		this.setCalledDevice(establishedPDU.getCalledDevice());
		this.setCallingDevice(establishedPDU.getCallingDevice());
		this.setDeviceID(establishedPDU.getDeviceID());
		this.setEventID(establishedPDU.getEventID());
		this.setEventName("event_establishedV2");
		this.setMessageID(establishedPDU.getMessageID());
		this.setMessageTimeStamp(establishedPDU.getMessageTimeStamp());
		this.setSessionID(establishedPDU.getSessionID());
		this.setTrunkGroup(establishedPDU.getTrunkGroup());
		this.setTrunkMem(establishedPDU.getTrunkMem());
		this.setUcid(this.getUcid());
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

	public String getCallingDevice() {
		return callingDevice;
	}

	public void setCallingDevice(String callingDevice) {
		this.callingDevice = callingDevice;
	}

	public String getCalledDevice() {
		return calledDevice;
	}

	public void setCalledDevice(String calledDevice) {
		this.calledDevice = calledDevice;
	}

	public String getTrunkGroup() {
		return TrunkGroup;
	}

	public void setTrunkGroup(String trunkGroup) {
		TrunkGroup = trunkGroup;
	}

	public String getTrunkMem() {
		return TrunkMem;
	}

	public void setTrunkMem(String trunkMem) {
		TrunkMem = trunkMem;
	}

	@Override
	public EventEstablishedV2PDU sendEventToClient(
			CallInfoManager callInfoManager) {

		return this;
	}

}
