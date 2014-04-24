package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseSingleStepTransferPDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String callID;

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
}
