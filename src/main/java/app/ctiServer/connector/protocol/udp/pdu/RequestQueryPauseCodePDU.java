package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;
import java.util.List;

import app.ctiServer.connector.Request;
import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.resource.Dictionary;
import component.util.Util;

public class RequestQueryPauseCodePDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String resValue;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getResValue() {
		return resValue;
	}

	public void setResValue(String resValue) {
		this.resValue = resValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"QueryPauseCode request ... sessionId=" + this.getSessionID()
						+ ",clientId=" + this.getClientID());

		ResponseQueryPauseCodePDU response_pausecode = new ResponseQueryPauseCodePDU();
		List<Dictionary> reasonCodeList = udpHandler.getPauseReasonList();
		if (reasonCodeList == null || reasonCodeList.size() == 0) {
			Util.trace(this, "Init reasonCodeList is null . " + reasonCodeList);
			Request request = new Request();
			request.object = "res";
			request.method = "queryDictionary";
			request.params = new String[] { "REASON_CODE" };
			Object result = udpHandler.processRequest(request, null);

			if (result != null && result instanceof List)
				reasonCodeList = (List<Dictionary>) result;
		}

		int reasonCodeListLen = 0;
		StringBuffer sb = new StringBuffer("");
		if (reasonCodeList != null) {
			reasonCodeListLen = reasonCodeList.size();
			if (reasonCodeListLen > 0) {
				sb.append("[" + reasonCodeListLen + "]");
				for (Dictionary dict : reasonCodeList) {
					sb.append("[" + (dict.getValue()) + "]["
							+ new String(dict.getName().getBytes("GBK")) + "]");
				}
			}
		}
		Util.trace(this, "Response reasonCode : " + sb.toString());
		response_pausecode.setPauseCode(sb.toString());
		response_pausecode.setMessageID(messageID);
		response_pausecode.setDeviceID(this.getDeviceID());
		response_pausecode.setResValue(this.getResValue());
		response_pausecode.setMessageTimeStamp(new Date());
		response_pausecode.setResponseName("Response_QueryPauseCode");
		response_pausecode.setClientID(this.getClientID());
		response_pausecode.setSessionID(this.getSessionID());
		// response_pausecode.trace();
		udpHandler.write(response_pausecode, this.getClientID());

	}
}
