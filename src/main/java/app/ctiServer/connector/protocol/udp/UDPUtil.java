package app.ctiServer.connector.protocol.udp;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import component.cti.server.data.SAgent;

import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponseFailurePDU;
import app.ctiServer.connector.protocol.udp.pdu.ResponsePDU;

public class UDPUtil {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private static final int UDP_BUFFER_SIZE = 2048;
	
	static int sequeueNO = 0;
	
	/**
	 * Generate event message sequeueNO.
	 * @return
	 */
    public static String generateSequeueNO(){
		
		String ret = "0000";
		
		if(sequeueNO >=9999){
			sequeueNO = 1;
			ret = "0001";
		}else if(sequeueNO >=999){
			sequeueNO = sequeueNO + 1;
			ret = ""+sequeueNO;
		}else if(sequeueNO >=99){
			sequeueNO = sequeueNO + 1;
			ret = "0"+sequeueNO;
		}else if(sequeueNO >=9){
			sequeueNO = sequeueNO + 1;
			ret = "00"+sequeueNO;
		}else{
			sequeueNO = sequeueNO + 1;
			ret = "000"+sequeueNO;
		}
		
		return ret;
	}
    
    public static boolean verfiyCalledNO(String calledNO){
    	if(calledNO.length()<=60){
    		Pattern pattern = Pattern.compile("[0-9*#]*");
    		Matcher matcher = pattern.matcher(calledNO);
    		return matcher.matches();
    	}else{
    		return false;
    	}
    }
    
    public static String convertAgentState(SAgent agent){
    	
    	String agentState = "1";
    	
    	if(agent.getMode().name().equalsIgnoreCase("Ready")){
    		agentState = "3";
    	}else if(agent.getMode().name().equalsIgnoreCase("NotReady")){
    		agentState = "2";
    	}else if(agent.getMode().name().equalsIgnoreCase("WorkNotReady")){
    		agentState = "4";
    	}else{
    		agentState = "1";
    	}
    	
    	return agentState;
    }

	/**
	 * Convert IoBuffer message to String list.
	 * 
	 * @param buffer
	 * @return
	 */
	public static ArrayList<String> convertBuffer2Strings(byte[] datas) {

		int len = datas.length;
		// logger.debug("Buffer realy length is " + len);
		final Charset charset=Charset.forName("US-ASCII");
		ArrayList<String> results = new ArrayList<String>();

		String messageID = new String(datas, 3, 4,charset);
		System.out.println(datas.toString());
       System.out.println(messageID+"____________");
  
		results.add(messageID);

		// logger.debug("Request MessageID ------ " + messageID);

		// UDP message body ipos is 9.
		int ipos = 9;

		for (int i = ipos; i < len; i++) {

			// logger.debug("In For Loop " + i + " " + datas[i]);

			if (datas[i] == UDPConstants.PKG_TAIL) {
				// End of buffer.
				String str = new String(datas, ipos, i - ipos);
				// logger.debug("Last data is " + str);
				results.add(str);
				break;
			} else if (datas[i] == UDPConstants.PKG_INTERVAL) {
				// Interval of buffer.

				String str = new String(datas, ipos, i - ipos);
				// logger.debug("Interval data is " + str);
				results.add(str);
				ipos = i + 1;

			} else {
				// buffer content,do nothing.
				// break;
			}
		}

		return results;
	}
	
	public static ResponsePDU loadResponsePDU(RequestPDU requestPDU) throws Throwable {
		
		ResponsePDU response = null;
		String requestName = requestPDU.getRequestName();
		String responseName = "app.ctiServer.connector.protocol.udp.pdu.Response"+requestName+"PDU";
		
		Class<?> resClass = Class.forName(responseName);
		Constructor<?> con =  resClass.getDeclaredConstructor();
		Object obj = con.newInstance();
		
		if(obj != null && obj instanceof ResponsePDU){
			response =  (ResponsePDU)obj;
			response.setClientID(requestPDU.getClientID());
			response.setMessageID(requestPDU.getMessageID());
			response.setSessionID(requestPDU.getSessionID());
			response.setResponseName(requestPDU.getRequestName());
			 response.setMessageTimeStamp(new Date());
		}else{
			throw new Exception("Load responsePDU failure for responseName : "+responseName);
		}
		
		return response;
		
	}
	
