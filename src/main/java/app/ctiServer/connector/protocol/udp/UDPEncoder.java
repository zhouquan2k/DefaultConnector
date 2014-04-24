package app.ctiServer.connector.protocol.udp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import app.ctiServer.connector.protocol.udp.pdu.EventAgentStateChangedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentStateChangedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAlertedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAlertedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventCallClearedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventConferencedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventConnectionClearedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredV2PDU;
//import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredV3PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDivertedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventEstablishedPDU;
//import app.ctiServer.connector.protocol.udp.pdu.EventEstablishedV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.EventHeldPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventOriginatedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventQueuedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventRetrievedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventServiceInitiatedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventTransferedPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseAlternateCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseAnswerCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseClearCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseConferenceCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseConsultationCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseHeartBeatPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseHoldCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseMakeCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseMonitorDevicePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryAgentStateV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryGroupAgentListV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryGroupInfoV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryPauseCodePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryTrunkInfoV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryVDNListPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseReconnectCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseRetrieveCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseSetAgentStatePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseSingleStepConferencePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseSingleStepTransferPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseStopMonitorDevicePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseTransferCallPDU;
import component.util.Util;

/**
 * UDP Encoder class convert PDU object to UDP message.
 * 
 * @author Dev.pyh
 * 
 */
public class UDPEncoder extends SimpleChannelHandler {

