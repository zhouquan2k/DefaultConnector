package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

/**
 * ServiceInitiated event object.
 * 
 * @author Dev.wtx
 * 
 */
public class EventServiceInitiatedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ucid;

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

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	@Override
	public EventServiceInitiatedPDU sendEventToClient(
			CallInfoManager callInfoManager) {
		Util.trace(this, "ServiceInitiatedEvent : [callID=" + this.getCallID()
				+ ",ucid=" + this.getUcid() + ",eventID=" + this.getEventID()
				+ "]");

		if (callInfoManager != null) {
			CallInfo new_callInfo = new CallInfo(this.getCallID(),
					this.getDeviceID());
			new_callInfo.setUCID(this.getUcid());
			new_callInfo.setCallingDevice("");
			new_callInfo.setCalledDevice("");

			callInfoManager.addCallInfo(new_callInfo);

		} else {
			Util.trace(this,
					"ServiceInitiated evnet callInfoMnager object is null. ");
		}

		Util.trace(
				this,
				"Send ServiceInitiatedEvent [callID=" + this.callID + ",ucid="
						+ this.ucid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		return this;

	}

}
