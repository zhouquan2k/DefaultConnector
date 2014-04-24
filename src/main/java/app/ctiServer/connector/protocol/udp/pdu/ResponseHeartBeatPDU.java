package app.ctiServer.connector.protocol.udp.pdu;

import component.util.Util;

import app.ctiServer.connector.protocol.udp.UDPHandler;

/**
 * Response for heartBeat request.
 * 
 * @author Dev.pyh
 * 
 */
public class ResponseHeartBeatPDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String versionNO;

	private String state;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getVersionNO() {
		return versionNO;
	}

	public void setVersionNO(String versionNO) {
		this.versionNO = versionNO;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public void responseToClient(UDPHandler udpHandler, String clientID) {

		Util.trace(this, "Response HeartBeat ... " + this.getMessageID());
		udpHandler.write(this, clientID);

	}

}
