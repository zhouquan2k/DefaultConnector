package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.AlternateCallRequest;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestAlternateCallPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String activeCallID = "";

	private String activeUCID;

	private String heldCallID = "";

	private String heldUCID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getActiveCallID() {
		return activeCallID;
	}

	public void setActiveCallID(String activeCallID) {
		this.activeCallID = activeCallID;
	}

	public String getActiveUCID() {
		return activeUCID;
	}

	public void setActiveUCID(String activeUCID) {
		this.activeUCID = activeUCID;
	}

	public String getHeldCallID() {
		return heldCallID;
	}

	public void setHeldCallID(String heldCallID) {
		this.heldCallID = heldCallID;
	}

	public String getHeldUCID() {
		return heldUCID;
	}

	public void setHeldUCID(String heldUCID) {
		this.heldUCID = heldUCID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"AlternateCall request ... deviceId=" + this.getDeviceID()
						+ ",activeCallId=" + this.activeUCID + ",heldCallId="
						+ this.heldUCID);
		CallInfo active_callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.deviceID, this.getActiveUCID());
		if (active_callInfo != null) {
			this.setActiveCallID(active_callInfo.getCallID());
			Util.trace(
					this,
					"AlternateCall request convert active ucid="
							+ this.getActiveUCID() + ", to callID="
							+ this.getActiveCallID());
		} else {
			Util.warn(
					this,
					"AlternateCall request callInfo is null. "
							+ this.getActiveUCID());
			ResponseFailurePDU response = UDPUtil.loadFailureResponsePDU(this);
			response.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(response, this.getClientID());
			return;
		}

		CallInfo held_callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.deviceID, this.getHeldUCID());
		if (held_callInfo != null) {
			this.setHeldCallID(held_callInfo.getCallID());
			Util.trace(
					this,
					"AlternateCall request convert held ucid="
							+ this.getHeldUCID() + ", to callID="
							+ this.getHeldCallID());
		} else {
			Util.warn(
					this,
					"AlternateCall request callInfo is null. "
							+ this.getHeldUCID());
			ResponseFailurePDU response = UDPUtil.loadFailureResponsePDU(this);
			response.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(response, this.getClientID());
			return;
		}

		AlternateCallRequest alternateCallRequest = new AlternateCallRequest();
		alternateCallRequest.deviceId = this.deviceID;
		alternateCallRequest.activeCallId = this.activeCallID;
		alternateCallRequest.heldCallId = this.heldCallID;
		alternateCallRequest.setSyncMode(SyncMode.Sync);
		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "alternateCall";
		request.params = new AlternateCallRequest[] { alternateCallRequest };
		request.object = "cti";
		Object transferCall_result = udpHandler.processRequest(request,
				this.getClientID());
		if (!(transferCall_result instanceof Future)) {
			if (transferCall_result instanceof Throwable) {
				udpHandler.writeRspFailed(this, (Throwable)transferCall_result);
			} else {
				ResponseAlternateCallPDU response = (ResponseAlternateCallPDU) UDPUtil
						.loadResponsePDU(this);
				response.setDeviceID(this.deviceID);
				udpHandler.write(response, this.getClientID());
			}
		}
	}

}
