package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventDeliveredPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ANI;

	private String DNIS;

	private String callType;

	private String uui;

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

	@Override
	public EventDeliveredPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(
				this,
				"Send DeliveredEvent [callID=" + this.callID + ",ucid="
						+ this.ucid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		if (callInfoManager != null) {

			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {
				Util.trace(this, "Warnning : CallInfo " + callInfo.toString()
						+ " already exist @ deviceId=" + this.getDeviceID()
						+ " for DeliveredEvent by " + this.getEventID());
			} else {

				callInfo = new CallInfo(this.getCallID(), this.getDeviceID());
				callInfo.setUCID(this.getUcid());
				callInfo.setCallingDevice(this.getANI());
				callInfo.setCalledDevice(this.getDNIS());
				callInfoManager.addCallInfo(callInfo);
			}

		} else {
			Util.trace(this, "Delivered evnet callInfoMnager object is null. ");
		}

		Util.trace(
				this,
				"Send DeliveredEvent [callID=" + this.callID + ",ucid="
						+ this.ucid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		return this;

	}
}
