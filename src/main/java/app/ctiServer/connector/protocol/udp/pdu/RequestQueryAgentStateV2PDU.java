package app.ctiServer.connector.protocol.udp.pdu;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPConstants;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import app.ctiServer.connector.protocol.udp.UDPUtil;
import component.cti.Constants.AgentMode;
import component.cti.Constants.DeviceState;
import component.cti.model.IAgent;
import component.cti.model.IDevice;
import component.cti.model.IAgent.IAgentQueue;
import component.cti.protocol.SnapshotDeviceRequest;
import component.cti.protocol.SnapshotDeviceResponse;
import component.cti.server.data.SAgent;
import component.cti.server.data.SStation;
import component.util.Future;
import component.util.SyncMode;
import component.util.Util;

public class RequestQueryAgentStateV2PDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String agentID;

	private String deviceID;

	private String deviceState;

	public String getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(String deviceState) {
		this.deviceState = deviceState;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "QueryAgentStateV2 request ... agentId="
				+ this.agentID + ", deviceId=" + this.deviceID);
		Request request = new Request();
		request.invokeId = this.getClientID() + "_" + this.messageID;
		request.method = "snapshotDevice";
		SnapshotDeviceRequest agentState = new SnapshotDeviceRequest(
				this.deviceID);
		agentState.agentId = this.agentID;
		agentState.setSyncMode(SyncMode.Async);
		request.params = new SnapshotDeviceRequest[] { agentState };
		request.object = "cti";
		Object result = udpHandler.processRequest(request, this.getClientID());
		if (!(result instanceof Future)) {
			if (result instanceof Throwable) {
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(this);
				failure.setFailCode(UDPConstants.CSTAUniversalFailure_t.GENERIC_OPERATION
						.getValue());
				udpHandler.write(failure, this.getClientID());

			} else {
				SnapshotDeviceResponse snashotAgent = (SnapshotDeviceResponse) result;
				this.sendResponse2Client(snashotAgent.device, udpHandler);
			}
		}
	}

	private void sendResponse2Client(IDevice device, UDPHandler udpHandler)
			throws Throwable {

		ResponseQueryAgentStateV2PDU response_queryAgentState = new ResponseQueryAgentStateV2PDU();
		SStation station = (SStation) device;
		response_queryAgentState.setSessionID(this.getSessionID());
		response_queryAgentState.setMessageID(this.getMessageID());
		response_queryAgentState.setResponseName("Response_QueryAgentStateV2");
		response_queryAgentState.setMessageTimeStamp(new Date());
		response_queryAgentState.setRequestTime(this.getMessageTimeStamp());
		response_queryAgentState.setClientID(this.getClientID());
		response_queryAgentState.setDeviceID(this.getDeviceID());
		response_queryAgentState.setAgentID(this.getAgentID());
		IAgent sAgentState = station.getAgent();

		Collection<IAgentQueue> groupList = ((SAgent) sAgentState).getQueues();
		String groups = "";
		if (groupList != null) {
			int groupListSize = groupList.size();
			StringBuffer group_sb = new StringBuffer("[" + groupListSize + "]");
			for (IAgentQueue group : groupList) {
				group_sb.append("[" + group.getQueueId() + "]");
			}

			groups = group_sb.toString();

		} else {
			groups = "[0]";
		}
		response_queryAgentState.setGroupID(groups);
		//
		response_queryAgentState.setPauseCode("");

		String deviceID = device.getId();
		// util.trace(this,
		// "QueryAgentState agentDevice for loginedDevice : "+login_device);
		String deviceState = "0";

		if (deviceID != null && deviceID.length() > 0) {
			response_queryAgentState.setLogin_Device(deviceID);
			String agentState = "1";
			if (AgentMode.Logout == sAgentState.getMode()) {
				agentState = "1";
			} else if (AgentMode.NotReady == sAgentState.getMode()) {
				agentState = "2";
				response_queryAgentState.setPauseCode(station.detailString());
			} else if (AgentMode.Ready == sAgentState.getMode()) {
				agentState = "3";
			} else if (AgentMode.WorkNotReady == sAgentState.getMode()) {
				agentState = "4";
			} else {
				agentState = "1";
			}

			// "0" idle,"1",busy,"2",inbound,"3"
			// outbound,"4",callRing,"5",dailing
			DeviceState dStatus = device.getDeviceState();
			String deviceStatus = "0";
			// "0" idle, "1",busy.
			if ("0".equals(deviceState)) {
				deviceStatus = "0";
			} else if ("1".equals(deviceState)) {

				if (dStatus != null) {
					switch (sAgentState.getStatus()) {
					case Conference:
						deviceStatus = "2";
						break;
					case Dailing:
						deviceStatus = "5";
						break;
					case OnCallIn:
						deviceStatus = "2";
						break;
					case Itnlcall:
						deviceStatus = "3";
						break;
					case Ringback:
						deviceStatus = "2";
						break;
					case OnCallOut:
						deviceStatus = "3";
						break;
					case Ringing:
						deviceStatus = "4";
						break;
					default:
						deviceStatus = "1";
					}
				} else {
					// util.trace(this,
					// "QueryAgentState reponse agentStatus is null.");
					deviceStatus = "1";
				}

			} else {
				// util.trace(this, "Unknow deviceStatus : "+deviceState);
				deviceStatus = "0";
			}

			// util.trace(this, "QueryAgentState deviceStatus : "+deviceStatus);
			response_queryAgentState.setDeviceState(deviceStatus);

			Date login_Date = sAgentState.getLoginTime();
			// SimpleDateFormat sdf = new
			// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String agentLoginTime = "";
			if (login_Date != null) {
				agentLoginTime = new SimpleDateFormat("HH:mm:ss")
						.format(login_Date);
			}

			response_queryAgentState.setAgentState(agentState);

			response_queryAgentState.setRes1(agentLoginTime);
			response_queryAgentState.setRes2("");

			response_queryAgentState.setAgentType(""); // Agent Type "0"
														// Monitor,"1" Agent.

			udpHandler.write(response_queryAgentState, this.getClientID());
		}

	}

}
