package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseStopMonitorDevicePDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
}
