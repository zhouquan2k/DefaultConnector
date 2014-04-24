package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventFailurePDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String failCode;

	private String ucid;

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

	public String getFailCode() {
		return failCode;
	}

	public void setFailCode(String failCode) {
		this.failCode = failCode;
	}

	@Override
	public EventFailurePDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "FailureEvent : [callID=" + this.callID + ",eventID="
				+ this.getEventID() + "]");
		if (callInfoManager != null) {

			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {

				this.setUcid(callInfo.getUCID());
			} else {
				this.setUcid(EXCE_UCID);
			}

			Util.trace(this, "Send EventFailureEvent [callID=" + this.callID
					+ ",ucid=" + this.ucid + "] to client ok, sessionID="
					+ this.getSessionID() + ", deviceID=" + this.getEventID());
			return this;
		} else {
			Util.trace(this, "EventFailurePDU event callInfoManager is null. ");
		}
		return null;
	}

}
