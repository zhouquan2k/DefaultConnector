package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Iterator;
import java.util.Set;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.protocol.SingleStepConferenceCallRequest;
import component.cti.protocol.SingleStepConferenceCallRequest.ParticipationType;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestSingleStepConferencePDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;
	private String callID;
	private String ucid;
	private String joinDevice;
	private String joinType;

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getJoinDevice() {
		return joinDevice;
	}

	public void setJoinDevice(String joinDevice) {
		this.joinDevice = joinDevice;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "SingleStepConferenceCall request ... deviceId="
				+ this.deviceID + ",joinDevice=" + this.getJoinDevice()
				+ ",confType=" + this.getJoinType());

		Set<CallInfo> callInfoSet = udpHandler.getCallInfoManager()
				.getCallInfoByDevice(this.getDeviceID());
		if (callInfoSet != null && callInfoSet.size() > 0) {
			// util.trace(this,
			// "SingleStepConference callInfo list size : "+callInfoList.size()+" - "+callInfoList.toString());
			if (callInfoSet.size() == 1) {
				Iterator<CallInfo> item = callInfoSet.iterator();
				CallInfo callInfo = item.next();
				if (callInfo != null) {
					this.callID = callInfo.getCallID();
					if (this.getDeviceID() != null
							&& this.getJoinDevice() != null) {
						Request request = new Request();
						request.invokeId = this.getClientID() + "_"
								+ this.messageID;
						request.method = "singleStepConferenceCall";
						SingleStepConferenceCallRequest singleStep = new SingleStepConferenceCallRequest(
								this.deviceID, this.callID, this.joinDevice);
						singleStep.setSyncMode(SyncMode.Async);

						request.object = "cti";
						if (UDPUtil.verfiyCalledNO(this.getJoinDevice())) {
							if ("0".equals(joinType)) {
								singleStep.participationType = ParticipationType.Active;
							} else {
								singleStep.participationType = ParticipationType.Silent;
							}
							request.params = new SingleStepConferenceCallRequest[] { singleStep };
							Object call_result = udpHandler.processRequest(
									request, this.getClientID());
							if (!(call_result instanceof Future)) {
								if (call_result instanceof Throwable) {
									udpHandler.writeRspFailed(this, (Throwable)call_result);
								} else {
									ResponseSingleStepConferencePDU response = (ResponseSingleStepConferencePDU) UDPUtil
											.loadResponsePDU(this);
									udpHandler.write(response,
											this.getClientID());
								}
							}

						} else {
							Util.trace(this,
									"SingleStepConference failure for input joinDevice format error, "
											+ this.getJoinDevice());
							ResponseFailurePDU failure = UDPUtil
									.loadFailureResponsePDU(this);
							failure.setFailCode(UDPConstants.REQ_ERROR_INPUTFORMAT);
							udpHandler.write(failure, this.getClientID());
						}
					} else {
						Util.trace(this,
								"SingleStepConference failure for input parameter is null.");
						ResponseFailurePDU failure = UDPUtil
								.loadFailureResponsePDU(this);
						failure.setFailCode(UDPConstants.REQ_DEVICE_NULL);
						udpHandler.write(failure, this.getClientID());
					}
				} else {
					Util.trace(this,
							"SingleStepConference failure for no call on device ["
									+ this.getDeviceID() + "]");
					ResponseFailurePDU failure = UDPUtil
							.loadFailureResponsePDU(this);
					failure.setFailCode(UDPConstants.CALL_NOT_EXIST);
					udpHandler.write(failure, this.getClientID());
				}
			} else {

				Util.trace(this,
						"SingleStepConference failure for more than one calls on devcie ["
								+ this.getDeviceID() + "] callSize : "
								+ callInfoSet.size());
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.CTI_RES_FAILURE);
				udpHandler.write(failure, this.getClientID());
			}
		} else {
			Util.trace(this,
					"SingleStepConferenceCall failure. caused by no call on device ["
							+ this.getDeviceID() + "]");
			ResponseFailurePDU failure = UDPUtil.loadFailureResponsePDU(this);
			failure.setFailCode(UDPConstants.CTI_RES_FAILURE);
			udpHandler.write(failure, this.getClientID());
		}
	}
}
