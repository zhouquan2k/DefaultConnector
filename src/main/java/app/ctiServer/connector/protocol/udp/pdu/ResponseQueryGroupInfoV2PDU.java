package app.ctiServer.connector.protocol.udp.pdu;

import component.cti.event.SnapshotEvent;

public class ResponseQueryGroupInfoV2PDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String groupID;

	private String groupDesc;

	private String avaibleAgents;

	private String queueNo;

	private String logedAgents;

	public ResponseQueryGroupInfoV2PDU() {
		// TODO Auto-generated constructor stub
	}

	public ResponseQueryGroupInfoV2PDU(SnapshotEvent event) {
		this.logedAgents = (String) event.getProperty("loggedInAgents");
		this.queueNo = (String) event.getProperty("callsInQueue");
		this.avaibleAgents = (String) event.getProperty("availableAgents");
		this.groupDesc = (String) event.getProperty("groupDesc");
		this.groupID = event.srcDeviceId;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getAvaibleAgents() {
		return avaibleAgents;
	}

	public void setAvaibleAgents(String avaibleAgents) {
		this.avaibleAgents = avaibleAgents;
	}

	public String getQueueNo() {
		return queueNo;
	}

	public void setQueueNo(String queueNo) {
		this.queueNo = queueNo;
	}

	public String getLogedAgents() {
		return logedAgents;
	}

	public void setLogedAgents(String logedAgents) {
		this.logedAgents = logedAgents;
	}
}
