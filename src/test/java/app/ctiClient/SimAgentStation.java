package app.ctiClient;

import java.rmi.RemoteException;

import component.cti.Constants.AgentMode;
import component.cti.model.IConnection;
import component.cti.model.IStation;
import component.cti.protocol.ClearCallRequest;
import component.cti.protocol.ClearConnectionRequest;
import component.cti.protocol.MakeCallRequest;
import component.cti.protocol.MakeCallResponse;
import component.cti.protocol.Session;
import component.cti.protocol.SetAgentStateRequest;
import component.cti.protocol.SnapshotDeviceRequest;
import component.cti.protocol.SnapshotDeviceResponse;
import component.cti.protocol.SetAgentStateRequest.AgentFunc;
import component.util.Event;
import component.util.Future;
import component.util.RemoteEventListener;

public class SimAgentStation implements RemoteEventListener {

	private Session session;
	private String deviceId;
	private String deviceId2;
	
	SimAgentStation(Session session,String deviceId,String deviceId2)
	{
		this.session=session;
		this.deviceId=deviceId;
		this.deviceId2=deviceId2;
	}
	
	public void init() throws Throwable
	{
		System.out.println("session id:"+session.getId());
		resetDevice(deviceId);
		session.setEventListener(this);
		session.monitorDevice(new String[]{deviceId});
	}
	
	
	public void run() throws Throwable
	{		
		SetAgentStateRequest req=new SetAgentStateRequest(AgentFunc.Login,deviceId);
		req.agentMode=AgentMode.NotReady;
		req.agentId=this.deviceId;
		req.password="000000";
		session.setAgentState(req);
	
		MakeCallRequest req1=new MakeCallRequest(deviceId2,deviceId);		
		Future<MakeCallResponse> resp1=session.makeCall(req1);
		MakeCallResponse mr=resp1.get();
		String callId=mr.callId;
		System.out.println("make call id:"+callId);
				
		Thread.sleep(1000);
		
		SnapshotDeviceResponse resp2=session.snapshotDevice(new SnapshotDeviceRequest(deviceId)).get();
		System.out.println("snapshot device:"+resp2);
		IStation s=(IStation)resp2.device;
		
		System.out.println("device status:"+s.detailString());
		
		SnapshotDeviceResponse resp3=session.snapshotDevice(new SnapshotDeviceRequest("101")).get();
		System.out.println("snapshot device:"+resp3);
		
		/*
		SnapshotDeviceRequest r=new SnapshotDeviceRequest(null);
		r.agentId=deviceId;
		*/
		
		
		session.clearCall(new ClearCallRequest(deviceId,callId));				
		
		Thread.sleep(1000);
		
		this.resetDevice(this.deviceId);
		
	}
	
	public void exit() throws Throwable
	{
		
		session.close();
	}

	@Override
	public void onEvent(Event event) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	
	private void resetDevice(String deviceId) throws Throwable
	{
		SnapshotDeviceResponse resp4=session.snapshotDevice(new SnapshotDeviceRequest(deviceId)).get();
		System.out.println("snapshot device:"+resp4);
		IStation station=(IStation)resp4.device;
		for (IConnection conn:station.getCalls())
		{
			session.clearConnection(new ClearConnectionRequest(deviceId,conn.getCallId()));
		}
		if (station.getAgentMode()!=AgentMode.Logout)
			session.setAgentState(new SetAgentStateRequest(AgentFunc.Logout,deviceId));
	}
	
	/*
	public static void main(String[] args) throws Throwable
	{
		SocketCTIAPIFactory factory=new SocketCTIAPIFactory();
		factory.init();
		SessionApi ctiApi=(SessionApi)factory.getProtocol("localhost:8123","cti");
		
		SimAgentStation sim=new SimAgentStation(ctiApi,"3331","3332");
		sim.init();
		sim.run();
	}
	*/
}
