package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.util.Util;

/**
 * Basic interface to query agentState.
 * 
 * @author Dev.pyh
 * 
 */
public class RequestSnapshotAgentStatePDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String agentID;

	private String groupID;

	public String getAgentID() {
		return agentID;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this,
				"SnapshotAgentState request ... agentId=" + this.getAgentID()
						+ ",groupId=" + this.getGroupID());

	}
}