	private UDPProtocolMgr protocolMgr;

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (this.protocolMgr == null)
			this.protocolMgr = Util.getBean("protocol", UDPProtocolMgr.class);
		byte[] datas = null;
		if (e.getMessage() instanceof ResponsePDU) {

			ResponsePDU responseMsg = (ResponsePDU) e.getMessage();
			UDPContext context = protocolMgr
					.getUDPConextByClientID(responseMsg.getClientID());
			InetSocketAddress remotAddress = null;
			if (context != null) {
				remotAddress = context.remotAddress;
			} else {
				Util.trace(this, "client is closed", "");
				return;
			}
			// Util.trace(this,"Response message to client ... sessionID="+responseMsg.getSessionID()+", responseName="+responseMsg.getResponseName());
			datas = this.convertResMsg2Byte(responseMsg);
			if (datas.length > 9 && remotAddress != null) {
				ChannelBuffer buf = ChannelBuffers.wrappedBuffer(datas);
				Channels.write(ctx, e.getFuture(), buf, remotAddress);
				Util.trace(
						this,
						"Send response message "
								+ responseMsg.getResponseName()
								+ " to client ok, "
								+ responseMsg.getMessageID() + ",["
								+ UDPUtil.getTimeStamp() + "]");
			} else {
				Util.warn(this,
						"This response message is not right,discard it!");
			}
		} else if (e.getMessage() instanceof EventPDU) {
			// Encode event message.
			EventPDU eventMsg = (EventPDU) e.getMessage();
			InetSocketAddress remotAddress = protocolMgr
					.getSocketAddressBySessionID(eventMsg.getSessionID());
			// Util.trace(this,"Event message to client sessionID="+eventMsg.getSessionID()+" , eventName="+eventMsg.getEventName());
			datas = this.convertEventMsg2Byte(eventMsg);
			if (datas != null && remotAddress != null) {

				if (datas.length > 9) {
					ChannelBuffer buf = ChannelBuffers.wrappedBuffer(datas);
					Channels.write(ctx, e.getFuture(), buf, remotAddress);
					Util.trace(this, "Send " + eventMsg.getEventName()
							+ " to client ok, " + eventMsg.getMessageID()
							+ ",[" + eventMsg.getSessionID() + "]");
				} else {
					Util.warn(this,
							"This event %s message is not right,discard it!",eventMsg.getEventName());
				}

			} else {
				Util.warn(this,
						"Send event $s to client failure, event data is null.",eventMsg.getEventName());
			}

		} else {
			// Unknow message type.
			Util.warn(this, "Unknow message object,Discard it. "
					+ e.getMessage().toString());
		}

	}

	/**
	 * Convert ResponsePDU object to UDP bytes.
	 * 
	 * @param responseMsg
	 * @return
	 */
	private byte[] convertResMsg2Byte(ResponsePDU responseMsg) {

		ArrayList<String> datas = new ArrayList<String>();
		// datas.add(responseMsg.getMessageID());
		if (responseMsg instanceof ResponseHeartBeatPDU) {
			// Util.trace(this,"Encode HeartBeat response package ... ");
			ResponseHeartBeatPDU response = (ResponseHeartBeatPDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add(response.getDeviceID());
			datas.add(response.getState());
			datas.add(response.getVersionNO());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_HEART_BEAT, datas);
			datas = null;
			// Util.trace(this,
			// "Response HeartBeat... "+response.getMessageID()+", "+response.getClientID());
			return res;

		} else if (responseMsg instanceof ResponseQueryGroupInfoV2PDU) {
			// Util.trace(this,
			// "Encode QueryGroupInfoV2 response package ... ");
			ResponseQueryGroupInfoV2PDU response = (ResponseQueryGroupInfoV2PDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add(response.getGroupID());
			datas.add(response.getGroupDesc());
			datas.add(response.getAvaibleAgents());
			datas.add(response.getQueueNo());
			datas.add(response.getLogedAgents());
			datas.add("");
			datas.add("");
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_GROUP_INFO_V2, datas);
			// Util.trace(this,
			// "Response QueryGroupInfo... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;

		} else if (responseMsg instanceof ResponseQueryVDNListPDU) {
			// Util.trace(this, "Encode QueryVDNList response package ... ");
			ResponseQueryVDNListPDU response = (ResponseQueryVDNListPDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add("1");
			List<String> vdnList = response.getVdnList();
			if (vdnList != null && vdnList.size() > 0) {
				datas.add(String.valueOf(vdnList.size()));
				for (int i = 0; i < vdnList.size(); i++) {
					datas.add(vdnList.get(i));
					datas.add(response.getVdnDesc().get(i));
				}
			} else {
				datas.add("0");
			}
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_VDN_LIST, datas);
			// Util.trace(this,
			// "Response QueryVDNList... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;

		} else if (responseMsg instanceof ResponseQueryPauseCodePDU) {
			// Util.trace(this,
			// "Encode QueryPauseReasonCode response package ... ");
			ResponseQueryPauseCodePDU response = (ResponseQueryPauseCodePDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add(response.getDeviceID());
			datas.add(response.getPauseCode());
			datas.add(response.getResValue());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_REASONCODE_INFO_V2, datas);
			// Util.trace(this,
			// "Response QueryPauseReasonCode... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;

		} else if (responseMsg instanceof ResponseMonitorDevicePDU) {
			// Util.trace(this,"Encode MonitorDevice response package ... ");
			ResponseMonitorDevicePDU response = (ResponseMonitorDevicePDU) responseMsg;
			datas.add(response.getMessageID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_MONITOR_DEVICE, datas);
			// Util.trace(this,
			// "Response MonitorDevice... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseStopMonitorDevicePDU) {
			// Util.trace(this,"Encode StopMonitorDevice response package ... ");
			ResponseStopMonitorDevicePDU response = (ResponseStopMonitorDevicePDU) responseMsg;
			datas.add(response.getMessageID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_STOP_MONITOR_DEVICE, datas);
			// Util.trace(this,
			// "Response StopMonitorDevice ... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseSetAgentStatePDU) {
			// Util.trace(this,"Encode SetAgentState response package ... ");
			ResponseSetAgentStatePDU response = (ResponseSetAgentStatePDU) responseMsg;
			datas.add(response.getMessageID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_CHANGE_AGENT_STATE, datas);
			// Util.trace(this,
			// "Response SetAgentState... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseMakeCallPDU) {
			// Util.trace(this, "Encode MakeCall response package ... ");
			ResponseMakeCallPDU response = (ResponseMakeCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			// String[] deviceID = device.split("/");
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_MAKE_CALL, datas);
			// Util.trace(this,
			// "Response MakeCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseHoldCallPDU) {
			// Util.trace(this, "Encode HoldCall response package ... ");
			ResponseHoldCallPDU response = (ResponseHoldCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			datas.add(response.getUcid());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_HOLD_CALL, datas);
			// Util.trace(this,
			// "Response HoldCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseRetrieveCallPDU) {
			// Util.trace(this, "Encode RetrieveCall response package ... ");
			ResponseRetrieveCallPDU response = (ResponseRetrieveCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			datas.add(response.getUcid());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_RETRIEVE_CALL, datas);
			// Util.trace(this,
			// "Response RetrieveCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseAnswerCallPDU) {
			// Util.trace(this, "Encode AnswerCall response package ... ");
			ResponseAnswerCallPDU response = (ResponseAnswerCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			datas.add(response.getUcid());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_ANSWER_CALL, datas);
			// Util.trace(this,
			// "Response AnswerCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseFailurePDU) {
			// Util.trace(this, "Encode Failure response package ... ");
			ResponseFailurePDU response = (ResponseFailurePDU) responseMsg;
			datas.add(response.getMessageID());
			byte[] res = UDPUtil
					.convertFailResponseData2Byte(UDPConstants.REQUEST_FAILURE,
							datas, response.getFailCode());
			// Util.trace(this,
			// "Response Failure... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseConsultationCallPDU) {
			// Util.trace(this, "Encode ConsulationCall response package ... ");
			ResponseConsultationCallPDU response = (ResponseConsultationCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_CONSULTATION_CALL, datas);
			// Util.trace(this,
			// "Response ConsulationCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseTransferCallPDU) {
			// Util.trace(this, "Encode TransferCallPDU response package ... ");
			ResponseTransferCallPDU response = (ResponseTransferCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_TRANSFER_CALL, datas);
			// Util.trace(this,
			// "Response TransferCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;

		} else if (responseMsg instanceof ResponseSingleStepTransferPDU) {
			// Util.trace(this,
			// "Encode SingleStepTransferPDU response package ... ");
			ResponseSingleStepTransferPDU response = (ResponseSingleStepTransferPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			datas.add(response.getCallID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_SINGLE_STEP_TRANSFER, datas);
			// Util.trace(this,
			// "Response SingleStepTransferCall ... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseConferenceCallPDU) {
			// Util.trace(this,
			// "Encode ConferenceCallPDU response package ... ");
			ResponseConferenceCallPDU response = (ResponseConferenceCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_CONFERENCE_CALL, datas);
			// Util.trace(this,
			// "Response ConferenceCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseSingleStepConferencePDU) {
			// Util.trace(this,
			// "Encode SingleStepConferencePDU response package ... ");
			ResponseSingleStepConferencePDU response = (ResponseSingleStepConferencePDU) responseMsg;
			datas.add(response.getMessageID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_SINGLE_STEP_CONFERENCE, datas);
			// Util.trace(this,
			// "Response SingleStepConference... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseAlternateCallPDU) {
			// Util.trace(this,
			// "Encode AlternateCallPDU response package ... ");
			ResponseAlternateCallPDU response = (ResponseAlternateCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_ALTERNATE_CALL, datas);
			// Util.trace(this,
			// "Response AlternateCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseClearCallPDU) {
			// Util.trace(this, "Encode ClearCallPDU response package ... ");
			ResponseClearCallPDU response = (ResponseClearCallPDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add(response.getDeviceID());
			datas.add(response.getCallID());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_CLEAR_CALL, datas);
			Util.trace(this, "Response ClearCall... " + response.getMessageID()
					+ ", " + response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseReconnectCallPDU) {
			// Util.trace(this,
			// "Encode ReconnectCallPDU response package ... ");
			ResponseReconnectCallPDU response = (ResponseReconnectCallPDU) responseMsg;
			datas.add(response.getMessageID());
			String device = response.getDeviceID();
			// String[] deviceID = device.split("/");
			datas.add(device);
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_RECONNECT_CALL, datas);
			// Util.trace(this,
			// "Response ReconnectCall... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseQueryGroupAgentListV2PDU) {
			// Util.trace(this,
			// "Encode QueryGroupAgentListV2PDU response package ... ");
			ResponseQueryGroupAgentListV2PDU response = (ResponseQueryGroupAgentListV2PDU) responseMsg;
			datas.add(response.getMessageID());
			String group = response.getGroupID();
			// String[] groupID = group.split("/");
			datas.add(group);
			datas.add(response.getGroupDesc());
			datas.add(response.getAgentList());
			datas.add(response.getResValue1());
			datas.add(response.getResValue2());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_GROUP_AGENTS_V2, datas);
			// Util.trace(this,
			// "Response QueryGroupAgentListV2... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseQueryTrunkInfoV2PDU) {
			// Util.trace(this, "Encode QueryTrunkInfo response package ... ");
			ResponseQueryTrunkInfoV2PDU response = (ResponseQueryTrunkInfoV2PDU) responseMsg;
			datas.add(response.getMessageID());
			datas.add(response.getTrunkID());
			datas.add(response.getTrunkDesc());
			datas.add(response.getAvailableTrunk());
			datas.add(response.getUsedTrunk());
			datas.add(response.getResStr1());
			datas.add(response.getResStr2());
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_TRUNK_INFO_V2, datas);
			// Util.trace(this,
			// "Response QueryTrunkInfoV2 ... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		} else if (responseMsg instanceof ResponseQueryAgentStateV2PDU) {
			// Util.trace(this,
			// "Encode QueryAgentStateV2PDU response package ... ");
			ResponseQueryAgentStateV2PDU response = (ResponseQueryAgentStateV2PDU) responseMsg;
			datas.add(response.getMessageID()); // messageID
			String device = response.getDeviceID();
			// String[] deviceID = device.split("/");
			datas.add(device); // deviceID
			datas.add(response.getAgentID()); // agentID
			datas.add(response.getAgentName()); // agentName
			datas.add(response.getGroupID()); // agentGroupID
			datas.add(response.getAgentType()); // agentType
			datas.add(response.getAgentState()); // agentState
			datas.add(response.getPauseCode()); // Reason code. // pauseCode
			datas.add(response.getLogin_Device()); // LoginedDeviceID.
			datas.add(response.getClient_IP()); // IP address.
			datas.add(response.getClient_Port()); // port.
			datas.add(response.getDeviceState()); // Station state.
			datas.add(response.getRes1()); // Res1
			datas.add(response.getRes2()); // Res2
			byte[] res = UDPUtil.convertResponseData2Byte(
					UDPConstants.REQUEST_QUERY_AGENT_STATE_V2, datas);
			// Util.trace(this,
			// "Response QueryAgentStateV2... "+response.getMessageID()+", "+response.getClientID());
			datas = null;
			return res;
		}/*
		 * else if(responseMsg instanceof ResponseSendEmbedMessagePDU){
		 * ResponseSendEmbedMessagePDU response =
		 * (ResponseSendEmbedMessagePDU)responseMsg;
		 * datas.add(response.getMessageID()); byte[] res =
		 * UDPUtil.convertResponseData2Byte
		 * (UDPConstants.REQUEST_SEND_EMBED_MESSAGES, datas); datas = null;
		 * return res; }
		 */
		else {
			return null;
		}

	}

	/**
	 * Convert EventPDU object to UDP bytes.
	 * 
	 * @param eventMsg
	 * @return
	 */
	private byte[] convertEventMsg2Byte(EventPDU eventMsg) {

		ArrayList<String> datas = new ArrayList<String>();

		if (eventMsg instanceof EventDeliveredPDU) {
			// Util.trace(this, "Encode DeliveredEvent package ... ");
			EventDeliveredPDU event = (EventDeliveredPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getDNIS());
			datas.add(event.getANI());
			datas.add(event.getCallType());
			datas.add(event.getUui());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_INCOMMING_CALL, datas);
			Util.trace(
					this,
					"Dispatch DeliveredEvent to sessionID="
							+ event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventDeliveredV2PDU) {

			EventDeliveredV2PDU event = (EventDeliveredV2PDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getDNIS());
			datas.add(event.getANI());
			datas.add(event.getCallType());
			datas.add(event.getTrunkGroup());
			datas.add(event.getTrunkMember());
			datas.add(event.getUui());
			datas.add(event.getSplit());
			datas.add(event.getVdn());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_INCOMMING_CALL_V2, datas);
			Util.trace(
					this,
					"Dispatch DeliveredEventV2 to sessionID="
							+ event.getSessionID());
			datas = null;
			return res;

//		} else if (eventMsg instanceof EventDeliveredV3PDU) {
//
//			EventDeliveredV3PDU event = (EventDeliveredV3PDU) eventMsg;
//			datas.add(event.getMessageID());
//			datas.add(event.getDeviceID());
//			datas.add(event.getUcid());
//			datas.add(event.getDNIS());
//			datas.add(event.getANI());
//			datas.add(event.getCallType());
//			datas.add(event.getTrunkGroup());
//			datas.add(event.getTrunkMember());
//			datas.add(event.getUui());
//			datas.add(event.getSplit());
//			datas.add(event.getVdn());
//			datas.add(event.getUcid());
//			byte[] res = UDPUtil.convertEventData2Byte(
//					UDPConstants.EVENT_INCOMMING_CALL_V3, datas);
//			Util.trace(
//					this,
//					"Dispatch DeliveredEventV3 to sessionID="
//							+ event.getSessionID());
//			datas = null;
//			return res;

		} else if (eventMsg instanceof EventAlertedPDU) {
			// Util.trace(this, "Encode AlertedEvent package ... ");
			EventAlertedPDU event = (EventAlertedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getANI());
			datas.add(event.getDNIS());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_ALERTING, datas);
			Util.trace(
					this,
					"Dispatch AlertedEvent to sessionID="
							+ event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventAlertedV2PDU) {
			// Util.trace(this, "Encode AlertedEvent package ... ");
			EventAlertedV2PDU event = (EventAlertedV2PDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getANI());
			datas.add(event.getDNIS());
			datas.add(event.getTrunkGroup());
			datas.add(event.getTrunkMem());
			datas.add(event.getUcid());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_ALERTING_V2, datas);
			Util.trace(
					this,
					"Dispatch AlertedEvent2 to sessionID="
							+ event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventEstablishedPDU) {
			// Util.trace(this, "Encode EstablishedEvent package ... ");
			EventEstablishedPDU event = (EventEstablishedPDU) eventMsg;
			// event.trace();
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getCalledDevice());
			datas.add(event.getTrunkGroup());
			datas.add(event.getTrunkMem());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_ESTABLISHED, datas);
			// Util.trace(this,
			// "Dispatch EstablishedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

