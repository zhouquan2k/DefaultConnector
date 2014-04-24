package app.ctiServer.connector.protocol.udp.pdu;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.CallInfoManager;

/**
 * Event abstract object.
 * @author Dev.pyh
 *
 */
public abstract class EventPDU extends PDU {

	private static final long serialVersionUID = 1L;
	
	public static final String EXCE_UCID = "#*";
	
	private String eventName;
	
	private String sessionID;
	
	private String eventID;
	
	public String getEventID() {
		return eventID;
	}

	public void setEventID(String eventID) {
		this.eventID = eventID;
	}

	public String getMessageID(){
		return this.messageID;
	}
	
	public void setMessageID(String messageID){
		this.messageID = messageID;
	}
	
	public void setMessageTimeStamp(Date messageTimeStamp){
		this.messageTimeStamp = messageTimeStamp;
	}
	
	public Date getMessageTimeStamp(){
		return this.messageTimeStamp;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	public abstract EventPDU sendEventToClient(CallInfoManager callInfoManager);

}
