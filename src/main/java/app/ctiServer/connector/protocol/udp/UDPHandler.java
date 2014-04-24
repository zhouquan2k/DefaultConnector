package app.ctiServer.connector.protocol.udp;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.pdu.RequestHeartBeatPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestUnknowPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseQueryGroupInfoV2PDU;
import component.resource.Dictionary;
import component.util.Util;

public class UDPHandler extends SimpleChannelUpstreamHandler {

	UDPProtocolMgr protocolMgr;

	private CleanCallInfoHandler cleanCallInfoThread = null;

	private ExecutorService requestExecutor = component.util.ThreadPoolExecutor
			.newThreadPoolExecutor("requestMap-UDPRequesthandler", 100, 200,
					2000, 1000);
	private ExecutorService heartBeatExecutor = component.util.ThreadPoolExecutor
			.newThreadPoolExecutor("heartBeat-UDPHeartBeatHandler", 100, 200,
					2000, 1000);

	private List<Dictionary> pauseReasonList;

	public UDPHandler() {

	}

	public UDPHandler(UDPProtocolMgr protocolMgr) {
		this.protocolMgr = protocolMgr;
		Timer clientSessionTimer = new Timer(true);
		clientSessionTimer.schedule(new ClientSessionTimer(this), 5000, 3*1000);
		requestExecutor.execute(new CleanRequestMapHandler(protocolMgr
				.getRequestMaps()));
		pauseReasonList = new ArrayList<Dictionary>();
		Timer timer = new Timer(true);
		final Request request = new Request();
		request.object = "res";
		request.method = "queryDictionary";

		request.params = new String[] { "REASON_CODE" };

		timer.schedule(new TimerTask() {

			Object result = null;

			@SuppressWarnings("unchecked")
			@Override
			public void run() {

				try {
					result = processRequest(request, null);
					if (result instanceof List) {
						pauseReasonList = (List<Dictionary>) result;
					}

				} catch (Throwable e) {
					Util.error(this, e, "");
				}

			}
		}, 60000, 86400000);
		cleanCallInfoThread = new CleanCallInfoHandler(getCallInfoManager());
		cleanCallInfoThread.start();
	}

	public UDPProtocolMgr getProtocolMgr() {
		return protocolMgr;
	}

	public void init() {

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object pdu = e.getMessage();
		InetSocketAddress remoteAddr = (InetSocketAddress) e.getRemoteAddress();
		Inet4Address ipAddr = (Inet4Address) remoteAddr.getAddress();
		String clientID = ipAddr.getHostAddress() + ":" + remoteAddr.getPort();
		UDPContext context = null;
		try {
			context = protocolMgr.getUDPConextByClientID(clientID);
			if (context == null)
				context = protocolMgr.addSession(
						(DatagramChannel) e.getChannel(), clientID, remoteAddr);
			if (context == null)
				return;
		} catch (Throwable e1) {
			Util.trace(this, e1.getMessage(), "");
		}
		if (pdu != null) {
			if (pdu instanceof RequestHeartBeatPDU) {
				RequestHeartBeatPDU heartBeatPDU = (RequestHeartBeatPDU) pdu;
				heartBeatPDU.setClientID(clientID);
				heartBeatPDU.setSessionID(context.sessionID);
				Util.trace(this,
						"Push heartBeat PDU to heartBeatPool ok for clientID="
								+ context.sessionID);
                context.aliveValue = 20;
				heartBeatExecutor.execute(new HeartBeatHandler(heartBeatPDU,
						this));

			} else if (pdu instanceof RequestPDU) {
				RequestPDU requestPDU = (RequestPDU) pdu;
				requestPDU.setClientID(clientID);
				requestPDU.setSessionID(context.sessionID);
				if (!(pdu instanceof RequestUnknowPDU))
					protocolMgr.getRequestMaps().put(
							clientID + "_" + requestPDU.getMessageID(),
							requestPDU);
				Util.trace(this,
						"Receieve request[" + requestPDU.getRequestName()
								+ "] from clientId=" + requestPDU.getClientID()
								+ "; sessionId=" + requestPDU.getSessionID());
				protocolMgr.executor.execute(new RequestHandler(requestPDU,
						this));
			}

		} else {
			Util.trace(this, "UDPHandler receive message is null.");
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Util.warn(this, "!!!!!!!!!!! exception occur ... ", e);
	}

	public Object processRequest(Request request, String clientID)
			throws Throwable {
		UDPContext context = null;
		if (clientID != null)
			context = protocolMgr.getUDPConextByClientID(clientID);
		return protocolMgr.processRequest(request, context);

	}

	public void write(ResponsePDU responsePDU, String clientID) {
		UDPContext context = protocolMgr
				.getUDPConextByClientID(clientID);
		if (context != null && responsePDU != null) {
			DatagramChannel channel = context.channel;
			if (channel != null && channel.isWritable())
				channel.write(responsePDU, context.remotAddress);
		} else {
			Util.trace(this, "session is null", "");
		}
	}

	public List<Dictionary> getPauseReasonList() {
		return pauseReasonList;
	}

	public void setPauseReasonList(List<Dictionary> pauseReasonList) {
		this.pauseReasonList = pauseReasonList;
	}

	public ResponseQueryGroupInfoV2PDU getGroupInfoV2PDU(String groupID)
			throws Throwable {

		if (protocolMgr.getGroupInfoV2PDUs() == null
				|| protocolMgr.getGroupInfoV2PDUs().size() == 0) {
			this.protocolMgr.queryGroupInfos();

		}
		return this.protocolMgr.getGroupInfoV2PDUs().get(groupID);
	}

	public CallInfoManager getCallInfoManager() {
		return protocolMgr.callInfoManager;
	}
	
	public void writeRspFailed(RequestPDU requestPDU,Throwable e){
		ResponseFailurePDU failure = UDPUtil
				.loadFailureResponsePDU(requestPDU);
		String message = ((Throwable) e).getMessage();
		if(message.contains("Connection refused: connect")){
			protocolMgr.cti_available = false;
			return;
		}
        if(message.contains("invalid session") || message.contains("session invalid")){
        	protocolMgr.getAllUDPContext().remove(requestPDU.getClientID());
        	protocolMgr.getSessionID_ClientID().remove(requestPDU.getSessionID());
        	return;
        }
		failure.setFailCode(UDPConstants.CSTAUniversalFailure_t
				.parse(message));
		this.write(failure, requestPDU.getClientID());
	}
}