//		} else if (eventMsg instanceof EventEstablishedV2PDU) {
//			// Util.trace(this, "Encode EstablishedEvent package ... ");
//			EventEstablishedV2PDU event = (EventEstablishedV2PDU) eventMsg;
//			// event.trace();
//			datas.add(event.getMessageID());
//			datas.add(event.getDeviceID());
//			datas.add(event.getUcid());
//			datas.add(event.getCallingDevice());
//			datas.add(event.getCalledDevice());
//			datas.add(event.getTrunkGroup());
//			datas.add(event.getTrunkMem());
//			datas.add(event.getUcid());
//			byte[] res = UDPUtil.convertEventData2Byte(
//					UDPConstants.EVENT_ESTABLISHED_V2, datas);
//			// Util.trace(this,
//			// "Dispatch EstablishedEvent to sessionID="+event.getSessionID());
//			datas = null;
//			return res;

		} else if (eventMsg instanceof EventAgentStateChangedPDU) {
			// Util.trace(this,"Encode AgentStateChangedEvent package ... ");
			EventAgentStateChangedPDU event = (EventAgentStateChangedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getAgentDevice());
			datas.add(event.getAgentID());
			datas.add(event.getAgentState());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_AGENT_STATE_CHANGED, datas);
			// Util.trace(this,
			// "Dispatch AgentStateChangedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventAgentStateChangedV2PDU) {
			// Util.trace(this, "Encode AgentStateChangedEventV2 package ... ");
			EventAgentStateChangedV2PDU event = (EventAgentStateChangedV2PDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getAgentDevice());
			datas.add(event.getAgentID());
			datas.add(event.getAgentState());
			datas.add(event.getPauseCode());
			datas.add(event.getResValue1());
			datas.add(event.getResValue2());
			datas.add(event.getAgentName());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_AGENT_STATE_CHANGED_V2, datas);
			// Util.trace(this,
			// "Dispatch AgentStateChangedEventV2 to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventFailurePDU) {
			// Util.trace(this, "Encode FailureEvent package ... ");
			EventFailurePDU event = (EventFailurePDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add("0");
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_FAILURE_CALL, datas);
			// Util.trace(this,
			// "Dispatch FailureEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventServiceInitiatedPDU) {
			// Util.trace(this, "Encode ServiceInitiatedEvent package ... ");
			EventServiceInitiatedPDU event = (EventServiceInitiatedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getUcid());
			datas.add(event.getDeviceID());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_SERVICE_INITIATED, datas);
			// Util.trace(this,
			// "Dispatch ServiceInitiatedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventOriginatedPDU) {
			// Util.trace(this, "Encode OriginatedEvent package ... ");
			EventOriginatedPDU event = (EventOriginatedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCalledNO());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_ORIGINATED_CALL, datas);
			// Util.trace(this,
			// "Dispatch OriginatedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventConnectionClearedPDU) {
			// Util.trace(this, "Encode ConnectionClearedEvent package ... ");
			EventConnectionClearedPDU event = (EventConnectionClearedPDU) eventMsg;
			String deviceID = event.getDeviceID();
			datas.add(event.getMessageID());
			datas.add(deviceID);
			datas.add(event.getUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getCalledDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_CONN_DISCONNECT, datas);
			// Util.trace(this,
			// "Dispatch ConnectionClearedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventCallClearedPDU) {

			EventCallClearedPDU event = (EventCallClearedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getCalledDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_CALL_DISCONNECT, datas);
			// Util.trace(this,
			// "Dispatch ConnectionClearedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

		} else if (eventMsg instanceof EventHeldPDU) {
			// Util.trace(this, "Encode HeldEvent package ... ");
			EventHeldPDU event = (EventHeldPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getCalledDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_HELD_CALL, datas);
			// Util.trace(this,
			// "Dispatch HeldEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventRetrievedPDU) {
			// Util.trace(this, "Encode RetrievedEvent package ... ");
			EventRetrievedPDU event = (EventRetrievedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getCalledDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_RETRIEVED_CALL, datas);
			// Util.trace(this,
			// "Dispatch RetrievedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventTransferedPDU) {
			// Util.trace(this, "Encode TransferedEvent package ... ");
			EventTransferedPDU event = (EventTransferedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getEventID());
			datas.add(event.getUcid());
			datas.add(event.getOldPrimaryUcid());
			datas.add(event.getOldSecondaryUcid());
			datas.add(event.getCallingDevice());
			datas.add(event.getTransferingDevice());
			datas.add(event.getTransferedDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_TRANSFERED_CALL, datas);
			// Util.trace(this,
			// "Dispatch TransferedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventConferencedPDU) {
			// Util.trace(this, "Encode ConferencedEvent package ... ");
			EventConferencedPDU event = (EventConferencedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getEventID());
			datas.add(event.getUcid());
			datas.add(event.getOldPrimaryUcid());
			datas.add(event.getOldSecondaryUcid());
			String deviceList = event.getDeviceList();
			String[] devices = deviceList.split(",");
			// Util.trace(this,
			// "Encoder conferenceEvent deviceList="+deviceList+",devicesLen="+devices.length);
			if (devices.length > 0) {
				datas.add("[" + devices.length + "]");
				for (String deviceId : devices) {
					datas.add(deviceId);
				}
			} else {
				datas.add("[0]");
			}
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_CONFERENCE_CALL, datas);
			// Util.trace(this,
			// "Dispatch ConferencedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventDivertedPDU) {
			// Util.trace(this, "Encode DivertedEvent package ... ");
			EventDivertedPDU event = (EventDivertedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getDeviceID());
			datas.add(event.getCalledDevice());
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_DIVERTED_CALL, datas);
			// Util.trace(this,
			// "Dispatch DivertedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;
		} else if (eventMsg instanceof EventQueuedPDU) {
			// Util.trace(this, "Encode QueuedEvent package ... ");
			EventQueuedPDU event = (EventQueuedPDU) eventMsg;
			datas.add(event.getMessageID());
			datas.add(event.getDeviceID());
			datas.add(event.getDeviceID());
			datas.add(event.getUcid());
			datas.add(event.getCalledDeviceID());
			datas.add(event.getCallingDeviceID());
			datas.add(event.getNumberQueued());
			datas.add(event.getLastRedirectDeviceID());
			datas.add("");
			byte[] res = UDPUtil.convertEventData2Byte(
					UDPConstants.EVENT_QUEUED_CALL, datas);
			// Util.trace(this,
			// "Dispatch QueuedEvent to sessionID="+event.getSessionID());
			datas = null;
			return res;

		}/*
		 * else if(eventMsg instanceof EventNotificationPDU){
		 * EventNotificationPDU event = (EventNotificationPDU)eventMsg;
		 * datas.add(event.getMessageID()); datas.add(event.getEmbedMessage());
		 * byte[] res =
		 * UDPUtil.convertEventData2Byte(UDPConstants.EVENT_SERVER_NOTIFY,
		 * datas); datas = null; return res; }
		 */
		else {
			Util.trace(this,
					"Unknow event message ... " + eventMsg.getEventName());
		}

		return null;
	}

}
