package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.UDPHandler;

/**
 * Response abstract object.
 * 
 * @author Dev.pyh
 * 
 */
public abstract class ResponsePDU extends PDU {

	private static final long serialVersionUID = 1L;

	private String sessionID;

	private String responseName;

	private String clientID;

	private Date requestTime;

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getMessageID() {
		return this.messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public Date getMessageTimeStamp() {
		return this.messageTimeStamp;
	}

	public void setMessageTimeStamp(Date messageTimeStamp) {
		this.messageTimeStamp = messageTimeStamp;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getResponseName() {
		return responseName;
	}

	public void setResponseName(String responseName) {
		this.responseName = responseName;
	}

	public void responseToClient(UDPHandler udpHandler, String clientID) {
		udpHandler.write(this, clientID);

	};

}
