package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventDivertedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String callID;

	private String ucid;

	private String callingDevice;

	private String calledDevice;

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

	@Override
	public EventDivertedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "DivertedEvent : [callID=" + this.callID + ",ucid="
				+ this.ucid + ",eventID=" + this.getEventID() + "]");
		if (callInfoManager != null) {
			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {

				this.setUcid(callInfo.getUCID());
				this.setCallingDevice(callInfo.getCallingDevice());
				this.setCalledDevice(callInfo.getCalledDevice());

			} else {
				Util.trace(this, "DivertedEvent event callInfo is null. "
						+ this.getCallID());
				this.setUcid(EXCE_UCID);
				this.setCallingDevice(this.getCallingDevice());
				this.setCalledDevice(this.getCalledDevice());
			}

			callInfoManager.removeCallInfo(callInfo);

		} else {
			Util.trace(this, "DeivertedEvent callInfoMnaager is null.");
		}

		Util.trace(this, "Send DivertedEvent [callID=" + this.callID + ",ucid="
				+ this.ucid + "] to client ok,sessionID" + this.getSessionID()
				+ ", deviceID=" + this.getDeviceID());
		return this;

	}

}
