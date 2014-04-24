package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.util.Util;

public class ResponseFailurePDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String failDesc;

	private int failCode;

	public String getFailDesc() {
		return failDesc;
	}

	public void setFailDesc(String failDesc) {
		this.failDesc = failDesc;
	}

	public int getFailCode() {
		return failCode;
	}

	public void setFailCode(int failCode) {
		this.failCode = failCode;
	}

	@Override
	public void responseToClient(UDPHandler handler, String clientID) {

		Util.trace(this, "Response Failure ... " + this.messageID);

		if (clientID != null) {
			handler.write(this, clientID);
			Util.trace(this, "Failure response ok " + clientID);
		} else {
			Util.warn(this, "Failure clientSession is null. ");
		}
	}
}
