package app.ctiServer.connector.protocol.udp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import component.util.Util;

public class ClientSessionTimer extends TimerTask {

	private UDPHandler udpHandler;

	public ClientSessionTimer(UDPHandler udpHandler) {
		this.udpHandler = udpHandler;
	}

	@Override
	public void run() {

		try {

			Map<String, UDPContext> contextMap = udpHandler.protocolMgr
					.getAllUDPContext();
			Map<String, RequestPDU> requestMap = udpHandler.protocolMgr
					.getRequestMaps();
			UDPContext context = null;
			RequestPDU pdu = null;
			if (contextMap != null && contextMap.size() > 0) {
				List<String> delList = new ArrayList<String>();
				synchronized (contextMap) {
					Set<String> clientds = contextMap.keySet();

					for (String clientID : clientds) {
						context = contextMap.get(clientID);
						if (context == null || context == null
								|| context.aliveValue < 0) {

							delList.add(clientID);

						} else {
							context.aliveValue -= 1;
						}
					}

				}
				if (delList.size() > 0) {
					Set<String> sqNums = requestMap.keySet();
					Request request = new Request();
					request.method = "close";
					request.object = "cti";
					for (String clientID : delList) {
						Util.trace(this, "close session  %s",
								contextMap.get(clientID));
						udpHandler.processRequest(request, clientID);
						synchronized (contextMap) {
							context = contextMap.get(clientID);
							if (context != null && context.sessionID != null) {
								udpHandler.getProtocolMgr().getSessionID_ClientID()
								           .remove(context.sessionID);
								udpHandler.getProtocolMgr()
										.getGroupInfoV2PDUs()
										.remove(context.sessionID);
								udpHandler.getProtocolMgr().getAllUDPContext().remove(clientID);
								Util.trace(this, "UDPContext is removed", "");
								for (String sqNo : sqNums) {
									pdu = requestMap.get(sqNo);
									if (clientID.equals(pdu.getClientID()))
										requestMap.remove(sqNo);
								}
							}
						}

					}
					sqNums.clear();
				}
				delList.clear();
			}

		} catch (Throwable e) {
			Util.error(this, e, "");
		}

	}

}
