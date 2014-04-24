package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.util.Future;
import component.util.Util;

/**
 * Stop monitor device request.
 * 
 * @author Dev.pyh
 * 
 */
public class RequestStopMonitorDevicePDU extends RequestPDU {

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

		Util.trace(this, "StopMonitor request ... deviceId=" + this.deviceID);
		udpHandler.getCallInfoManager().removeDeviceID(this.deviceID);
		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "stopMonitorDevice";
		request.params = new String[] { this.deviceID };
		request.object = "cti";
		Object result = udpHandler.processRequest(request, this.getClientID());
		if (!(result instanceof Future)) {
			if (result instanceof Throwable) {
				udpHandler.writeRspFailed(this, (Throwable)result);
			} else {

				ResponseStopMonitorDevicePDU monitorDeviceRes = (ResponseStopMonitorDevicePDU) UDPUtil
						.loadResponsePDU(this);
				monitorDeviceRes.setDeviceID(this.getDeviceID());
				udpHandler.write(monitorDeviceRes, this.getClientID());
			}
		}
	}
}
