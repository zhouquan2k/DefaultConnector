package app.ctiServer.connector.protocol.udp;

import java.util.Date;

import app.ctiServer.connector.protocol.udp.pdu.RequestHeartBeatPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseHeartBeatPDU;
import component.util.Util;

/**
 * This thread only handle heartBeat request message,and check clientSession's lifecycle.
 * @author Dev.pyh
 *
 */
public class HeartBeatHandler extends Thread{
			
	private RequestHeartBeatPDU heartBeatPDU;
	
	private UDPHandler udpHandler;
	
	public HeartBeatHandler(RequestHeartBeatPDU heartBeatPDU,UDPHandler udpHandler){
		this.udpHandler = udpHandler;
		this.heartBeatPDU = heartBeatPDU;
	}
	
	@Override
	public void run() {
		if(udpHandler.getProtocolMgr().cti_available)
			try{
				if(heartBeatPDU != null){
					if(udpHandler != null){
						String messageID = heartBeatPDU.getMessageID();
						ResponseHeartBeatPDU heartBeatResponse = new ResponseHeartBeatPDU();
						heartBeatResponse.setDeviceID("");
						heartBeatResponse.setMessageID(messageID);
						heartBeatResponse.setResponseName(heartBeatPDU.getRequestName());
						heartBeatResponse.setVersionNO(UDPConstants.CTI_VERSION);
						heartBeatResponse.setState("");
						heartBeatResponse.setMessageTimeStamp(new Date());
						heartBeatResponse.setClientID(heartBeatPDU.getClientID());
						heartBeatResponse.setSessionID(heartBeatPDU.getSessionID());
						try {
							heartBeatPDU.invokeAPI(udpHandler);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						heartBeatResponse.responseToClient(udpHandler,heartBeatPDU.getClientID());
	
					}   else{
						Util.trace(this,"Unknow client,Maybe it has been closed,please check it. "+heartBeatPDU.getClientID());
					}
				}else{
					Util.warn(this,"HeartBeatPDU is null,discard it.");
				}
			}catch(Exception e){
				Util.error(this, e,"");
			}
			
	}

}
