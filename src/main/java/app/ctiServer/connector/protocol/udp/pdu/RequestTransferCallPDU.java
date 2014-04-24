package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.TransferCallRequest;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestTransferCallPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	private String activeCallID;

	private String activeUCID;

	private String heldCallID;

	private String heldUCID;

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

		Util.trace(this, "TransferCall request ... deviceId=" + this.deviceID
				+ ",activeCallId=" + this.activeUCID + ",heldCallId="
				+ this.heldUCID);
		CallInfo active_callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.getDeviceID(),
						this.getActiveUCID());
		if (active_callInfo != null) {
			this.setActiveCallID(active_callInfo.getCallID());
		} else {
			Util.trace(
					this,
					"TransferCall request callInfo is null. "
							+ this.getActiveUCID());
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(failure, this.getClientID());
			return;
		}

		CallInfo held_callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.deviceID, this.getHeldUCID());
		if (held_callInfo != null) {
			this.setHeldCallID(held_callInfo.getCallID());
			Util.trace(
					this,
					"TransferCall request convert held ucid="
							+ this.getHeldUCID() + ", to callID="
							+ this.getHeldCallID());
		} else {
			Util.warn(
					this,
					"TransferCall request callInfo is null. "
							+ this.getHeldUCID());
			ResponseFailurePDU response = UDPUtil.loadFailureResponsePDU(this);
			response.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(response, this.getClientID());
			return;
		}

		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "transferCall";
		TransferCallRequest transfer = new TransferCallRequest(this.deviceID,
				this.getHeldCallID(), this.activeCallID);
		transfer.setSyncMode(SyncMode.Async);
		request.params = new TransferCallRequest[] { transfer };
		request.object = "cti";
		Object result = udpHandler.processRequest(request, this.getClientID());
		if (result != null) {
			if (!(result instanceof Future)) {
				if (result instanceof Throwable) {
					udpHandler.writeRspFailed(this, (Throwable)result);
				} else {
					ResponseTransferCallPDU response = (ResponseTransferCallPDU) UDPUtil
							.loadResponsePDU(this);
					response.setDeviceID(this.deviceID);
					udpHandler.write(response, this.getClientID());
				}
			}
		} else {
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.REQ_CTI_FAILURE);
			udpHandler.write(failure, this.getClientID());
		}
	}

}
