package app.ctiServer.connector.protocol.udp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import component.util.Util;


public class CallDevice implements Serializable {
	
	enum CallStatus{
		Busy,Idle
	}

	private static final long serialVersionUID = 1L;
	
	private static final int CALL_LEN_TIMEOUT = 7200;
	
	
	private String deviceId;
	private String serverId;
	private Set<CallInfo> callInfoSet = new HashSet<CallInfo>();
	private CallStatus callStatus;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public CallStatus getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(CallStatus callStatus) {
		this.callStatus = callStatus;
	}
	
	public void addCallInfo(CallInfo callInfo){
		CallInfo _callInfo = this.getCallInfoByCallId(callInfo.getCallID());
		if(_callInfo != null){
			Util.trace(this, "CallInfo["+callInfo.toString()+"] already exist @ device="+this.deviceId);
			if(callInfo.getUCID() != null)_callInfo.setUCID(callInfo.getUCID());
			if(callInfo.getCallingDevice() != null)_callInfo.setCallingDevice(callInfo.getCallingDevice());
			if(callInfo.getCalledDevice() != null)_callInfo.setCalledDevice(callInfo.getCalledDevice());
			if(callInfo.getDeviceID() != null)_callInfo.setDeviceID(callInfo.getDeviceID());
			if(callInfo.getLiveFlag() != 0)_callInfo.setLiveFlag(callInfo.getLiveFlag());
		}else{
			Util.trace(this, "Add new CallInfo: "+callInfo.toString()+" @ deviceId="+this.deviceId);
			this.callInfoSet.add(callInfo);
		}
		this.setCallStatus(CallStatus.Busy);
	}
	
	public void removeCallInfo(CallInfo callInfo){
		
		if(callInfo != null){
			
			CallInfo _callInfo = this.getCallInfoByCallId(callInfo.getCallID());
			if(_callInfo != null){
				if(_callInfo.getLiveFlag()<1){
					_callInfo.setLiveFlag(1);
				}
			}
		}
		
	}
	
	public void removeCallInfoByCallId(String callId){
		
		CallInfo _callInfo = this.getCallInfoByCallId(callId);
		if(_callInfo != null){
			if(_callInfo.getLiveFlag()<1){
				_callInfo.setLiveFlag(1);
			}
		}
		
	}
	/*
	public CallInfo getCallInfoByUcid(String ucid){
		if(this.callInfoSet.size()>0){
			for(CallInfo callInfo : callInfoSet){
				if(callInfo != null){
					if(callInfo.getUCID().equals(ucid)){
						return callInfo;
					}
				}
			}
		}
		return null;
	}
	*/
	
	public CallInfo getActiveCallInfoByUcid(String ucid){
		if(this.callInfoSet.size()>0){
			for(CallInfo callInfo : callInfoSet){
				if(callInfo != null && callInfo.getLiveFlag() == 0){
					if(callInfo.getUCID().equals(ucid)){
						return callInfo;
					}
				}
			}
		}
		return null;
	}
	
	public Set<CallInfo> getCallInofSet(){
		return this.callInfoSet;
	}
	
	public CallInfo getCallInfoByCallId(String callId){
		if(this.callInfoSet.size()>0){
			for(CallInfo callInfo : callInfoSet){
				if(callInfo != null){
					if(callInfo.getCallID().equals(callId)){
						return callInfo;
					}
				}
			}
		}
		return null;
	}
	
	public void updateCallInfo(){
		
		if(this.callInfoSet.size()>0){
			
			List<CallInfo> tempCallIdList = new ArrayList<CallInfo>();
			
			for(CallInfo callInfo : callInfoSet){
				if(callInfo != null){
					
					int callLen = (int)((System.currentTimeMillis()-callInfo.getCreateTime().getTime())/1000);
					// util.trace(this, "callId="+callInfo.getCallID()+",nowTime="+System.currentTimeMillis()+",createTime="+callInfo.getCreateTime().getTime()+", callLen="+callLen);
					if(callLen<CALL_LEN_TIMEOUT){
						int liveFlag = callInfo.getLiveFlag();
						if(liveFlag >=2){
							tempCallIdList.add(callInfo);
						}else{
							if(liveFlag>0){
								liveFlag = liveFlag + 1;
								callInfo.setLiveFlag(liveFlag);
							}
						}
					}else{
						tempCallIdList.add(callInfo);
					}
					
				}
			}
			
			if(tempCallIdList.size()>0){
				
				synchronized(this.callInfoSet){
					for(CallInfo _callInfo : tempCallIdList){
						boolean iRet = this.callInfoSet.remove(_callInfo);
						Util.trace(this, "Remove callInfo: "+_callInfo.toString()+" @ deviceId="+this.deviceId+" ret="+iRet);
					}
				}
			}
			
			if(this.callInfoSet.size()<=0){
				this.setCallStatus(CallStatus.Idle);
			}
			
		}else{
			this.setCallStatus(CallStatus.Idle);
		}
		
	}

	@Override
	public boolean equals(Object obj){
		if(obj != null && obj instanceof CallDevice){
			CallDevice callDevice = (CallDevice)obj;
			if(callDevice.getDeviceId().equals(this.deviceId)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "CallDevice : ["+callInfoSet.size()+":{"+callInfoSet.toString()+"}]";
	}
	

}
