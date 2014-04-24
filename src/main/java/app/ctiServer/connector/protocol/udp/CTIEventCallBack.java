package app.ctiServer.connector.protocol.udp;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentLoginPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentLogoutPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentNotReadyPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentReadyPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentStateChangedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAgentWorkNotReadyPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventAlertedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventCallClearedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventConferencedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventConnectionClearedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDeliveredV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.EventDivertedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventEstablishedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventHeldPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventOriginatedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventQueuedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventRetrievedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventServiceInitiatedPDU;
import app.ctiServer.connector.protocol.udp.pdu.EventTransferedPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestAlternateCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestAnswerCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestClearCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestConferenceCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestConsultationCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestHoldCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestMakeCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestQueryAgentStateV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestReconnectCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestRetrieveCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestSetAgentStatePDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestTransferCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseAlternateCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseAnswerCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseClearCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseConferenceCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseConsultationCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseHoldCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseMakeCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryAgentStateV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryGroupAgentListV2PDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseReconnectCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseRetrieveCallPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseSetAgentStatePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseTransferCallPDU;
import component.cti.Constants.AgentMode;
//import component.cti.event.AgentBusyEvent;
import component.cti.event.AgentLoggedOffEvent;
import component.cti.event.AgentLoggedOnEvent;
import component.cti.event.AgentNotReadyEvent;
import component.cti.event.AgentReadyEvent;
import component.cti.event.AgentWorkingAfterCallEvent;
import component.cti.event.CallClearedEvent;
import component.cti.event.ConferencedEvent;
import component.cti.event.ConnectionClearedEvent;
import component.cti.event.DeliveredEvent;
import component.cti.event.DivertedEvent;
import component.cti.event.EstablishedEvent;
import component.cti.event.HeldEvent;
import component.cti.event.OriginatedEvent;
import component.cti.event.QueuedEvent;
import component.cti.event.RetrievedEvent;
import component.cti.event.ServiceInitiatedEvent;
import component.cti.event.TransferredEvent;
import component.cti.model.IAgent.IAgentQueue;
import component.cti.model.IDevice;
import component.cti.protocol.SnapshotDeviceResponse;
import component.cti.protocol.UserData;
import component.cti.server.data.SAgent;
import component.cti.server.data.SStation;
import component.util.Event;
import component.util.Util;

public class CTIEventCallBack {

	private UDPProtocolMgr protocolMgr;

	public CTIEventCallBack(UDPProtocolMgr protocolMgr) {
		this.protocolMgr = protocolMgr;
		Util.info(this, "Init Event Callback ok.");
	}

