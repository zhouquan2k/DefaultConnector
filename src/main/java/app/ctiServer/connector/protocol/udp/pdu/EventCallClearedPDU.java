package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventCallClearedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;
		
	private String callID;
	
	private String deviceID;
	
	private String callingDevice;
	
	private String calledDevice;
	
	private String releaseDevice;
	
	private String ucid;

	@Override
	public EventPDU sendEventToClient(CallInfoManager callInfoManager) {
		
		Util.trace(this, "EventCallClearedPDU : [eventID="+this.getEventID()+",callID="+this.getCallID()+",releaseDevice="+this.getReleaseDevice()+"]");
		
		if(callInfoManager != null){
	
			CallInfo callInfo = callInfoManager.getCallInfoByCallID(this.getDeviceID(), this.callID);
			if(callInfo != null){
				
				Util.trace(this, "EventCallClearedPDU remove callInfo callID="+callInfo.getCallID()+", deviceID="+this.getDeviceID());
				callInfoManager.removeCallInfo(callInfo);
						
				this.setCalledDevice(callInfo.getCalledDevice());
				this.setCallingDevice(callInfo.getCallingDevice());
				this.setUcid(callInfo.getUCID());
						
			}else{
				Util.trace(this,"EventCallClearedPDU event callInfo is null for callID="+this.getCallID()+" on releaseDevice="+this.releaseDevice);
				// this.setCalledDevice();
				// this.setCallingDevice("");
				this.setUcid(EXCE_UCID);
			}
			Util.trace(this,"Send EventCallClearedPDU [callID="+this.callID+",ucid="+this.ucid+"] to client ok,sessionID="+this.getSessionID()+", deviceID="+deviceID);
			return this;
		}else{
			Util.trace(this,"EventCallClearedPDU event callInfoManager is null. ");
		}
		
	return null;		
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

	public String getReleaseDevice() {
		return releaseDevice;
	}

	public void setReleaseDevice(String releaseDevice) {
		this.releaseDevice = releaseDevice;
	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}
	
}
