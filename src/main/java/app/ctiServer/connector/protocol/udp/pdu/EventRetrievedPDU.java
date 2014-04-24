package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventRetrievedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ucid;

	private String callingDevice;

	private String calledDevice;

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

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
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

	@Override
	public EventRetrievedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "RetrievedEvent : [callID=" + this.callID
				+ ",eventID=" + this.getEventID() + "]");
		if (callInfoManager != null) {
			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {

				this.setUcid(callInfo.getUCID());
				this.setCallingDevice(callInfo.getCallingDevice());
				this.setCalledDevice(callInfo.getCalledDevice());

			} else {
				this.setUcid(EXCE_UCID);
				this.setCallingDevice("");
				this.setCalledDevice("");
			}

		} else {
			Util.trace(this, "Retrieved event callInfoManager is null. ");
		}

		Util.trace(
				this,
				"Send RetrievedEvent [callID=" + this.callID + ",ucid="
						+ this.ucid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		return this;

	}

}
