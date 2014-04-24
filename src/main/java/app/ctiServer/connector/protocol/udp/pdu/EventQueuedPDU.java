package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventQueuedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String callingDeviceID;

	private String calledDeviceID;

	private String lastRedirectDeviceID;

	private String numberQueued;

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

	public String getCallingDeviceID() {
		return callingDeviceID;
	}

	public void setCallingDeviceID(String callingDeviceID) {
		this.callingDeviceID = callingDeviceID;
	}

	public String getCalledDeviceID() {
		return calledDeviceID;
	}

	public void setCalledDeviceID(String calledDeviceID) {
		this.calledDeviceID = calledDeviceID;
	}

	public String getLastRedirectDeviceID() {
		return lastRedirectDeviceID;
	}

	public void setLastRedirectDeviceID(String lastRedirectDeviceID) {
		this.lastRedirectDeviceID = lastRedirectDeviceID;
	}

	public String getNumberQueued() {
		return numberQueued;
	}

	public void setNumberQueued(String numberQueued) {
		this.numberQueued = numberQueued;
	}

	@Override
	public EventQueuedPDU sendEventToClient(CallInfoManager callInfoManager) {

		if (callInfoManager != null) {

			String[] device = this.getEventID().split(":");
			String _deviceID = device[0];

			CallInfo callInfo = callInfoManager.getCallInfoByCallID(_deviceID,
					this.getCallID());
			if (callInfo != null) {

				this.setUcid(callInfo.getUCID());

			} else {
				this.setUcid(EXCE_UCID);
				Util.trace(this,
						"Queued event callInfo is null. " + this.getCallID());
			}

		} else {
			Util.trace(this, "Queued event callInfoManager is null. ");
		}

		Util.trace(this, "Send QueuedEvent [callID=" + this.callID + ",ucid="
				+ this.ucid + "] to client ok, sessionID" + this.getSessionID()
				+ ", deviceID=" + this.getEventID());
		return this;

	}

}
