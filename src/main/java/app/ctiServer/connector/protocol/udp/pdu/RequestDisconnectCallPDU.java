package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Iterator;
import java.util.Set;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.ClearConnectionRequest;
import component.util.Future;
import component.util.Util;

public class RequestDisconnectCallPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String destDeviceID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getDestDeviceID() {
		return destDeviceID;
	}

	public void setDestDeviceID(String destDeviceID) {
		this.destDeviceID = destDeviceID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"DisconnectCall Request ... deviceId=" + this.getDeviceID()
						+ ", destDeviceId=" + this.getDestDeviceID());

		Set<CallInfo> callInfoSet = udpHandler.getCallInfoManager()
				.getCallInfoByDevice(this.getDestDeviceID());
		String dis_callID = "";
		if (callInfoSet != null && callInfoSet.size() > 0) {
			if (callInfoSet.size() == 1) {
				Iterator<CallInfo> item = callInfoSet.iterator();
				CallInfo callInfo = item.next();
				dis_callID = callInfo.getCallID();
			} else {
				for (CallInfo activeCallInfo : callInfoSet) {
					if (activeCallInfo != null
							&& activeCallInfo.getLiveFlag() == 0) {
						dis_callID = activeCallInfo.getCallID();
						break;
					}
				}

			}

			Util.trace(this, "DisconnectCall callID=" + dis_callID
					+ ", deviceID=" + this.getDestDeviceID());
			if (dis_callID.length() > 0) {
				Request request = new Request();
				request.invokeId = this.getClientID() + "_" + this.messageID;
				request.method = "clearConnection";
				ClearConnectionRequest clearCall = new ClearConnectionRequest(
						this.deviceID, dis_callID);
				request.params = new ClearConnectionRequest[] { clearCall };
				request.object = "cti";
				if (dis_callID != null) {
					Object result = udpHandler.processRequest(request,
							this.getClientID());
					if (!(result instanceof Future)) {
						if (result instanceof Throwable) {
							udpHandler.writeRspFailed(this, (Throwable)result);
						} else {
							ResponseDisconnectCallPDU response = (ResponseDisconnectCallPDU) UDPUtil
									.loadResponsePDU(this);
							response.setDeviceID(this.deviceID);
							udpHandler.write(response, this.getClientID());
						}

					}
				}
			} else {

				Util.warn(this,
						"DisconnCall failure, more than one call on device ["
								+ this.getDestDeviceID() + "] callSize : "
								+ callInfoSet.size());
				udpHandler.getCallInfoManager().removeDeviceID(destDeviceID);
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
				udpHandler.write(failure, this.getClientID());

			}
		} else {

			Util.warn(this,
					"DisconnCall failure, no call on this device deviceID="
							+ this.getDestDeviceID());
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(failure, this.getClientID());

		}

	}
}
