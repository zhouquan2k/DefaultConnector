package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.util.Future;
import component.util.Util;

/**
 * MonitorDevice Request object.
 * 
 * @author Dev.pyh
 * 
 */
public class RequestMonitorDevicePDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String mode;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(
				this,
				"RequestMonitorDevice request ... deviceId="
						+ this.getDeviceID() + ",mode=" + this.getMode());
		if (this.deviceID != null && this.deviceID.length() != 0) {
			Request request = new Request();
			request.invokeId = this.getClientID() + "_" + this.messageID;
			request.method = "monitorDevice";
			request.params = new String[] { this.deviceID };
			request.object = "cti";

			Object monitor_reuslt = null;

			if ("5".equals(this.getMode())) {
				// Monitor Agent;
				Util.trace(this, "Monitor agent request agentID="
						+ this.deviceID);
				request.method = "monitorAgent";
				Util.trace(this, "MonitorAgent " + this.deviceID
						+ " request ... " + this.getClientID() + " ok.");

			} else if ("6".equals(this.getMode())) {
				// Monitor VDN
				Util.trace(this,
						"Monitor VDN request vdn=" + this.getDeviceID());
				request.method = "monitorDevice";
				Util.trace(this, "MonitorDevice " + this.deviceID
						+ " Request ... " + this.getClientID() + " ok.");

			} else {

				// Monitor Device
				Util.trace(
						this,
						"Monitor station request stationNO="
								+ this.getDeviceID());
				request.method = "monitorDevice";
			}

			monitor_reuslt = udpHandler.processRequest(request,
					this.getClientID());
			if (!(monitor_reuslt instanceof Future))
				if (monitor_reuslt instanceof Throwable) {
					udpHandler.writeRspFailed(this, (Throwable)monitor_reuslt);
					String message = ((Throwable)monitor_reuslt).getMessage();
					if (message != null && message.contains("no such object in table"))
						udpHandler.getProtocolMgr().getAllUDPContext()
								.remove(this.getClientID());
				} else {
					ResponseMonitorDevicePDU monitorDeviceRes = (ResponseMonitorDevicePDU) UDPUtil
							.loadResponsePDU(this);
					monitorDeviceRes.setDeviceID(this.getDeviceID());
					udpHandler.write(monitorDeviceRes, this.getClientID());
				}
		} else {
			Util.warn(this, "MonitorDevice deviceID is null from clientID : "
					+ this.getClientID());
			ResponseFailurePDU failureRes = UDPUtil
					.loadFailureResponsePDU(this);
			failureRes.setFailCode(UDPConstants.REQ_DEVICE_NULL);
			udpHandler.write(failureRes, this.getClientID());
		}
	}
}
