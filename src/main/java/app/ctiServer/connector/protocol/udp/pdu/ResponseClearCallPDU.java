package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;

public class ResponseClearCallPDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String callID;

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

}
