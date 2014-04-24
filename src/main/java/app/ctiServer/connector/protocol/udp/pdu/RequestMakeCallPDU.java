package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.MakeCallRequest;
import component.cti.protocol.UserData;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

/**
 * MakeCall Request object.
 * 
 * @author Dev.pyh
 * 
 */
public class RequestMakeCallPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String callingDeviceID;

	private String calledDeviceID;

	private String callType;

	private String uui;

	public String getCallType() {
		return this.callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getCallingDeviceID() {
		return callingDeviceID;
	}

	public void setCallingDeviceID(String callingDeviceID) {
		this.callingDeviceID = callingDeviceID;
	}

	public String getCalledDeviceID() {
		return calledDeviceID;
	}

	public void setCalledDeviceID(String calledDeviceID) {
		this.calledDeviceID = calledDeviceID;
	}

	public String getUui() {
		return uui;
	}

	public void setUui(String uui) {
		this.uui = uui;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"MakeCall Request ... deviceId=" + this.getDeviceID()
						+ ", calledDevice=" + this.getCalledDeviceID()
						+ ",uui=" + this.getUui());
		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "makeCall";
		MakeCallRequest makeCall = new MakeCallRequest(this.deviceID,
				this.callingDeviceID);
		makeCall.origin = this.callingDeviceID;
		makeCall.dest = this.calledDeviceID;
		makeCall.userData = new UserData();
		makeCall.setSyncMode(SyncMode.Async);
		makeCall.userData.setProperty("uui", this.getUui());
		request.params = new MakeCallRequest[] { makeCall };
		request.object = "cti";
		if (this.deviceID != null && this.calledDeviceID != null) {
			if (UDPUtil.verfiyCalledNO(this.calledDeviceID)) {
				Object result = udpHandler.processRequest(request,
						this.getClientID());

				if (!(result instanceof Future))
					if (result instanceof Throwable) {
						udpHandler.writeRspFailed(this, (Throwable)result);

					} else {
						ResponseMakeCallPDU response = (ResponseMakeCallPDU) UDPUtil
								.loadResponsePDU(this);
						response.setDeviceID(this.getDeviceID());
						udpHandler.write(response, this.getClientID());
					}

			} else {
				Util.trace(this,
						"MakeCall failure for input calledDeviceID format error, "
								+ this.getCalledDeviceID());
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.REQ_ERROR_INPUTFORMAT);
				udpHandler.write(failure, this.getClientID());
			}
		} else {
			Util.trace(this, "MakeCall failure for input parameter is null.");
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.REQ_DEVICE_NULL);
			udpHandler.write(failure, this.getClientID());
		}

	}
}
