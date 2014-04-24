package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventAlertedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String ANI;

	private String DNIS;

	private String ucid;

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

	@Override
	public EventAlertedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "AlertedEvent : [callID=" + this.callID + ",ucid="
				+ this.ucid + ",eventID=" + this.getEventID() + "]");

		if (callInfoManager != null) {

			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {

				callInfo.setCallingDevice(this.getANI());
				callInfo.setCalledDevice(this.getDNIS());
				callInfo.setUCID(this.ucid);

			} else {

				callInfo = new CallInfo(this.getCallID(), this.getDeviceID());
				callInfo.setCallingDevice(this.getANI());
				callInfo.setCalledDevice(this.getDNIS());
				callInfo.setUCID(this.getUcid());

			}

			callInfoManager.addCallInfo(callInfo);

		} else {
			Util.trace(this, "Alerted evnet callInfoMnager object is null. ");
		}
		return this;

	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

}
