package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseQueryPauseCodePDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String pauseCode;

	private String resValue;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getPauseCode() {
		return pauseCode;
	}

	public void setPauseCode(String pauseCode) {
		this.pauseCode = pauseCode;
	}

	public String getResValue() {
		return resValue;
	}

	public void setResValue(String resValue) {
		this.resValue = resValue;
	}
}
