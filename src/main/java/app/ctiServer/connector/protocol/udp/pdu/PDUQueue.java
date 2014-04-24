package app.ctiServer.connector.protocol.udp.pdu;

import java.util.LinkedList;

import component.util.Util;

/**
 * PDU object queue.
 * @author Dev.pyh
 *
 */
public class PDUQueue {
	
    // Logger logger = Logger.getLogger(PDUQueue.class);
		
	LinkedList<PDU> pdus = null;
	
	private String queueName;
	
	public PDUQueue(String queueName){
			
			this.queueName = queueName;
		
			pdus = new LinkedList<PDU>();
			Util.trace(this,"Init "+queueName+" PDU Queue .");
	}
	
	public void pushPDU(PDU pdu){
		
		synchronized(pdus){
			pdus.add(pdu);
			if(pdus.size()>20){
				Util.warn(this,queueName+" PDU queue too long "+pdus.size());
			}
			pdus.notifyAll();
		}
	}
	
	public PDU popPDU(){
		
		synchronized(pdus){
			while(pdus.size() <= 0){
				try {
					pdus.wait(10);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
			return pdus.remove();
		}
	}
	
	/**
	 * Query queue size . 
	 * @return
	 */
	public int getQueueSize(){
		
		return pdus.size();
		
	}

}
