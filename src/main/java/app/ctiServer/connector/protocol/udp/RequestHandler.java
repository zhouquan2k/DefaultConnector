package app.ctiServer.connector.protocol.udp;

import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.RequestSetAgentStatePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import component.util.Util;

public class RequestHandler implements Runnable {

	private RequestPDU requestPDU;

	private UDPHandler udpHandler;

	public RequestHandler(RequestPDU requestPDU, UDPHandler udpHandler) {

		this.requestPDU = requestPDU;
		this.udpHandler = udpHandler;
	}

	public void run() {

		if (requestPDU != null) {

			if (udpHandler.protocolMgr.getRequestMaps().size() > 200) {
				Util.warn(this, "Request pending queue size : "
						+ udpHandler.protocolMgr.getRequestMaps().size());
			}

			try {

				UDPContext context = udpHandler.getProtocolMgr()
						.getUDPConextByClientID(requestPDU.getClientID());
				if (context != null) {
					if (requestPDU instanceof RequestSetAgentStatePDU) {
						RequestSetAgentStatePDU agentRequest = (RequestSetAgentStatePDU) requestPDU;
						agentRequest.setAgentWorkMode(udpHandler
								.getProtocolMgr().getAgentWorkMode());
						requestPDU = agentRequest;
					}
					requestPDU.invokeAPI(udpHandler);
				} else {
					Util.warn(this, "Invalid session for client : "
							+ requestPDU.getClientID());
				}

			} catch (Throwable e) {
				Util.error(this, e, "");
				ResponseFailurePDU failure = UDPUtil
						.loadFailureResponsePDU(requestPDU);
				failure.setFailCode(UDPConstants.REQ_CTI_FAILURE);
				udpHandler.write(failure, requestPDU.getClientID());
			}

		}
	}

}
