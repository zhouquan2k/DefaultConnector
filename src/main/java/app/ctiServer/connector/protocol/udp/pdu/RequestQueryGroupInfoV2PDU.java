package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.UDPHandler;
import component.util.Util;

public class RequestQueryGroupInfoV2PDU extends RequestPDU {

	private static final long serialVersionUID = 1L;

	private String deviceID;

	private String groupID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	@Override
	public void invokeAPI(UDPHandler udpHandler) throws Throwable {

		Util.trace(this, "QueryGroupInfo request ... deviceId=" + this.deviceID
				+ ",groupId=" + this.groupID);
		ResponseQueryGroupInfoV2PDU groupInfoV2PDU = udpHandler
				.getGroupInfoV2PDU(this.groupID);
		if (groupInfoV2PDU != null) {
			groupInfoV2PDU.setGroupID(this.groupID);
			groupInfoV2PDU.setClientID(this.getClientID());
			groupInfoV2PDU.setMessageID(this.getMessageID());
			udpHandler.write(groupInfoV2PDU, this.getClientID());
		}

	}

}
