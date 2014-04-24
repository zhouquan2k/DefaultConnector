package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.ConsultationCallRequest;
import component.cti.protocol.UserData;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestConsultationCallPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

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

	private String callID;

	private String ucid;

	private String callType;

	private String uui;

	private String calledDevice;

	public String getCalledDevice() {
		return calledDevice;
	}

	public void setCalledDevice(String calledDevice) {
		this.calledDevice = calledDevice;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "ConsulationCall request ... deviceId="
				+ this.deviceID + ",callId=" + this.ucid + ",calledDevice="
				+ this.calledDevice + ",uui=" + this.uui);

		CallInfo callInfo = udpHandler.getCallInfoManager()
				.getActiveCallInfoByUcid(this.getDeviceID(), this.getUcid());
		if (callInfo != null) {
			this.setCallID(callInfo.getCallID());
			Util.trace(this,
					"ConsulationCall request convert ucid=" + this.getUcid()
							+ ", to callID=" + this.getCallID());
		} else {
			Util.warn(
					this,
					"ConsulationCall request callInfo is null. "
							+ this.getUcid());
			ResponseFailurePDU response = UDPUtil.loadFailureResponsePDU(this);
			response.setFailCode(UDPConstants.CALL_NOT_EXIST);
			udpHandler.write(response, this.getClientID());
			return;
		}
		if (this.getDeviceID() != null && this.getCalledDevice() != null) {
			if(UDPUtil.verfiyCalledNO(this.getCalledDevice())){
			Request request = new Request();
			request.invokeId = this.getClientID() + "_" + this.messageID;
			request.method = "consultationCall";
			ConsultationCallRequest consultationCall = new ConsultationCallRequest(
					this.deviceID, callID, this.calledDevice);
			consultationCall.setSyncMode(SyncMode.Async);
			consultationCall.userData = new UserData();
			consultationCall.userData.setProperty("uui", this.uui);
			request.params = new ConsultationCallRequest[] { consultationCall };
			request.object = "cti";
			Object result = udpHandler.processRequest(request,
					this.getClientID());
			if (!(result instanceof Future)) {
				if (UDPUtil.verfiyCalledNO(this.getCalledDevice())) {
					if (result instanceof Throwable) {
						udpHandler.writeRspFailed(this, (Throwable)result);
					} else {
						ResponseAlternateCallPDU response = (ResponseAlternateCallPDU) UDPUtil
								.loadResponsePDU(this);
						response.setDeviceID(this.deviceID);
						udpHandler.write(response, this.getClientID());
					}
				}
			} else {
				Util.trace(this,
						"ConsulationCall failure for calledDevice format error, "
								+ this.getCalledDevice());
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.REQ_ERROR_INPUTFORMAT);
				udpHandler.write(failure, this.getClientID());
			}
			}else{
				Util.trace(this, "ConsulationCall failure for calledDevice format error, "+this.getCalledDevice());
				ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.REQ_ERROR_INPUTFORMAT);
				udpHandler.write(failure,this.getClientID());
			}
		} else {
			Util.trace(this,
					"ConsulationCall failure for input parmater is null.");
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.REQ_DEVICE_NULL);
			udpHandler.write(failure, this.getClientID());
		}
	}

}
