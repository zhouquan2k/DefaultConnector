package app.ctiServer.connector.protocol.udp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.ctiServer.connector.protocol.udp.CallDevice.CallStatus;
import component.util.Util;

public class CallInfoManager {
		
	private static CallInfoManager instance;
	
	private Map<String,CallDevice> callDeviceMaps = null;
	
	private CallInfoManager(){
		callDeviceMaps = Collections.synchronizedMap(new HashMap<String,CallDevice>());
	}
	
	public static CallInfoManager getInstance(){
		
		if(instance == null){
			instance = new CallInfoManager();
		}
		return instance;
	}
	
	
	public Set<String> getAllDevices(){
		return callDeviceMaps.keySet();
	}
	
	public void traceCallInfoMaps(String deviceID){
		
		CallDevice callDevice = callDeviceMaps.get(deviceID);
		if(callDevice != null){
			Util.trace(this, "!!! Trace callDevice --> "+callDevice.toString());
		}
		
	}
	
	public boolean checkDeviceIDExist(String deviceID){
		
		if(deviceID == "" || deviceID == null){
			return false;
		}else{
			
			boolean iret = callDeviceMaps.containsKey(deviceID);
			return iret;
		}
	}
	
	public CallDevice getCallDevice(String deviceID){
		
		if(deviceID == "" || deviceID == null){
			return null;
		}else{
			return callDeviceMaps.get(deviceID);
		}
	}
	
	public Set<CallInfo> getCallInfoByDevice(String deviceID){
		CallDevice callDevice = this.getCallDevice(deviceID);
		if(callDevice != null){
			return callDevice.getCallInofSet();
		}
		return null;
	}
	
	
	public void removeDeviceID(String deviceID){
		
		if(this.checkDeviceIDExist(deviceID)){
			synchronized(callDeviceMaps){
				callDeviceMaps.remove(deviceID);
				Util.trace(this, "Remove deviceID="+deviceID +" from callDeviceMaps ... "+callDeviceMaps.size());
			}
		}
	}

	
	public CallInfo getCallInfoByCallID(String deviceId,String callID){
		
		CallInfo callInfo = null;
		
		CallDevice callDevice = callDeviceMaps.get(deviceId);
		if(callDevice != null){
			callInfo = callDevice.getCallInfoByCallId(callID);
		}
		
		return callInfo;
		
	}
	
	/*
	public CallInfo getCallInfoByUcid(String deviceId,String ucid){
		
		CallInfo callInfo = null;
		CallDevice callDevice = this.callDeviceMaps.get(deviceId);
		if(callDevice != null){
			callInfo = callDevice.getCallInfoByUcid(ucid);
		}
		
		return callInfo;
	}
	*/
	
	public CallInfo getActiveCallInfoByUcid(String deviceId,String ucid){
		
		CallInfo callInfo = null;
		CallDevice callDevice = this.callDeviceMaps.get(deviceId);
		if(callDevice != null){
			callInfo = callDevice.getActiveCallInfoByUcid(ucid);
		}
		
		return callInfo;
	}
	
	
	public boolean addCallInfo(CallInfo callInfo){
		
		if(callInfo != null){
			
			String deviceId = callInfo.getDeviceID();
			CallDevice callDevice = callDeviceMaps.get(deviceId);
			if(callDevice != null){
				callDevice.addCallInfo(callInfo);
				return true;
			}else{
				CallDevice newCallDevice = new CallDevice();
				newCallDevice.setDeviceId(deviceId);
				newCallDevice.setServerId("");
				newCallDevice.addCallInfo(callInfo);
				newCallDevice.setCallStatus(CallStatus.Busy);
				synchronized(callDeviceMaps){
					callDeviceMaps.put(deviceId, newCallDevice);
				}
				Util.trace(this, "Add new CallDevice["+deviceId+"] into callDeviceMap ok.");
				return true;
			}
			
		}else{
			Util.trace(this, "Add callInfo failure : callInfo is null.");
		}
		
		return false;
	}
	
	public void removeCallInfoByCallId(String deviceId,String callId){
		
		CallDevice callDevice = this.callDeviceMaps.get(deviceId);
		if(callDevice != null){
			callDevice.removeCallInfoByCallId(callId);
		}
		
	}
	
	public void removeCallInfo(CallInfo callInfo){
		
		if(callInfo != null){
			this.removeCallInfoByCallId(callInfo.getDeviceID(), callInfo.getCallID());
		}
		
	}
	
	
	public void updateCallDeviceInfo(){
		
		if(this.callDeviceMaps.size()>0){
			
			List<String> tempDeviceIdList = new ArrayList<String>();
			
			Set<String> deviceIdSet = this.callDeviceMaps.keySet();
			for(String deviceId : deviceIdSet){
				
				this.traceCallInfoMaps(deviceId);
				
				CallDevice callDevice = this.callDeviceMaps.get(deviceId);
				if(callDevice != null){
					
					callDevice.updateCallInfo();
					
					if(callDevice.getCallStatus() == CallStatus.Idle){
						tempDeviceIdList.add(deviceId);
					}
					
				}else{
					tempDeviceIdList.add(deviceId);
				}
				
			}
			
			if(tempDeviceIdList.size()>0){
				synchronized(callDeviceMaps){
					for(String deviceId : tempDeviceIdList){
						callDeviceMaps.remove(deviceId);
						Util.trace(this, "Remove deviceID="+deviceId +" from callDeviceMaps ... "+callDeviceMaps.size());
					}
				}
			}
			
		}
		
	}
	
	
}
