package app.ctiServer.connector.protocol.udp;

import java.util.ArrayList;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import app.ctiServer.connector.protocol.udp.pdu.RequestAlternateCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestAnswerCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestClearCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestConferenceCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestConsultationCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestDisconnectCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestHeartBeatPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestHoldCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestMakeCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestMonitorDevicePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestQueryAgentStateV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestQueryGroupInfoV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestQueryPauseCodePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestQueryVDNListPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestReconnectCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestRetrieveCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestSetAgentStatePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestSingleStepConferencePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestSingleStepTransferPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestStopMonitorDevicePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestTransferCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestUnknowPDU;
import component.util.Util;

/**
 * UDP Decoder class convert UDP message to PDU object.
 * 
 * @author Dev.pyh
 * 
 */
public class UDPDecoder extends FrameDecoder {

	private static int totalRecValue = 0;

	private RequestPDU parseRequest(byte[] buffer) {

		if (buffer != null) {
			int len = buffer.length;
			// Util.trace(this,"Handle request buffer len "+len);
			ArrayList<String> datas = null;

			if (len > 8) {
				byte messageID = buffer[7];
				;
				// Util.trace(this,"Request messageID = "+messageID);
				switch (messageID) {

				case UDPConstants.REQUEST_HEART_BEAT:
					// Util.trace(this,"Request Heart_Beat ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestHeartBeatPDU heartBeatPDU = new RequestHeartBeatPDU();
					heartBeatPDU.setRequestName("HeartBeat");
					// heartBeatPDU.setClientID(clientID);
					// heartBeatPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					heartBeatPDU.setMessageID(datas.get(0));
					Util.trace(this,
							"Request HeartBeat ... messageID=" + datas.get(0));
					datas = null;
					return heartBeatPDU;
				case UDPConstants.REQUEST_QUERY_GROUP_INFO_V2:
					// Util.trace(this,"Request Query_Group_Info_V2 ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestQueryGroupInfoV2PDU queryGroupInfoV2PDU = new RequestQueryGroupInfoV2PDU();
					queryGroupInfoV2PDU.setRequestName("QueryGroupInfoV2");
					// queryGroupInfoV2PDU.setClientID(clientID);
					// queryGroupInfoV2PDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					queryGroupInfoV2PDU.setMessageID(datas.get(0));
					queryGroupInfoV2PDU.setDeviceID(datas.get(1).trim());
					queryGroupInfoV2PDU.setGroupID(datas.get(2).trim());
					Util.trace(this, "Request QueryGroupInfo ... groupID="
							+ datas.get(2) + ", deviceID=" + datas.get(1));
					datas = null;
					return queryGroupInfoV2PDU;
				case UDPConstants.REQUEST_MONITOR_DEVICE:
					// Util.trace(this,"Request Monitor_Device ...");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestMonitorDevicePDU monitorDevicePDU = new RequestMonitorDevicePDU();
					monitorDevicePDU.setRequestName("MonitorDevice");
					// monitorDevicePDU.setClientID(clientID);
					// monitorDevicePDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					monitorDevicePDU.setMessageID(datas.get(0));
					monitorDevicePDU.setDeviceID(datas.get(1).trim());
					monitorDevicePDU.setMode(datas.get(2));
					Util.trace(this, "Request MonitorDevice ... deviceID="
							+ datas.get(1));
					datas = null;
					return monitorDevicePDU;
				case UDPConstants.REQUEST_STOP_MONITOR_DEVICE:
					// Util.trace(this,"Request Stop_Monitor_Device ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestStopMonitorDevicePDU stopMonitorDevicePDU = new RequestStopMonitorDevicePDU();
					stopMonitorDevicePDU.setRequestName("StopMonitorDevice");
					// stopMonitorDevicePDU.setClientID(clientID);
					// stopMonitorDevicePDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					stopMonitorDevicePDU.setMessageID(datas.get(0));
					stopMonitorDevicePDU.setDeviceID(datas.get(1).trim());
					Util.trace(this, "Request StopMonitorDevice ...  deviceID="
							+ datas.get(1));
					datas = null;
					return stopMonitorDevicePDU;

				case UDPConstants.REQUEST_MAKE_CALL:
					// Util.trace(this,"Request Make_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestMakeCallPDU makeCallPDU = new RequestMakeCallPDU();
					makeCallPDU.setRequestName("MakeCall");
					// makeCallPDU.setClientID(clientID);
					// makeCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					makeCallPDU.setMessageID(datas.get(0));
					makeCallPDU.setDeviceID(datas.get(1));
					makeCallPDU.setCallingDeviceID(datas.get(1));
					makeCallPDU.setCalledDeviceID(datas.get(2).trim());
					makeCallPDU.setCallType(datas.get(3));
					makeCallPDU.setUui(datas.get(4).trim());
					Util.trace(this, "Request MakeCall ... callingDevice="
							+ datas.get(0) + ", calledDevice=" + datas.get(1));
					datas = null;
					return makeCallPDU;

				case UDPConstants.REQUEST_CHANGE_AGENT_STATE:
					// Util.trace(this,"Request Change_Agent_State ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestSetAgentStatePDU setAgentStatePDU = new RequestSetAgentStatePDU();
					setAgentStatePDU.setRequestName("SetAgentState");
					// setAgentStatePDU.setClientID(clientID);
					setAgentStatePDU.setMessageTimeStamp(new Date());
					setAgentStatePDU.setMessageID(datas.get(0));
					setAgentStatePDU.setDeviceID(datas.get(1));
					setAgentStatePDU.setAgentID(datas.get(2).trim());
					setAgentStatePDU.setAgentPwd(datas.get(3).trim());
					setAgentStatePDU.setSkillGroup(datas.get(4));
					setAgentStatePDU.setAgentMode(datas.get(5));
					if (datas.size() > 6) {
						String pauseCode = datas.get(6);
						if (pauseCode != null) {
							setAgentStatePDU.setPauseCode(pauseCode);
						} else {
							setAgentStatePDU.setPauseCode("0");
							Util.trace(this,
									"ChangeAgentState request pauseCode is null.");
						}
					}

					if (datas.size() > 7) {
						String pending = datas.get(7);
						if (pending != null) {
							setAgentStatePDU.setPending(pending);
							Util.trace(this, "SetAgentState request pending : "
									+ pending);
						}
					}

					Util.trace(this, "Request ChangeAgentState ... agentID="
							+ datas.get(2) + ", agentMode=" + datas.get(5));
					datas = null;
					return setAgentStatePDU;

				case UDPConstants.REQUEST_ANSWER_CALL:
					// Util.trace(this,"Request Answer_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestAnswerCallPDU answerCallPDU = new RequestAnswerCallPDU();
					answerCallPDU.setRequestName("AnswerCall");
					// answerCallPDU.setClientID(clientID);
					// answerCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					answerCallPDU.setMessageID(datas.get(0));
					answerCallPDU.setDeviceID(datas.get(1));
					answerCallPDU.setUcid(datas.get(2));
					Util.trace(this,
							"Request AnswerCall ... callID=" + datas.get(2));
					datas = null;
					return answerCallPDU;

				case UDPConstants.REQUEST_CONSULTATION_CALL:
					// Util.trace(this,"Request Consulation_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestConsultationCallPDU consulateCallPDU = new RequestConsultationCallPDU();
					consulateCallPDU.setRequestName("ConsultationCall");
					// consulateCallPDU.setClientID(clientID);
					// consulateCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					consulateCallPDU.setMessageID(datas.get(0));
					consulateCallPDU.setDeviceID(datas.get(1));
					consulateCallPDU.setUcid(datas.get(2));
					consulateCallPDU.setCalledDevice(datas.get(3).trim());
					consulateCallPDU.setCallType(datas.get(4));
					consulateCallPDU.setUui(datas.get(5).trim());
					Util.trace(this, "Request ConsulationCall ...  callID="
							+ datas.get(2) + ", calledDevice=" + datas.get(3));
					datas = null;
					return consulateCallPDU;

				case UDPConstants.REQUEST_HOLD_CALL:
					// Util.trace(this,"Request Hold_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestHoldCallPDU holdCallPDU = new RequestHoldCallPDU();
					holdCallPDU.setRequestName("HoldCall");
					// holdCallPDU.setClientID(clientID);
					// holdCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					holdCallPDU.setMessageID(datas.get(0));
					holdCallPDU.setDeviceID(datas.get(1));
					holdCallPDU.setUcid(datas.get(2));
					Util.trace(this,
							"Request HoldCall ...  callID=" + datas.get(2));
					datas = null;
					return holdCallPDU;

				case UDPConstants.REQUEST_RETRIEVE_CALL:
					// Util.trace(this,"Request Retrieve_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestRetrieveCallPDU retrieveCallPDU = new RequestRetrieveCallPDU();
					retrieveCallPDU.setRequestName("RetrieveCall");
					// retrieveCallPDU.setClientID(clientID);
					// retrieveCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					retrieveCallPDU.setMessageID(datas.get(0));
					retrieveCallPDU.setDeviceID(datas.get(1));
					retrieveCallPDU.setUcid(datas.get(2));
					Util.trace(this, "Request RetrieveCall ...  callID="
							+ datas.get(2));
					datas = null;
					return retrieveCallPDU;

				case UDPConstants.REQUEST_TRANSFER_CALL:
					// Util.trace(this,"Request Transfer_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestTransferCallPDU transferCallPDU = new RequestTransferCallPDU();
					transferCallPDU.setRequestName("TransferCall");
					// transferCallPDU.setClientID(clientID);
					// transferCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					transferCallPDU.setMessageID(datas.get(0));
					transferCallPDU.setDeviceID(datas.get(1));
					transferCallPDU.setHeldUCID(datas.get(2));
					transferCallPDU.setActiveUCID(datas.get(4));
					Util.trace(this, "Request TransferCall ...  activeCallID="
							+ datas.get(4));
					datas = null;
					return transferCallPDU;

				case UDPConstants.REQUEST_ALTERNATE_CALL:
					// Util.trace(this,"Request Alternate_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestAlternateCallPDU alternateCallPDU = new RequestAlternateCallPDU();
					alternateCallPDU.setRequestName("AlternateCall");
					// alternateCallPDU.setClientID(clientID);
					// alternateCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					alternateCallPDU.setMessageID(datas.get(0));
					alternateCallPDU.setDeviceID(datas.get(1));
					alternateCallPDU.setHeldUCID(datas.get(2));
					alternateCallPDU.setActiveUCID(datas.get(4));
					Util.trace(this, "Request AlternateCall... activeCallID="
							+ datas.get(4) + ", heldCallID=" + datas.get(2));
					datas = null;
					return alternateCallPDU;

				case UDPConstants.REQUEST_RECONNECT_CALL:
					// Util.trace(this,"Request Reconnect_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestReconnectCallPDU reconnectCallPDU = new RequestReconnectCallPDU();
					reconnectCallPDU.setRequestName("ReconnectCall");
					// reconnectCallPDU.setClientID(clientID);
					// reconnectCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					reconnectCallPDU.setMessageID(datas.get(0));
					reconnectCallPDU.setDeviceID(datas.get(1));
					reconnectCallPDU.setHeldUCID(datas.get(2));
					reconnectCallPDU.setActiveUCID(datas.get(4));
					Util.trace(this, "Request ReconnectCall... activeCallID="
							+ datas.get(4) + ", heldCallID=" + datas.get(2));
					datas = null;
					return reconnectCallPDU;

				case UDPConstants.REQUEST_CONFERENCE_CALL:
					// util.trace(this,"Request Conference_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestConferenceCallPDU conferenceCallPDU = new RequestConferenceCallPDU();
					conferenceCallPDU.setRequestName("ConferenceCall");
					// conferenceCallPDU.setClientID(clientID);
					// conferenceCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					conferenceCallPDU.setMessageID(datas.get(0));
					conferenceCallPDU.setDeviceID(datas.get(1));
					conferenceCallPDU.setHeldUCID(datas.get(2));
					conferenceCallPDU.setActiveUCID(datas.get(4));
					Util.trace(this, "Request ConferenceCall ... activeCallID="
							+ datas.get(4) + ", heldCallID=" + datas.get(2));
					datas = null;
					return conferenceCallPDU;

				case UDPConstants.REQUEST_CLEAR_CALL:
					// Util.trace(this,"Request Clear_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestClearCallPDU clearCallPDU = new RequestClearCallPDU();
					clearCallPDU.setRequestName("ClearCall");
					// clearCallPDU.setClientID(clientID);
					// clearCallPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					clearCallPDU.setMessageID(datas.get(0));
					clearCallPDU.setDeviceID(datas.get(1));
					clearCallPDU.setUcid(datas.get(2));
					Util.trace(this,
							"Request ClearCall ... callID=" + datas.get(2));
					datas = null;
					return clearCallPDU;

				case UDPConstants.REQUEST_SINGLE_STEP_TRANSFER:
					// Util.trace(this,"Request Single_Step_Transfer_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestSingleStepTransferPDU singleTransferPDU = new RequestSingleStepTransferPDU();
					singleTransferPDU.setRequestName("SingleStepTransfer");
					// singleTransferPDU.setClientID(clientID);
					// singleTransferPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					singleTransferPDU.setMessageID(datas.get(0));
					singleTransferPDU.setDeviceID(datas.get(1));
					singleTransferPDU.setDestDeviceID(datas.get(2).trim());
					singleTransferPDU.setCallType(datas.get(3));
					singleTransferPDU.setUui(datas.get(4).trim());
					Util.trace(
							this,
							"Request SingleStepTransferCall...  deviceID="
									+ datas.get(1) + ", destDeviceID="
									+ datas.get(2));
					datas = null;
					return singleTransferPDU;

				case UDPConstants.REQUEST_SINGLE_STEP_CONFERENCE:
					// Util.trace(this,"Request Single_Step_Conference_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestSingleStepConferencePDU singleConferencePDU = new RequestSingleStepConferencePDU();
					singleConferencePDU.setRequestName("SingleStepConference");
					// singleConferencePDU.setClientID(clientID);
					// singleConferencePDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					singleConferencePDU.setMessageID(datas.get(0));
					singleConferencePDU.setCallID(datas.get(1)); // UCID maybe
																	// is null.
					singleConferencePDU.setDeviceID(datas.get(2));
					singleConferencePDU.setJoinDevice(datas.get(3).trim());
					singleConferencePDU.setJoinType(datas.get(4));
					Util.trace(
							this,
							"Request SingleStepConferenceCall...  deviceID="
									+ datas.get(2) + ", joinDevice="
									+ datas.get(3) + ", joinType="
									+ datas.get(4));
					datas = null;
					return singleConferencePDU;

				case UDPConstants.REQUEST_QUERY_AGENT_STATE_V2:
					// Util.trace(this,"Request Query_Agent_State_V2 ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestQueryAgentStateV2PDU queryAgentStateExPDU = new RequestQueryAgentStateV2PDU();
					queryAgentStateExPDU.setRequestName("QueryAgentStateV2");
					// queryAgentStateExPDU.setClientID(clientID);
					// queryAgentStateExPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					queryAgentStateExPDU.setMessageID(datas.get(0));
					queryAgentStateExPDU.setDeviceID(datas.get(1).trim());
					queryAgentStateExPDU.setAgentID(datas.get(2).trim());
					Util.trace(this, "Request QueryAgentStateV2...  agentID="
							+ datas.get(2));
					datas = null;
					return queryAgentStateExPDU;

				case UDPConstants.REQUEST_QUERY_VDN_LIST:
					// Util.trace(this,"Request Query_VDN_List ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestQueryVDNListPDU queryVDNPDU = new RequestQueryVDNListPDU();
					queryVDNPDU.setRequestName("QueryVDNList");
					// queryVDNPDU.setClientID(clientID);
					// queryVDNPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					queryVDNPDU.setMessageID(datas.get(0));
					Util.trace(this, "Request QueryVDNList... ");
					datas = null;
					return queryVDNPDU;

				case UDPConstants.REQUEST_QUERY_REASONCODE_INFO_V2:
					// Util.trace(this,"Request Query_ReasonCode_Info_V2 ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestQueryPauseCodePDU queryPauseCodePDU = new RequestQueryPauseCodePDU();
					queryPauseCodePDU.setRequestName("QueryPauseCode");
					// queryPauseCodePDU.setClientID(clientID);
					// queryPauseCodePDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					queryPauseCodePDU.setMessageID(datas.get(0));
					queryPauseCodePDU.setDeviceID(datas.get(1));
					queryPauseCodePDU.setResValue(datas.get(2));
					Util.trace(this, "Request QueryPauseCode... ");
					datas = null;
					return queryPauseCodePDU;

				case UDPConstants.REQUEST_DISCONNECT_CALL:
					// Util.trace(this, "Request Disconnect_Call ... ");
					datas = UDPUtil.convertBuffer2Strings(buffer);
					RequestDisconnectCallPDU disConnectPDU = new RequestDisconnectCallPDU();
					disConnectPDU.setRequestName("DisConnectCall");
					// disConnectPDU.setClientID(clientID);
					// disConnectPDU.setMessageTimeStamp(UDPUtil.getTimeStamp());
					disConnectPDU.setMessageID(datas.get(0));
					disConnectPDU.setDeviceID(datas.get(1));
					disConnectPDU.setDestDeviceID(datas.get(2));
					Util.trace(this, "Request DisconnectCall ... ");
					datas = null;
					return disConnectPDU;

				case UDPConstants.REQUEST_QUERY_QUEUE_INFO:
					// Util.trace(this,"Request Query_Queue_Info ... ");
					RequestUnknowPDU unSupportPDU = new RequestUnknowPDU();
					unSupportPDU.setRequestName("Unknow");
					return unSupportPDU;

					/*
					 * case UDPConstants.REQUEST_SEND_EMBED_MESSAGES: datas =
					 * UDPUtil.convertBuffer2Strings(buffer);
					 * RequestEmbedMessagePDU embedPDU = new
					 * RequestEmbedMessagePDU();
					 * embedPDU.setRequestName("request_embedMessage");
					 * embedPDU.setClientID(clientID);
					 * embedPDU.setMessageID(datas.get(0));
					 * embedPDU.setEmbedMsg(datas.get(1)); Util.trace(this,
					 * "Request SendEmbedMessage ... "); datas = null; return
					 * embedPDU;
					 */

				default:
					Util.trace(this,
							"Unknow message, discard it. " + buffer.toString());
					RequestUnknowPDU unknowPDU = new RequestUnknowPDU();
					unknowPDU.setRequestName("Unknow");
					return unknowPDU;
				}

			} else {
				Util.trace(this, "Bad request message ,discard it.");
			}
		} else {
			Util.warn(this, "Handle request buffer is null.");
		}

		return null;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		// InetSocketAddress remoteAddr =
		// (InetSocketAddress)channel.getRemoteAddress();
		// Inet4Address ipAddr = (Inet4Address)remoteAddr.getAddress();
		// String clientSessionID =
		// ipAddr.getHostAddress()+":"+remoteAddr.getPort();
		totalRecValue = totalRecValue + 1;
		if (buffer.readableBytes() < 8)
			return null;
		Date requestTimeStamp = new Date();
		buffer.markReaderIndex();
		int end_index = buffer.bytesBefore(UDPConstants.PKG_TAIL);
		int start_index = buffer.bytesBefore(UDPConstants.PKG_HEAD);
		if (start_index == -1 || end_index == -1) {
			buffer.resetReaderIndex();
			return null;
		}
		buffer.readerIndex(start_index);
		byte[] datas = new byte[end_index - start_index + 1];
		buffer.readBytes(datas, start_index, datas.length);
		RequestPDU requestPDU = this.parseRequest(datas);
		// requestPDU.setMessageTimeStamp(requestTimeStamp);
		if (requestPDU != null) {
			if ("Unknow".equals(requestPDU.getRequestName())) {
				Util.trace(this, "Request PDU is unknow,discard this message.");
			} else {
				requestPDU.setMessageTimeStamp(requestTimeStamp);
				return requestPDU;
			}
		} else {
			Util.warn(this, "Request PDU is null,discard this message .");
		}
		return null;
	}

}
