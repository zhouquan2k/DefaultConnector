package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.UDPHandler;

/**
 * OCX Request abstract Object,All of request class must extends this.
 * 
 * @author Dev.pyh
 * 
 */
public abstract class RequestPDU extends PDU {

	private static final long serialVersionUID = 1L;

	private String sessionID;

	private String clientID;

	private String requestName;

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientID() {
		return this.clientID;
	}

	public String getSessionID() {
		return this.sessionID;
	}

	public void setMessageID(String messageID) {
		super.messageID = messageID;
	}

	public String getMessageID() {
		return super.messageID;
	}

	public void setMessageTimeStamp(Date messageTimeStamp) {
		super.messageTimeStamp = messageTimeStamp;
	}

	public Date getMessageTimeStamp() {
		return super.messageTimeStamp;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public abstract void invokeAPI(UDPHandler udpHandler) throws Throwable;

}
