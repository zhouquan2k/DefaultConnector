package app.ctiServer.connector.protocol.udp;

import java.io.Serializable;
import java.util.Date;

/**
 * Call Information include callID,UCID,deviceID,callingDevice,calledDevice etc.
 * @author Dev.wtx
 *
 */
public class CallInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String callID;
	private String UCID;
	private String deviceID;
	private String callingDevice;
	private String calledDevice;
	private Date createTime;
	private int liveFlag;  // default value "0"; deleted value "1"; when value == "3" delete this callInfo.  
	
	public CallInfo(String callId,String deviceId){
		this.callID = callId;
		this.deviceID = deviceId;
		this.createTime = new Date();
		this.liveFlag = 0;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getLiveFlag() {
		return liveFlag;
	}

	public void setLiveFlag(int liveFlag) {
		this.liveFlag = liveFlag;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getUCID() {
		return UCID;
	}

	public void setUCID(String uCID) {
		UCID = uCID;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getCallingDevice() {
		return callingDevice;
	}

	public void setCallingDevice(String callingDevice) {
		this.callingDevice = callingDevice;
	}

	public String getCalledDevice() {
		return calledDevice;
	}

	public void setCalledDevice(String calledDevice) {
		this.calledDevice = calledDevice;
	}

	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof CallInfo){
			CallInfo call = (CallInfo)obj;
			if(call.getCallID().equals(this.getCallID())){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Integer.parseInt(this.callID);
	}

	@Override
	public String toString() {
		return "[ucid="+this.getUCID()+", callID="+this.getCallID()+", deviceID="+this.getDeviceID()+", liveFlag="+this.getLiveFlag()+",hashCode="+this.hashCode()+",createTime="+this.getCreateTime()+"]";
	}

}
