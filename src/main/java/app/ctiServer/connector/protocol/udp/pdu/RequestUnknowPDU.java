package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;

public class RequestUnknowPDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {
		return;
	}

}
