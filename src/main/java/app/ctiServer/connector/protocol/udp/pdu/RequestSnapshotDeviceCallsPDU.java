package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.cti.protocol.SnapshotDeviceRequest;
import component.util.Util;

public class RequestSnapshotDeviceCallsPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "SnapshotDeviceCall request ... deviceId="
				+ this.deviceID);
		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "snapshotDevice";
		SnapshotDeviceRequest device = new SnapshotDeviceRequest(this.deviceID);
		request.params = new SnapshotDeviceRequest[] { device };
		request.object = "cti";
		if (this.deviceID != null) {
			udpHandler.processRequest(request, this.getClientID());
		} else {
			Util.trace(this, "SnapshotDeviceCall failure for deviceID is null");
		}

	}
}