	/**
	 * Package new reponsePDU object.
	 * 
	 * @param message
	 * @return
	 * @throws JMSException
	 */
	public ResponsePDU packagResponsePDU(Object r, Request req)
			throws Throwable {

		RequestPDU requestPDU = protocolMgr.getRequestMaps().get(req.invokeId);
		protocolMgr.getRequestMaps().remove(req.invokeId);
		String requestName = req.method;
		if (requestPDU != null) {

			if (r instanceof Throwable) {

				Util.trace(
						this,
						"Response failure @ requestName="
								+ requestPDU.getRequestName() + " for "
								+ ((Throwable) r).getMessage());
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(requestPDU);
				String msg = ((Throwable) r).getMessage();
				failure.setFailCode(UDPConstants.CSTAUniversalFailure_t
						.parse(msg));
				return failure;

			} else {
				ResponsePDU response = UDPUtil.loadResponsePDU(requestPDU);

				if ("setAgentState".equalsIgnoreCase(requestName)) {
					ResponseSetAgentStatePDU response_setAgentState = (ResponseSetAgentStatePDU) response;
					RequestSetAgentStatePDU reqeust_setAgentState = (RequestSetAgentStatePDU) requestPDU;
					response_setAgentState.setDeviceID(reqeust_setAgentState
							.getDeviceID());
					response_setAgentState.setAgentID(reqeust_setAgentState
							.getAgentID());
					response_setAgentState.setAgentState(reqeust_setAgentState
							.getAgentMode());
					response_setAgentState.setAgentName(reqeust_setAgentState
							.getAgentName());
					response_setAgentState.setAgentPwd(reqeust_setAgentState
							.getAgentPwd());
					response_setAgentState.setAgentType(reqeust_setAgentState
							.getAgentType());
					response_setAgentState.setSkillGroup(reqeust_setAgentState
							.getSkillGroup());
					response_setAgentState.setClient_IP(reqeust_setAgentState
							.getClient_IP());
					response_setAgentState.setClient_Port(reqeust_setAgentState
							.getClient_Port());
					return response_setAgentState;
				} else if ("answerCall".equalsIgnoreCase(requestName)) {
					ResponseAnswerCallPDU response_answerCall = (ResponseAnswerCallPDU) response;
					RequestAnswerCallPDU request_answerCall = (RequestAnswerCallPDU) requestPDU;
					response_answerCall.setDeviceID(request_answerCall
							.getDeviceID());
					response_answerCall.setCallID(request_answerCall
							.getCallID());
					response_answerCall.setUcid(request_answerCall.getUcid());
					return response_answerCall;
				} else if ("makeCall".equalsIgnoreCase(requestName)) {
					ResponseMakeCallPDU response_makeCall = (ResponseMakeCallPDU) response;
					RequestMakeCallPDU request_makeCall = (RequestMakeCallPDU) requestPDU;
					response_makeCall.setDeviceID(request_makeCall
							.getDeviceID());
					return response_makeCall;
				} else if ("holdCall".equalsIgnoreCase(requestName)) {
					ResponseHoldCallPDU response_holdCall = (ResponseHoldCallPDU) response;
					RequestHoldCallPDU request_holdCall = (RequestHoldCallPDU) requestPDU;
					response_holdCall.setDeviceID(request_holdCall
							.getDeviceID());
					response_holdCall.setCallID(request_holdCall.getCallID());
					response_holdCall.setUcid(request_holdCall.getUcid());
					return response_holdCall;
				} else if ("retrieveCall".equalsIgnoreCase(requestName)) {
					ResponseRetrieveCallPDU response_retrieveCall = (ResponseRetrieveCallPDU) response;
					RequestRetrieveCallPDU request_retrieveCall = (RequestRetrieveCallPDU) requestPDU;
					response_retrieveCall.setDeviceID(request_retrieveCall
							.getDeviceID());
					response_retrieveCall.setCallID(request_retrieveCall
							.getCallID());
					response_retrieveCall.setUcid(request_retrieveCall
							.getUcid());
					return response_retrieveCall;
				} else if ("consultationCall".equalsIgnoreCase(requestName)) {
					ResponseConsultationCallPDU response_consulation = (ResponseConsultationCallPDU) response;
					RequestConsultationCallPDU request_consulation = (RequestConsultationCallPDU) requestPDU;
					response_consulation.setDeviceID(request_consulation
							.getDeviceID());
					return response_consulation;
				} else if ("transferCall".equalsIgnoreCase(requestName)) { // ???
					ResponseTransferCallPDU response_transfer = (ResponseTransferCallPDU) response;
					RequestTransferCallPDU request_transfer = (RequestTransferCallPDU) requestPDU;
					response_transfer.setDeviceID(request_transfer
							.getDeviceID());
					return response_transfer;
				} else if ("conferenceCall".equalsIgnoreCase(requestName)) {
					ResponseConferenceCallPDU response_conference = (ResponseConferenceCallPDU) response;
					RequestConferenceCallPDU request_conference = (RequestConferenceCallPDU) requestPDU;
					response_conference.setDeviceID(request_conference
							.getDeviceID());
					return response_conference;
				} else if ("clearCall".equalsIgnoreCase(requestName)
						|| "clearConnection".equalsIgnoreCase(requestName)) {
					ResponseClearCallPDU response_clearCall = (ResponseClearCallPDU) response;
					RequestClearCallPDU request_clearCall = (RequestClearCallPDU) requestPDU;
					response_clearCall.setDeviceID(request_clearCall
							.getDeviceID());
					response_clearCall.setCallID(request_clearCall.getCallID());
					response_clearCall.setUcid(request_clearCall.getUcid());
					return response_clearCall;
				} else if ("alternateCall".equalsIgnoreCase(requestName)) {
					ResponseAlternateCallPDU response_alternate = (ResponseAlternateCallPDU) response;
					RequestAlternateCallPDU request_alternate = (RequestAlternateCallPDU) requestPDU;
					response_alternate.setDeviceID(request_alternate
							.getDeviceID());
					return response_alternate;
				} else if ("reconnectCall".equalsIgnoreCase(requestName)) {
					ResponseReconnectCallPDU response_reconnect = (ResponseReconnectCallPDU) response;
					RequestReconnectCallPDU request_reconnect = (RequestReconnectCallPDU) requestPDU;
					response_reconnect.setDeviceID(request_reconnect
							.getDeviceID());
					return response_reconnect;
				} else if ("singleStepConference".equalsIgnoreCase(requestName)) {
					// TODO
				} else if ("snapshotDevice".equalsIgnoreCase(requestName)) {
					ResponseQueryAgentStateV2PDU response_queryAgentState = (ResponseQueryAgentStateV2PDU) response;
					RequestQueryAgentStateV2PDU request_queryAgentState = (RequestQueryAgentStateV2PDU) requestPDU;
					if (r != null) {
						IDevice device = ((SnapshotDeviceResponse) r).device;
						SStation station = (SStation)device;
						SAgent sAgent = (SAgent) ((station)
								.getAgent());
						response_queryAgentState
								.setSessionID(request_queryAgentState
										.getSessionID());
						response_queryAgentState
								.setMessageID(request_queryAgentState
										.getMessageID());
						response_queryAgentState
								.setResponseName("Response_QueryAgentStateV2");
						response_queryAgentState
								.setMessageTimeStamp(new Date());
						response_queryAgentState
								.setRequestTime(request_queryAgentState
										.getMessageTimeStamp());
						response_queryAgentState
								.setClientID(request_queryAgentState
										.getClientID());
						response_queryAgentState.setDeviceID(device.getId());
						response_queryAgentState.setAgentID(sAgent.getId());
						Collection<IAgentQueue> groupList = sAgent.getQueues();
						String groups = "";
						if (groupList != null) {
							int groupListSize = groupList.size();
							StringBuffer group_sb = new StringBuffer("["
									+ groupListSize + "]");
							for (IAgentQueue group : groupList) {
								group_sb.append("[" + group.getQueueId() + "]");
							}

							groups = group_sb.toString();

						} else {
							groups = "[0]";
						}
						response_queryAgentState.setPauseCode("");
						sAgent.getQueues();
						response_queryAgentState.setGroupID(groups);
						String deviceID = device.getId();
						// util.trace(this,
						// "QueryAgentState agentDevice for loginedDevice : "+login_device);

						if (deviceID != null && deviceID.length() > 0) {
							response_queryAgentState.setLogin_Device(deviceID);
							String agentState = "1";
							if (AgentMode.Logout == sAgent.getMode()) {
								agentState = "1";
							} else if (AgentMode.NotReady == sAgent.getMode()) {
								agentState = "2";
								response_queryAgentState.setPauseCode(sAgent
										.getReason());
							} else if (AgentMode.Ready == sAgent.getMode()) {
								agentState = "3";
							} else if (AgentMode.WorkNotReady == sAgent
									.getMode()) {
								agentState = "4";
							} else {
								agentState = "1";
							}
							// "0" idle,"1",busy,"2",inbound,"3"
							// outbound,"4",callRing,"5",dailing
							//DeviceState dStatus = device.getDeviceState();
							String deviceStatus = "0";
							String loginedDevice = "";
							if ("1".equals(agentState)) {
								loginedDevice = "";
								deviceStatus = "0";
							} else {
								if (station != null) {
									loginedDevice = device.getId();
									// "0" idle, "1",busy.
									if (station.getCalls().size() == 0) {
										deviceStatus = "0";
										Util.trace(this, "Check device ["
												+ loginedDevice
												+ "] is idle and remove it.");
										protocolMgr.callInfoManager
												.removeDeviceID(loginedDevice);
									} else {

										switch (sAgent.getStatus()) {
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

									}
								} 
							}
							// util.trace(this,
							// "QueryAgentState deviceStatus : "+deviceStatus);
							response_queryAgentState
									.setDeviceState(deviceStatus);
							response_queryAgentState
									.setLogin_Device(loginedDevice);
							Date login_Date = sAgent.getLoginTime();
							// SimpleDateFormat sdf = new
							// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String agentLoginTime = "";
							if (login_Date != null) {
								agentLoginTime = new SimpleDateFormat(
										"HH:mm:ss").format(login_Date);
							}

							response_queryAgentState.setAgentState(agentState);

							response_queryAgentState.setRes1(agentLoginTime);
							response_queryAgentState.setRes2("");
							response_queryAgentState.setAgentType(""); // Agent
																		// "0"
							return response_queryAgentState;
						}
					} else {
						ResponseFailurePDU failure = UDPUtil
								.loadFailureResponsePDU(requestPDU);
						failure.setFailCode(UDPConstants.CTI_RES_FAILURE);
						return failure;
					}
				} else if ("queryAgentLogin".equalsIgnoreCase(requestName)) {

					ResponseQueryGroupAgentListV2PDU response_queryGroupAgentList = (ResponseQueryGroupAgentListV2PDU) response;
					// RequestQueryGroupAgentListV2 request_queryGroupAgentList
					// = (RequestQueryGroupAgentListV2)requestPDU;
					String agentIDList = "";

					if (r != null) {
						String[] agentList = (String[]) r;
						StringBuffer sb = new StringBuffer();
						for (String agentID : agentList) {
							sb.append(agentID + ",");
						}
						String deviceList = sb.toString();
						if (deviceList.length() > 0) {
							agentIDList = deviceList.substring(0,
									deviceList.length() - 1);
							response_queryGroupAgentList
									.setAgentList(agentIDList);
						} else {
							Util.trace(this,
									"Query loginedAgents response no agent login.");
							response_queryGroupAgentList.setAgentList("");
						}
						return response_queryGroupAgentList;
					} else {
						ResponseFailurePDU failure = UDPUtil
								.loadFailureResponsePDU(requestPDU);
						failure.setFailCode(UDPConstants.CTI_RES_FAILURE);
						return failure;
					}
				}
				return response;
			}
		}
		return null;
	}

	public EventPDU convertResponse2Event(ResponseSetAgentStatePDU responsePDU) {

		EventAgentStateChangedPDU agentStateChangedEvent = new EventAgentStateChangedPDU();

		String deviceID = responsePDU.getDeviceID();

		agentStateChangedEvent.setMessageID(UDPUtil.generateSequeueNO());
		agentStateChangedEvent.setEventName("event_AgentStateChanged");
		agentStateChangedEvent.setEventID(deviceID);
		agentStateChangedEvent.setSessionID(responsePDU.getSessionID());
		agentStateChangedEvent.setAgentID(responsePDU.getAgentID());
		agentStateChangedEvent.setAgentState(responsePDU.getAgentState());
		agentStateChangedEvent.setAgentDevice(deviceID);
		agentStateChangedEvent.setAgentName(responsePDU.getAgentName());
		agentStateChangedEvent.setAgentPwd(responsePDU.getAgentPwd());
		agentStateChangedEvent.setAgentType(responsePDU.getAgentType());
		agentStateChangedEvent.setSkillGroup(responsePDU.getSkillGroup());
		agentStateChangedEvent.setClient_IP(responsePDU.getClient_IP());
		agentStateChangedEvent.setClient_Port(responsePDU.getClient_Port());
		agentStateChangedEvent.setMessageTimeStamp(new Date());

		// util.trace(this,
		// "Convert AgentStateChangedResponse to AgentStateChangedEvent ok . ");

		return agentStateChangedEvent;

	}

	public EventPDU packagEventPDU(Event e) throws Throwable {

		String eventname = e.getEventName();
		// util.trace(this,"Event name : "+eventname);

		if ("serviceInitiated".equalsIgnoreCase(eventname)) {
			EventServiceInitiatedPDU serviceInitEvent = new EventServiceInitiatedPDU();
			ServiceInitiatedEvent event = (ServiceInitiatedEvent) e;
			serviceInitEvent.setMessageID(UDPUtil.generateSequeueNO());
			serviceInitEvent.setEventName("event_ServiceInitiated");
			serviceInitEvent.setSessionID((String) event
					.getProperty("_sessionId"));
			serviceInitEvent.setCallID(event.callId);
			serviceInitEvent.setUcid(event.callId);
			serviceInitEvent.setDeviceID(event.srcDeviceId);
			serviceInitEvent.setEventID(event.srcDeviceId);
			serviceInitEvent.setMessageTimeStamp(new Date());
			return serviceInitEvent;

		} else if ("originated".equalsIgnoreCase(eventname)) {
			EventOriginatedPDU orginatedEvent = new EventOriginatedPDU();
			OriginatedEvent event = (OriginatedEvent) e;
			orginatedEvent.setMessageID(UDPUtil.generateSequeueNO());
			orginatedEvent.setEventName("event_Orginated");
			orginatedEvent.setSessionID((String) event
					.getProperty("_sessionId"));
			orginatedEvent.setCallID(event.callId);
			orginatedEvent.setEventID(event.srcDeviceId);
			orginatedEvent.setDeviceID(event.srcDeviceId);
			orginatedEvent.setCalledNO(event.calledDevice);
			orginatedEvent.setCallingNO(event.callingDevice);
			orginatedEvent.setMessageTimeStamp(new Date());
			return orginatedEvent;

		} else if ("delivered".equalsIgnoreCase(eventname)) {
			DeliveredEvent event = (DeliveredEvent) e;
			String deviceID = event.srcDeviceId;
			Util.trace(this, "Delivered Event eventID=" + deviceID
					+ ", callingDevice=" + event.callingDevice + "callID"
					+ event.callId);
			if (!deviceID.equals(event.alertingDevice)) {
				// out bound alerted event.
				EventAlertedPDU alertedEvent = new EventAlertedPDU();
				alertedEvent.setMessageID(UDPUtil.generateSequeueNO());
				alertedEvent.setEventName("event_Alerted");
				alertedEvent.setSessionID((String) event
						.getProperty("_sessionId"));
				alertedEvent.setCallID(event.callId);
				alertedEvent.setDeviceID(event.srcDeviceId);
				alertedEvent.setUcid(event.callId);
				alertedEvent.setEventID(deviceID);
				alertedEvent.setANI(event.callingDevice);
				alertedEvent.setDNIS(event.calledDevice);
				alertedEvent.setMessageTimeStamp(new Date());
				return alertedEvent;

			} else {
				// in bound delivered event.
				EventDeliveredV2PDU deliveredv2Event = new EventDeliveredV2PDU();
				deliveredv2Event.setMessageID(UDPUtil.generateSequeueNO());
				deliveredv2Event.setEventName("event_DeliveredV2");
				deliveredv2Event.setSessionID((String) e
						.getProperty("_sessionId"));
				deliveredv2Event.setCallID(event.callId);
				deliveredv2Event.setDeviceID(event.srcDeviceId);
				deliveredv2Event.setEventID(deviceID);
				deliveredv2Event.setUcid(event.callId);
				deliveredv2Event.setANI(event.callingDevice);
				deliveredv2Event.setDNIS(event.calledDevice);
				deliveredv2Event.setCallType("1");
				deliveredv2Event
						.setTrunkGroup(event.trunkGroup != null ? event.trunkGroup
								: "");
				deliveredv2Event
						.setTrunkMember(event.trunkMember != null ? event.trunkMember
								: "");
				UserData userData = event.userData;
				if (userData != null) {
					String uui = (String) userData.getProperty("uui");
					if (uui != null && uui.toString().length() > 0) {
						// uui = uui.substring(1);
						// uui = uui.substring(2, uui.length()-1);
						Util.trace(this, "DeliveredV2 uui : " + uui);
						deliveredv2Event.setUui(uui.toString());
					} else {
						deliveredv2Event.setUui("");
					}
				}
				deliveredv2Event.setSplit(event.split != null ? event.split
						: "");
				deliveredv2Event
						.setVdn(event.lastRedirectionDevice != null ? event.lastRedirectionDevice
								: "");

				// deliveredEvent.setUui((String)e.getProperty("uui"));
				deliveredv2Event.setMessageTimeStamp(new Date());
				return deliveredv2Event;

			}
		} else if ("established".equalsIgnoreCase(eventname)) {
			EstablishedEvent event = (EstablishedEvent) e;
			Util.trace(this, "established Event eventID=" + event.srcDeviceId
					+ ", callingDevice=" + event.callingDevice + "callID"
					+ event.callId);
			EventEstablishedPDU establishedEvent = new EventEstablishedPDU();
			establishedEvent.setMessageID(UDPUtil.generateSequeueNO());
			establishedEvent.setEventName("event_Established");
			establishedEvent.setSessionID((String) e.getProperty("_sessionId"));
			establishedEvent.setCallID(event.callId);
			establishedEvent.setEventID(event.srcDeviceId);
			establishedEvent.setDeviceID(event.srcDeviceId);
			establishedEvent.setUcid(event.callId);
			establishedEvent
					.setTrunkGroup(event.trunkGroup != null ? event.trunkGroup
							: "");
			establishedEvent
					.setTrunkMem(event.trunkMember != null ? event.trunkMember
							: "");
			establishedEvent.setCallingDevice(event.callingDevice);
			establishedEvent.setCalledDevice(event.calledDevice);

			establishedEvent.setMessageTimeStamp(new Date());
			return establishedEvent;

		} else if ("connectionCleared".equalsIgnoreCase(eventname)) {
			ConnectionClearedEvent event = (ConnectionClearedEvent) e;
			String releaseDevice = event.releasingDevice;
			String eventID = event.srcDeviceId;

			Util.trace(this, "ConnectionCleared releaseDevice:" + releaseDevice
					+ ", srcDevice:" + event.srcDeviceId + ", callID"
					+ event.callId);

			String srcDevice = event.srcDeviceId;

			if (releaseDevice != null && releaseDevice.equals(srcDevice)) {

				EventCallClearedPDU callClearedEvent = new EventCallClearedPDU();

				callClearedEvent.setMessageID(UDPUtil.generateSequeueNO());
				callClearedEvent.setEventName("event_CallCleared");
				callClearedEvent.setSessionID((String) e
						.getProperty("_sessionId"));
				callClearedEvent.setEventID(eventID);
				callClearedEvent.setCallID(event.callId);
				callClearedEvent.setDeviceID(srcDevice);
				callClearedEvent.setReleaseDevice(releaseDevice);
				callClearedEvent.setMessageTimeStamp(new Date());

				return callClearedEvent;

			} else {

				EventConnectionClearedPDU connClearedEvent = new EventConnectionClearedPDU();

				connClearedEvent.setDeviceID(srcDevice);

				connClearedEvent.setMessageID(UDPUtil.generateSequeueNO());
				connClearedEvent.setEventName("event_ConnCleared");
				connClearedEvent.setSessionID((String) e
						.getProperty("_sessionId"));
				connClearedEvent.setEventID(eventID);
				connClearedEvent.setCallID(event.callId);
				connClearedEvent.setReleaseDevice(releaseDevice);

				connClearedEvent.setMessageTimeStamp(new Date());
				return connClearedEvent;

			}

		} else if ("callCleared".equalsIgnoreCase(eventname)) {
			CallClearedEvent event = (CallClearedEvent) e;
			Util.trace(this, "CallCleared Event eventID=" + event.srcDeviceId
					+ "callID" + event.callId);
			EventCallClearedPDU callClearedEvent = new EventCallClearedPDU();
			String eventID = event.srcDeviceId;
			callClearedEvent.setMessageID(UDPUtil.generateSequeueNO());
			callClearedEvent.setEventName("event_CallCleared");
			callClearedEvent.setSessionID((String) e.getProperty("_sessionId"));
			callClearedEvent.setEventID(eventID);
			callClearedEvent.setCallID(event.callId);
			callClearedEvent.setDeviceID(event.srcDeviceId);
			callClearedEvent.setMessageTimeStamp(new Date());
			return callClearedEvent;

		}
		// else if ("system".equalsIgnoreCase(eventname)) {
		// SystemEvent event = (SystemEvent) e;
		// EventFailurePDU failureEvent = new EventFailurePDU();
		// failureEvent.setMessageID(UDPUtil.generateSequeueNO());
		// failureEvent.setEventName("event_Failure");
		// failureEvent.setSessionID((String) e.getProperty("_sessionId"));
		// failureEvent.setCallID(event.srcDeviceId);
		// failureEvent.setDeviceID(event.srcDeviceId);
		// failureEvent.setFailCode(event.reason.toString());
		// failureEvent.setMessageTimeStamp(new Date());
		// return failureEvent;
		//
		// }
		else if ("held".equalsIgnoreCase(eventname)) {
			HeldEvent event = (HeldEvent) e;
			EventHeldPDU heldEvent = new EventHeldPDU();
			heldEvent.setMessageID(UDPUtil.generateSequeueNO());
			heldEvent.setEventName("event_Held");
			heldEvent.setSessionID((String) e.getProperty("_sessionId"));
			heldEvent.setCallID(event.callId);
			heldEvent.setDeviceID(event.srcDeviceId);
			heldEvent.setEventID(event.srcDeviceId);
			// SStation station = (SStation) (event.getEventSrc());
			// heldEvent.setCalledDevice("6815");
			// heldEvent.setCallingDevice("6832");
			heldEvent.setMessageTimeStamp(new Date());
			return heldEvent;

		} else if ("retrieved".equalsIgnoreCase(eventname)) {
			RetrievedEvent event = (RetrievedEvent) e;
			EventRetrievedPDU retrievedEvent = new EventRetrievedPDU();
			retrievedEvent.setMessageID(UDPUtil.generateSequeueNO());
			retrievedEvent.setEventName("event_Retrieved");
			retrievedEvent.setSessionID((String) e.getProperty("_sessionId"));
			retrievedEvent.setCallID(event.callId);
			retrievedEvent.setEventID(event.srcDeviceId);
			retrievedEvent.setDeviceID(event.srcDeviceId);
			retrievedEvent.setMessageTimeStamp(new Date());
			return retrievedEvent;

		} else if ("transferred".equalsIgnoreCase(eventname)) {
			// util.trace(this,
			// "Transfered call event .... "+(String)e.getProperty("device:device"));
			TransferredEvent event = (TransferredEvent) e;
			EventTransferedPDU transferedEvent = new EventTransferedPDU();
			transferedEvent.setMessageID(UDPUtil.generateSequeueNO());
			String deviceID = event.srcDeviceId;
			transferedEvent.setEventName("event_Transfered");
			transferedEvent.setSessionID((String) e.getProperty("_sessionId"));
			// if (deviceID.equals(event.transferringDevice))
			// transferedEvent.setCallID(event.primaryOldCall);
			// else
			transferedEvent.setCallID(event.callId);
			transferedEvent.setEventID(deviceID);
			transferedEvent.setUcid(event.callId);
			transferedEvent.setDeviceID(deviceID);
			transferedEvent.setCallingDevice(event.transferringDevice);
			transferedEvent.setOldPrimaryCallID(event.primaryOldCall);
			transferedEvent.setOldSecondaryCallID(event.secondaryOldCall);
			transferedEvent.setTransferingDevice(event.transferringDevice);
			transferedEvent.setTransferedDevice(event.transferredToDevice);

			return transferedEvent;

		} else if ("conferenced".equalsIgnoreCase(eventname)) {
			ConferencedEvent event = (ConferencedEvent) e;
			EventConferencedPDU conferencedEvent = new EventConferencedPDU();
			String deviceID = event.srcDeviceId;
			conferencedEvent.setMessageID(UDPUtil.generateSequeueNO());
			conferencedEvent.setEventName("event_Conferenced");
			conferencedEvent.setSessionID((String) e.getProperty("_sessionId"));
			// if(deviceID.equals(event.conferencingDevice))
			// conferencedEvent.setCallID(event.primaryOldCall);
			// else
			conferencedEvent.setUcid(event.callId);
			conferencedEvent.setCallID(event.secondaryOldCall);
			conferencedEvent.setEventID(deviceID);
			conferencedEvent.setOldPrimaryCallID(event.primaryOldCall);
			conferencedEvent.setOldSecondaryCallID(event.secondaryOldCall);
			conferencedEvent.setAddParty(event.addedParty);
			conferencedEvent.setConfController(event.conferencingDevice);
			conferencedEvent.setDeviceID(deviceID);
			return conferencedEvent;

		} else if ("diverted".equalsIgnoreCase(eventname)) {
			DivertedEvent event = (DivertedEvent) e;
			EventDivertedPDU divertedEvent = new EventDivertedPDU();
			divertedEvent.setMessageID(UDPUtil.generateSequeueNO());
			divertedEvent.setEventName("event_Diverted");
			divertedEvent.setEventID(event.srcDeviceId);
			divertedEvent
					.setSessionID((String) event.getProperty("_sessionId"));
			divertedEvent.setCallID(event.callId);
			divertedEvent.setDeviceID(event.srcDeviceId);
			return divertedEvent;

		} else if ("queued".equalsIgnoreCase(eventname)) {
			QueuedEvent event = (QueuedEvent) e;
			EventQueuedPDU queuedEvent = new EventQueuedPDU();
			queuedEvent.setMessageID(UDPUtil.generateSequeueNO());
			queuedEvent.setEventName("event_Queued");
			queuedEvent.setCallID(event.callId);
			queuedEvent.setSessionID((String) event.getProperty("_sessionId"));
			queuedEvent.setDeviceID(event.srcDeviceId);
			queuedEvent.setEventID(event.srcDeviceId);
			queuedEvent.setCallingDeviceID(event.callingDeviceId);
			queuedEvent.setCalledDeviceID(event.calledDeviceId);
			queuedEvent.setLastRedirectDeviceID(event.lastRedirectDeviceId);
			queuedEvent.setNumberQueued(event.numberQueued + "");
			// queuedEvent.setTimeStamp(PDUUtil.getTimeStamp());
			return queuedEvent;
		} else if ("agentLoggedOn".equalsIgnoreCase(eventname)) {
			EventAgentLoginPDU loginEvent = new EventAgentLoginPDU();
			AgentLoggedOnEvent event = (AgentLoggedOnEvent) e;
			loginEvent.setMessageID(UDPUtil.generateSequeueNO());
			loginEvent.setEventName("event_AgentLogin");
			loginEvent.setSessionID((String) event.getProperty("_sessionId"));
			loginEvent.setAgentDevice(event.srcDeviceId);
			loginEvent.setAgentID(event.agentId);
			loginEvent.setGroupID(event.queueId);
			return loginEvent;

		} else if ("agentLoggedOff".equalsIgnoreCase(eventname)) {
			EventAgentLogoutPDU logoutEvent = new EventAgentLogoutPDU();
			AgentLoggedOffEvent event = (AgentLoggedOffEvent) e;
			logoutEvent.setMessageID(UDPUtil.generateSequeueNO());
			logoutEvent.setEventName("event_AgentLogout");
			logoutEvent.setSessionID((String) e.getProperty("_sessionId"));
			logoutEvent.setAgentID(event.agentId);
			logoutEvent.setAgentDevice(event.srcDeviceId);
			logoutEvent.setGroupID((String) e.getProperty("queueId"));
			return logoutEvent;

		} else if ("agentNotReady".equalsIgnoreCase(eventname)) {
			EventAgentNotReadyPDU agentNotReadyEvent = new EventAgentNotReadyPDU();
			AgentNotReadyEvent event = (AgentNotReadyEvent) e;
			agentNotReadyEvent.setMessageID(UDPUtil.generateSequeueNO());
			agentNotReadyEvent.setEventName("event_AgentNotReady");
			agentNotReadyEvent.setSessionID((String) event
					.getProperty("_sessionId"));
			agentNotReadyEvent.setAgentID(event.agentId);
			agentNotReadyEvent.setAgentDevice(event.srcDeviceId);
			String pauseCode = event.reason;
			Util.trace(
					this,
					"AgentNotReadyEvent agentId="
							+ (String) e.getProperty("agentId") + ",pauseCode="
							+ pauseCode);
			if (pauseCode != null && pauseCode.length() > 0) {
				agentNotReadyEvent.setPauseCode(pauseCode);
			} else {
				agentNotReadyEvent.setPauseCode("0");
			}
			return agentNotReadyEvent;

		} else if ("agentReady".equalsIgnoreCase(eventname)) {
			EventAgentReadyPDU agentReadyEvent = new EventAgentReadyPDU();
			AgentReadyEvent event = (AgentReadyEvent) e;
			agentReadyEvent.setMessageID(UDPUtil.generateSequeueNO());
			agentReadyEvent.setEventName("event_AgentReady");
			agentReadyEvent.setSessionID((String) event
					.getProperty("_sessionId"));
			agentReadyEvent.setEventID(event.srcDeviceId);
			agentReadyEvent.setAgentID(event.agentId);
			agentReadyEvent.setAgentDevice(event.srcDeviceId);
			return agentReadyEvent;

		} else if ("agentWorkingAfterCall".equalsIgnoreCase(eventname)) {
			AgentWorkingAfterCallEvent event = (AgentWorkingAfterCallEvent) e;
			EventAgentWorkNotReadyPDU workNotReadyEvent = new EventAgentWorkNotReadyPDU();
			workNotReadyEvent.setMessageID(UDPUtil.generateSequeueNO());
			workNotReadyEvent.setEventName("event_AgentWorkNotReady");
			workNotReadyEvent.setSessionID((String) event
					.getProperty("_sessionId"));
			workNotReadyEvent.setAgentID(event.agentId);
			workNotReadyEvent.setEventID(event.srcDeviceId);
			workNotReadyEvent.setAgentDevice(event.srcDeviceId);
			return workNotReadyEvent;
		} else if ("agentBusy".equalsIgnoreCase(eventname)) {
			// AgentBusyEvent event = (AgentBusyEvent)e;
			return null;
		} else {
			Util.trace(this, "Unknow event message,will discard it . "
					+ eventname);
			return null;
		}
	}

}
