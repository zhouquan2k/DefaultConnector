package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Iterator;
import java.util.Set;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.SingleStepTransferCallRequest;
import component.cti.protocol.UserData;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestSingleStepTransferPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String oldCallID;

	private String destDeviceID;

	private String userInfo;

	private String callType;

	private String uui;

	private String ucid;

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getUui() {
		return uui;
	}

	public void setUui(String uui) {
		this.uui = uui;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getOldCallID() {
		return oldCallID;
	}

	public void setOldCallID(String oldCallID) {
		this.oldCallID = oldCallID;
	}

	public String getDestDeviceID() {
		return destDeviceID;
	}

	public void setDestDeviceID(String destDeviceID) {
		this.destDeviceID = destDeviceID;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(
				this,
				"SingleStepTransferCall request ... deviceId="
						+ this.getDeviceID() + ",destDevice="
						+ this.getDestDeviceID() + ",uui=" + this.getUui());
		Set<CallInfo> callInfoSet = udpHandler.getCallInfoManager()
				.getCallInfoByDevice(this.getDeviceID());
		if (callInfoSet != null && callInfoSet.size() > 0) {
			if (callInfoSet.size() == 1) {
				Iterator<CallInfo> item = callInfoSet.iterator();
				CallInfo callInfo = item.next();
				if (callInfo != null) {
					this.oldCallID = callInfo.getCallID();
					this.ucid = callInfo.getUCID();
				}

			} else {
				Util.warn(this, "SingleStepTransfer deviceID [" + deviceID
						+ "] has more than one calls. " + callInfoSet.size());
				for (CallInfo activeCall : callInfoSet) {
					if (activeCall.getLiveFlag() == 0) {
						this.oldCallID = activeCall.getCallID();
						this.ucid = activeCall.getUCID();
						Util.trace(this,
								"Now activeCall : " + activeCall.getCallID()
										+ ", " + activeCall.getCallingDevice()
										+ " on deviceID [" + deviceID + "]");
						break;
					}
				}

				if (this.oldCallID == null) {
					ResponseFailurePDU failure = UDPUtil
							.loadFailureResponsePDU(this);
					failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
					udpHandler.write(failure, this.getDestDeviceID());
					return;
				}
			}

		} else {
			Util.trace(this,
					"SingleStepTransfer no call on this device deviceID="
							+ deviceID);
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(failure, this.getDestDeviceID());
			return;
		}

		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "singleStepTransferCall";
		SingleStepTransferCallRequest singleStep = new SingleStepTransferCallRequest(
				this.deviceID, this.oldCallID, this.destDeviceID);

		if (this.uui != null) {
			singleStep.userData = new UserData();
			singleStep.userData.setProperty("uui", this.getUui());
		}
		singleStep.setSyncMode(SyncMode.Async);
		request.params = new SingleStepTransferCallRequest[] { singleStep };
		request.object = "cti";
		Object result = udpHandler.processRequest(request, this.getClientID());
		if (this.getDestDeviceID() != null && this.oldCallID != null) {
			if (UDPUtil.verfiyCalledNO(this.getDestDeviceID())) {
				if (!(result instanceof Future)) {
					if (result instanceof Throwable) {
						udpHandler.writeRspFailed(this, (Throwable)result);
					} else {
						ResponseSingleStepTransferPDU response = (ResponseSingleStepTransferPDU) UDPUtil
								.loadResponsePDU(this);
						response.setDeviceID(this.getDeviceID());
						response.setCallID(this.ucid);
						udpHandler.write(response, this.getClientID());
					}
				}

			} else {
				Util.trace(this,
						"SingleStepTransfer failure for input destDeviceID format error, "
								+ this.getDestDeviceID());
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.REQ_ERROR_INPUTFORMAT);
				udpHandler.write(failure, this.getClientID());
			}
		} else {
			Util.trace(this,
					"SingleStepTransfer failure for input parameter is null.");
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.REQ_DEVICE_NULL);
			udpHandler.write(failure, this.getClientID());
		}
	}
}
