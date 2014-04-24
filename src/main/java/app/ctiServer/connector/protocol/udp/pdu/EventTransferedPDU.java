package app.ctiServer.connector.protocol.udp.pdu;

import app.ctiServer.connector.protocol.udp.CallInfo;
import app.ctiServer.connector.protocol.udp.CallInfoManager;
import component.util.Util;

public class EventTransferedPDU extends EventPDU {

	private static final long serialVersionUID = 1L;

	private String callID;

	private String ucid;

	private String oldPrimaryCallID;

	private String oldPrimaryUcid;

	private String oldSecondaryCallID;

	private String oldSecondaryUcid;

	private String transferingDevice;

	private String transferedDevice;

	private String callingDevice;

	private String deviceID;

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

	public String getTransferingDevice() {
		return transferingDevice;
	}

	public void setTransferingDevice(String transferingDevice) {
		this.transferingDevice = transferingDevice;
	}

	public String getTransferedDevice() {
		return transferedDevice;
	}

	public void setTransferedDevice(String transferedDevice) {
		this.transferedDevice = transferedDevice;
	}

	public String getCallingDevice() {
		return callingDevice;
	}

	public void setCallingDevice(String callingDevice) {
		this.callingDevice = callingDevice;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	@Override
	public EventTransferedPDU sendEventToClient(CallInfoManager callInfoManager) {

		Util.trace(this, "TransferedEvent : [callID=" + this.callID + ",ucid="
				+ this.ucid + ",primaryCall=" + this.oldPrimaryCallID
				+ ",secondaryCall=" + this.oldSecondaryCallID + ",eventID="
				+ this.getEventID() + ",transferringDevice="
				+ this.transferingDevice + ",trasferedDevice="
				+ this.transferedDevice + "]");
		if (callInfoManager != null) {
			if (this.getDeviceID().equals(this.getTransferingDevice())) {
				// This event for transfering device . 转移发起方
				// 1, query callID to ucid.
				CallInfo callInfo = callInfoManager.getCallInfoByCallID(
						this.getDeviceID(), this.getCallID());
				if (callInfo != null) {
					// this.setUcid("");
					this.setOldSecondaryUcid(callInfo.getUCID());
					callInfoManager.removeCallInfo(callInfo);
				} else {
					Util.trace(this, "Transfered Event callInfo is null. "
							+ this.getCallID());
				}
				// 2, query oldPrimaryCallID to oldPrimaryUcid.
				CallInfo oldPrimary_callInfo = callInfoManager
						.getCallInfoByCallID(this.getDeviceID(),
								this.getOldPrimaryCallID());
				if (oldPrimary_callInfo != null) {
					this.setUcid(oldPrimary_callInfo.getUCID());
					this.setOldPrimaryUcid(oldPrimary_callInfo.getUCID());
					this.setCallingDevice(this.getDeviceID());

					callInfoManager.removeCallInfo(oldPrimary_callInfo);
				} else {
					Util.trace(this,
							"Transfered Event oldPrimary_CallInfo is null . "
									+ this.getOldPrimaryCallID());
				}
			} else if (this.getDeviceID().equals(this.getTransferedDevice())) {
				// This event for transfered device . 转移到方
				// 1, query callID to ucid.
				CallInfo callInfo = callInfoManager.getCallInfoByCallID(
						this.getDeviceID(), this.getCallID());
				if (callInfo != null) {
					this.setOldSecondaryUcid(callInfo.getUCID());
					this.setCallingDevice(this.getDeviceID());
				} else {
					Util.trace(this,
							"Transfered Event oldSecondaryCallinfo is null. "
									+ this.getCallID());
				}

				// 2, query oldPrimaryCallInfo

				CallInfo oldPrimaryCallInfo = callInfoManager
						.getCallInfoByCallID(this.getTransferingDevice(),
								this.getOldPrimaryCallID());
				if (oldPrimaryCallInfo != null) {
					this.setOldPrimaryUcid(oldPrimaryCallInfo.getUCID());
					this.setUcid(oldPrimaryCallInfo.getUCID());
					this.setCallingDevice(this.getDeviceID());

					if (callInfo != null) {
						callInfo.setUCID(oldPrimaryCallInfo.getUCID());
					} else {
						CallInfo new_callInfo = new CallInfo(this.getCallID(),
								this.getDeviceID());
						new_callInfo.setUCID(oldPrimaryCallInfo.getUCID());
						new_callInfo.setCallID(this.getCallID());
						new_callInfo.setDeviceID(this.getDeviceID());
						new_callInfo.setCallingDevice(oldPrimaryCallInfo
								.getCallingDevice());
						new_callInfo.setCalledDevice(this.getDeviceID());
						new_callInfo.setLiveFlag(0);
						callInfoManager.addCallInfo(new_callInfo);
					}

				} else {
					Util.trace(this,
							"Transfered Event oldPrimary_CallInfo is null . "
									+ this.getOldPrimaryCallID());
					if (callInfo != null) {
						this.setUcid(callInfo.getUCID());
					} else {
						this.setUcid("");
					}
				}

			} else {
				// This event for third party . 被转移方
				CallInfo callInfo = callInfoManager.getCallInfoByCallID(
						this.getDeviceID(), this.getOldPrimaryCallID());
				if (callInfo != null) {

					this.setOldPrimaryUcid(callInfo.getUCID());
					this.setUcid(callInfo.getUCID());
					this.setCallingDevice(this.getDeviceID());
					callInfo.setCallID(this.getCallID());
					callInfoManager.addCallInfo(callInfo);

				} else {
					Util.trace(
							this,
							"Transfered Event oldPrimaryCall is null, "
									+ this.getOldPrimaryCallID());

					// 非被转移方,转移到方处理.

					// 1, query callID to ucid.
					callInfo = callInfoManager.getCallInfoByCallID(
							this.getDeviceID(), this.getCallID());
					if (callInfo != null) {
						this.setOldSecondaryUcid(callInfo.getUCID());
						this.setCallingDevice(this.getDeviceID());
					} else {
						Util.trace(this,
								" ~~~~ Transfered Event oldSecondaryCallinfo is null. "
										+ this.getCallID());
					}

					// 2, query oldPrimaryCallInfo

					CallInfo oldPrimaryCallInfo = callInfoManager
							.getCallInfoByCallID(this.getTransferingDevice(),
									this.getOldPrimaryCallID());
					if (oldPrimaryCallInfo != null) {
						this.setOldPrimaryUcid(oldPrimaryCallInfo.getUCID());
						this.setUcid(oldPrimaryCallInfo.getUCID());
						this.setCallingDevice(this.getDeviceID());

						if (callInfo != null) {
							callInfo.setUCID(oldPrimaryCallInfo.getUCID());
						} else {
							CallInfo new_callInfo = new CallInfo(
									this.getCallID(), this.getDeviceID());
							new_callInfo.setUCID(oldPrimaryCallInfo.getUCID());
							new_callInfo.setCallID(this.getCallID());
							new_callInfo.setDeviceID(this.getDeviceID());
							new_callInfo.setCallingDevice(oldPrimaryCallInfo
									.getCallingDevice());
							new_callInfo.setCalledDevice(this.getDeviceID());
							new_callInfo.setLiveFlag(0);
							callInfoManager.addCallInfo(new_callInfo);
						}

					} else {
						Util.trace(this,
								"Transfered Event oldPrimary_CallInfo is null . "
										+ this.getOldPrimaryCallID());
						if (callInfo != null) {
							this.setUcid(callInfo.getUCID());
						} else {
							this.setUcid("");
						}
					}

				}
			}

		} else {
			Util.trace(this, "CallInfoManager is null for transferEvent ... ");
		}

		Util.trace(
				this,
				"Send TransferedEvent [ucid=" + this.ucid + ",primaryUcid="
						+ this.oldPrimaryUcid + ",secondaryUcid="
						+ this.oldSecondaryUcid + "] to client ok, sessionID="
						+ this.getSessionID() + ", deviceID="
						+ this.getEventID());
		return this;

	}
}
