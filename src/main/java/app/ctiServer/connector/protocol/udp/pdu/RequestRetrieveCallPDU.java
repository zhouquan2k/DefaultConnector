package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.RetrieveCallRequest;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestRetrieveCallPDU extends RequestPDU {

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

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "RetrieveCall Request ... " + this.getClientID());
		CallInfo callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.getDeviceID(), this.getUcid());
		if (callInfo != null) {
			this.setCallID(callInfo.getCallID());
			Util.trace(this,
					"RetrieveCall request convert ucid=" + this.getUcid()
							+ " to callID=" + this.getCallID());

		} else {
			Util.trace(this,
					"RetrieveCall request callInfo is null . " + this.getUcid());
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(failure, this.getClientID());
			return;
		}
		if (this.getDeviceID() != null && this.getCallID() != null) {
			Request request = new Request();
			request.invokeId = this.getClientID() + "_" + this.messageID;
			request.method = "retrieveCall";
			RetrieveCallRequest retrieveCall = new RetrieveCallRequest(
					this.deviceID, this.callID);
			retrieveCall.setSyncMode(SyncMode.Async);
			request.params = new RetrieveCallRequest[] { retrieveCall };
			request.object = "cti";

			Object result = udpHandler.processRequest(request,
					this.getClientID());
			if ((result instanceof Future)) {

				if (result instanceof Throwable) {
					udpHandler.writeRspFailed(this, (Throwable)result);
				} else {
					ResponseRetrieveCallPDU response = (ResponseRetrieveCallPDU) UDPUtil
							.loadResponsePDU(this);
					response.setDeviceID(this.deviceID);
					response.setCallID(this.callID);
					udpHandler.write(response, this.getClientID());
				}
			}
		} else {
			Util.trace(this,
					"RetrieveCall failure for input paramater is null.");
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.REQ_DEVICE_NULL);
			udpHandler.write(failure, this.getClientID());
		}

	}
}
