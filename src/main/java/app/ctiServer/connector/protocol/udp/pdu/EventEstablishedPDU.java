package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventEstablishedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String deviceID;

	private String callingDevice;

	private String calledDevice;

	private String TrunkGroup;

	private String TrunkMem;

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

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	@Override
	public EventEstablishedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "EstablishedEvent : [callID=" + this.callID
				+ ",eventID=" + this.getEventID() + ",answerDevice="
				+ this.deviceID + "]");
		if (callInfoManager != null) {
			CallInfo callInfo = callInfoManager.getCallInfoByCallID(
					this.getDeviceID(), this.getCallID());
			if (callInfo != null) {
				callInfo.setCallingDevice(this.getCallingDevice());
				callInfo.setCalledDevice(this.getCalledDevice());
				callInfo.setUCID(this.ucid);
			} else {
				callInfo = new CallInfo(this.getCallID(), this.getDeviceID());
				callInfo.setCallingDevice(this.getCallingDevice());
				callInfo.setCalledDevice(this.getCalledDevice());
				callInfo.setUCID(this.getUcid());
			}
			callInfoManager.addCallInfo(callInfo);
			Util.trace(this, "Send EstablishedEvent [callID=" + this.callID
					+ ",ucid=" + this.ucid + "] to client ok, sessionID="
					+ this.getSessionID() + ", deviceID=" + this.getEventID());
			return this;

		} else {
			Util.warn(this,
					"Send Established event to client failure, clientSession is null.");
		}

		return null;
	}

}
