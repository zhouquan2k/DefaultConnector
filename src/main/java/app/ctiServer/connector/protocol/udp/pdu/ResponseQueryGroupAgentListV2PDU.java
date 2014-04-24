package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseQueryGroupAgentListV2PDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String groupID;

	private String groupDesc;

	private String agentList;

	private String resValue1;

	private String resValue2;

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

	public String getAgentList() {
		return agentList;
	}

	public void setAgentList(String agentList) {
		this.agentList = agentList;
	}

	public String getResValue1() {
		return resValue1;
	}

	public void setResValue1(String resValue1) {
		this.resValue1 = resValue1;
	}

	public String getResValue2() {
		return resValue2;
	}

	public void setResValue2(String resValue2) {
		this.resValue2 = resValue2;
	}

}
