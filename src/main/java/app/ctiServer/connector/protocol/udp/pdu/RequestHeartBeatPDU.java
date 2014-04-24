package app.ctiServer.connector.protocol.udp.pdu;

import component.util.Util;
import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPHandler;

public class RequestHeartBeatPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"HeartBeat Request ... sessionId=" + this.getSessionID()
						+ ",clientId=" + this.getClientID());

		Request request = new Request();
		request.method = "heartbeat";
		request.object = "cti";
		udpHandler.processRequest(request, this.getClientID());
	}
}
