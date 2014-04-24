package app.ctiServer.connector.protocol.udp.pdu;

import java.io.Serializable;
import java.util.Date;

public abstract class PDU implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String messageID = null;
	
	protected Date messageTimeStamp = null;
	
	public abstract void setMessageTimeStamp(Date messageTimeStamp);
		

}