	public static ResponseFailurePDU loadFailureResponsePDU(RequestPDU requestPDU){
		
		ResponseFailurePDU failureResponse = new ResponseFailurePDU();
		failureResponse.setClientID(requestPDU.getClientID());
		failureResponse.setMessageID(requestPDU.getMessageID());
		failureResponse.setSessionID(requestPDU.getSessionID());
		
		return failureResponse;
		
	}
	
	
	/**
	 * convert responseData to UDP byte array.
	 * @param messageID
	 * @param datas
	 * @return
	 */
	public static byte[] convertResponseData2Byte(byte messageID, ArrayList<String> datas) {

		byte[] udps = new byte[UDP_BUFFER_SIZE];
		int tail_index = 0;
		// declare UDP package head.

		// 1, add package head,server,client;
		udps[0] = UDPConstants.PKG_HEAD;
		udps[1] = UDPConstants.TYPE_SERVER;
		udps[2] = UDPConstants.TYPE_CLIENT;

		// 2, add 4 bytes sequeueNO;
		String sequeueNO = datas.get(0);
		byte[] sequeues = sequeueNO.getBytes();
		// logger.debug("Response SequeueNO : "+sequeueNO+", sequeueArray size : "+sequeues.length);
		System.arraycopy(sequeues,0,udps,3,4);
		
		// 3, add 1 byte messageID;
		
		udps[7] = messageID;
		// 4, add 1 byte messageType,message head over.
		udps[8] = UDPConstants.MSG_RESPONSE;
		// Test udp message package head.
		
		// System.out.println(" package head over .... ");
		int ipos = 9;
		// System.out.println("DataSize --> "+datas.size());
		
		// 5, add message body.
		for(int index = 1;index<(datas.size()-1);index++){
			
			// System.out.println("data value : "+datas.get(index));
			
			String data = datas.get(index);
			if(data == null || data.length() == 0){
				data = "";
			}
			
			byte[] bytes = data.getBytes();
			
			int len = bytes.length;
			if(len >= 1){
				System.arraycopy(bytes,0,udps,ipos,len);
			}else{
				len = 0;
			}
			
			ipos = ipos+len;
			// System.out.println("Now ipos value : "+ipos);
			udps[ipos] = UDPConstants.PKG_INTERVAL; 
			ipos++;
		}
		
		if(datas.size()>1){
			// ipos = ipos + 1;
			String last_data = datas.get(datas.size()-1);
			if(last_data == null){
				last_data = "";
			}
			byte[] last_bytes = last_data.getBytes();
			// System.out.println("last data byte size : "+last_bytes.length);
			System.arraycopy(last_bytes, 0, udps, ipos, last_bytes.length);
			ipos = ipos + last_bytes.length;
			udps[ipos] = UDPConstants.PKG_TAIL;
			tail_index = ipos+1;
		}else{
			udps[ipos] = UDPConstants.PKG_TAIL;
			tail_index = ipos+1;
		}
		
		// System.out.println("Response udps array len "+tail_index);
		byte[] dests = new byte[tail_index];
		System.arraycopy(udps, 0, dests, 0,tail_index);
		
		return dests;
	}
	
