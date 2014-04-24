package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventOriginatedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String calledNO;

	private String callingNO;

	private String ucid;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getCallingNO() {
		return callingNO;
	}

	public void setCallingNO(String callingNO) {
		this.callingNO = callingNO;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getCalledNO() {
		return calledNO;
	}

	public void setCalledNO(String calledNO) {
		this.calledNO = calledNO;
	}

	@Override
	public EventOriginatedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(
				this,
				"OriginatedEvent : [callID=" + this.callID + ",eventID="
						+ this.getEventID() + ", calledDevice="
						+ this.getCalledNO() + "]");
		if (callInfoManager != null) {

			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {
				this.setUcid(callInfo.getUCID());
				callInfo.setCallingDevice(this.getCallingNO());
				callInfo.setCalledDevice(this.getCalledNO());

				callInfoManager.addCallInfo(callInfo);

			} else {
				this.setUcid(EXCE_UCID);
				Util.trace(
						this,
						"Orginated event callInfo not exist for callId : "
								+ this.getCallID() + " on device ["
								+ this.getDeviceID() + "]");
			}
		} else {
			Util.trace(this, "Orginated event callInfoMnager object is null. ");
		}

		Util.trace(
				this,
				"Send OrginatedEvent [callID=" + this.callID + ",ucid="
						+ this.ucid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		return this;
	}

}
