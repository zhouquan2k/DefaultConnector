package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseRetrieveCallPDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String callID;

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

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}
}
