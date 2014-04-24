package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;

import component.util.Util;

public class RequestQueryVDNListPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"QueryVDNList request ... sessionId=" + this.getSessionID()
						+ ",clientId=" + this.getClientID());

	}

}