	public static byte[] convertFailResponseData2Byte(byte messageID,ArrayList<String> datas,int code){
		
		
		byte[] udps = new byte[UDP_BUFFER_SIZE];
		int tail_index = 0;
		// declare UDP package head.

		// 1, add package head,server,client;
		udps[0] = UDPConstants.PKG_HEAD;
		udps[1] = UDPConstants.TYPE_SERVER;
		udps[2] = UDPConstants.TYPE_CLIENT;

		// 2, add 4 bytes sequeueNO;
		String sequeueNO = datas.get(0);
		byte[] sequeues = sequeueNO.getBytes();
		// logger.debug("Response SequeueNO : "+sequeueNO+", sequeueArray size : "+sequeues.length);
		System.arraycopy(sequeues,0,udps,3,4);
		
		// 3, add 1 byte messageID;
		
		udps[7] = messageID;
		// 4, add 1 byte messageType,message head over.
		udps[8] = UDPConstants.MSG_RESPONSE;
		// Test udp message package head.
		/*for(int j = 0;j<9;j++){
			System.out.print(udps[j]+"-");
		}*/
		
		// System.out.println(" package head over .... ");
		int ipos = 9;
		// System.out.println("DataSize --> "+datas.size());
		
		// 5, add message body.
		
		// System.out.println("Last value : "+code);
		
		udps[ipos] = (byte)code;
		ipos = ipos + 1;
		udps[ipos] = UDPConstants.PKG_TAIL;
		tail_index = ipos+1;
		
		byte[] dests = new byte[tail_index];
		System.arraycopy(udps, 0, dests, 0,tail_index);
		
		return dests;
		
		
	}
	
	/**
	 * Convert event data to bytes.
	 * @param messageID
	 * @param datas
	 * @return
	 */
	public static byte[] convertEventData2Byte(byte messageID,ArrayList<String> datas) {
		
		byte[] udps = new byte[UDP_BUFFER_SIZE];
		int tail_index = 0;
		// declare UDP package head.

		// 1, add package head,server,client;
		udps[0] = UDPConstants.PKG_HEAD;
		udps[1] = UDPConstants.TYPE_SERVER;
		udps[2] = UDPConstants.TYPE_CLIENT;

		// 2, add 4 bytes sequeueNO;
		String sequeueNO = datas.get(0);
		byte[] sequeues = sequeueNO.getBytes();
		// logger.debug("Event SequeueNO : "+sequeueNO+", sequeueArray size : "+sequeues.length);
		System.arraycopy(sequeues,0,udps,3,4);
		
		// 3, add 1 byte messageID;
		udps[7] = messageID;
		// 4, add 1 byte messageType,message head over.
		udps[8] = UDPConstants.MSG_EVENT;
		// Test udp message package head.
		
		int ipos = 9;
		// System.out.println("DataSize --> "+datas.size());
		
		// 5, add message body.
		for(int index = 1;index<(datas.size()-1);index++){
			
			String data = datas.get(index);
			if(data == null || data.length() == 0){
				data = "";
			}
			
			// logger.debug("Event2Message convert data "+data + " to byte ... ");
			
			byte[] bytes = data.getBytes();
			
			int len = bytes.length;
			if(len >= 1){
				System.arraycopy(bytes,0,udps,ipos,len);
			}else{
				len = 0;
			}
			
			ipos = ipos+len;
			udps[ipos] = UDPConstants.PKG_INTERVAL; 
			ipos++;
		}
		
		if(datas.size()>1){
			String last_data = datas.get(datas.size()-1);
			if(last_data == null){
				last_data = "";
			}
			byte[] last_bytes = last_data.getBytes();
			System.arraycopy(last_bytes, 0, udps, ipos, last_bytes.length);
			ipos = ipos + last_bytes.length;
			udps[ipos] = UDPConstants.PKG_TAIL;
			tail_index = ipos+1;
		}else{
			udps[ipos] = UDPConstants.PKG_TAIL;
			tail_index = ipos+1;
		}
		
		// Remove null byte from array.
		// logger.debug("Event udps array len "+tail_index);
		byte[] dests = new byte[tail_index];
		System.arraycopy(udps, 0, dests, 0,tail_index);
		
		return dests;
		
	}

	/**
	 * Get Now TimeStamp.
	 * 
	 * @return
	 */
	public static String getTimeStamp() {
		return sdf.format(new Date());
	}

}
