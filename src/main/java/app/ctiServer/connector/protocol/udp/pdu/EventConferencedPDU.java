package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventConferencedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String ucid;

	private String oldPrimaryCallID;

	private String oldPrimaryUcid;

	private String oldSecondaryCallID;

	private String oldSecondaryUcid;

	private String deviceList;

	private String addParty;

	private String confController;

	private String deviceID;

	public String getAddParty() {
		return addParty;
	}

	public void setAddParty(String addParty) {
		this.addParty = addParty;
	}

	public String getConfController() {
		return confController;
	}

	public void setConfController(String confController) {
		this.confController = confController;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getUcid() {
		return ucid;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getOldPrimaryCallID() {
		return oldPrimaryCallID;
	}

	public void setOldPrimaryCallID(String oldPrimaryCallID) {
		this.oldPrimaryCallID = oldPrimaryCallID;
	}

	public String getOldPrimaryUcid() {
		return oldPrimaryUcid;
	}

	public void setOldPrimaryUcid(String oldPrimaryUcid) {
		this.oldPrimaryUcid = oldPrimaryUcid;
	}

	public String getOldSecondaryCallID() {
		return oldSecondaryCallID;
	}

	public void setOldSecondaryCallID(String oldSecondaryCallID) {
		this.oldSecondaryCallID = oldSecondaryCallID;
	}

	public String getOldSecondaryUcid() {
		return oldSecondaryUcid;
	}

	public void setOldSecondaryUcid(String oldSecondaryUcid) {
		this.oldSecondaryUcid = oldSecondaryUcid;
	}

	public String getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(String deviceList) {
		this.deviceList = deviceList;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	@Override
	public EventConferencedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "ConferencedEvent : [callID=" + this.callID + ",ucid="
				+ this.ucid + ",primaryCall=" + this.oldPrimaryCallID
				+ ",secondaryCall=" + this.oldSecondaryCallID + ",eventID="
				+ this.getEventID() + ",addParty=" + this.addParty
				+ ",confController=" + this.confController + "]");
		if (callInfoManager != null) {

			StringBuffer conf_buffer = new StringBuffer(this.confController
					+ ",");

			CallInfo primaryCallInfo = callInfoManager.getCallInfoByCallID(
					this.confController, this.getOldPrimaryCallID());
			if (primaryCallInfo != null) {

				if (primaryCallInfo.getUCID().equals(ucid)) {
					// First conference,change primaryCallId and
					// secondaryCallId.

					CallInfo secondaryCallInfo = callInfoManager
							.getCallInfoByCallID(this.confController,
									this.getOldSecondaryCallID());
					if (secondaryCallInfo != null) {
						primaryCallInfo.setUCID(secondaryCallInfo.getUCID());
						secondaryCallInfo.setUCID(ucid);
					}
					Util.trace(this, "Begin to remove primaryCallInfo : "
							+ primaryCallInfo);
					callInfoManager.removeCallInfo(primaryCallInfo);

				}

				conf_buffer.append(primaryCallInfo.getCallingDevice() + ",");
				CallInfo secondaryCallInfo = callInfoManager
						.getCallInfoByCallID(this.confController,
								this.getOldSecondaryCallID());
				if (secondaryCallInfo != null) {
					conf_buffer.append(secondaryCallInfo.getCalledDevice());
				}

				String confList = conf_buffer.toString();
				if (confList.endsWith(",")) {
					confList = confList.substring(0, confList.length() - 1);
				}

				this.setDeviceList(confList);

				this.setUcid(ucid);
				this.setOldPrimaryUcid(ucid);
				this.setOldSecondaryUcid(primaryCallInfo.getUCID());

				if (this.getDeviceID().equals(this.addParty)) {
					CallInfo oldSecondaryCallInfo = callInfoManager
							.getCallInfoByCallID(this.getDeviceID(),
									this.getOldSecondaryCallID());
					if (oldSecondaryCallInfo != null) {
						oldSecondaryCallInfo.setUCID(ucid);
						// callInfoManager.removeCallInfo(oldSecondaryCallInfo);
						// util.trace(this,
						// "Update SecondaryCallInfo[ucid="+oldSecondaryCallInfo.getUCID()+",callId="+oldSecondaryCallInfo.getCallID()+"] @ device="+_deviceID);
					}
				} else if (this.getDeviceID().equals(this.confController)) {
					// TODO
				} else {
					CallInfo oldPrimaryCallInfo = callInfoManager
							.getCallInfoByCallID(this.getDeviceID(),
									this.getOldPrimaryCallID());
					if (oldPrimaryCallInfo != null) {
						oldPrimaryCallInfo.setCallID(this
								.getOldSecondaryCallID());
						// callInfoManager.removeCallInfo(oldPrimaryCallInfo);
						Util.trace(this, "Update PrimaryCallInfo[ucid="
								+ oldPrimaryCallInfo.getUCID() + ",callId="
								+ oldPrimaryCallInfo.getCallID()
								+ "] @ device=" + this.getDeviceID());
					} else {
						oldPrimaryCallInfo = callInfoManager
								.getCallInfoByCallID(this.getDeviceID(),
										this.getOldSecondaryCallID());
						if (oldPrimaryCallInfo != null) {
							oldPrimaryCallInfo.setUCID(this.ucid);
							Util.trace(
									this,
									"Update SecondaryCallInfo[ucid=" + ucid
											+ ",callId="
											+ this.getOldSecondaryCallID());
						}
					}

				}

			}
		}
		Util.trace(this, "Send ConferencedEvent [ucid=" + this.ucid
				+ ",primaryUcid=" + this.oldPrimaryUcid + ",secondaryUcid="
				+ this.oldSecondaryUcid + ",deviceList=" + this.getDeviceList()
				+ "] to client ok, sessionID=" + this.getSessionID()
				+ ", deviceID=" + this.getEventID());

		return this;
	}

}
