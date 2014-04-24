package app.ctiServer.connector.protocol.udp.pdu;

public class ResponseQueryTrunkInfoV2PDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private String trunkID;

	private String trunkDesc;

	private String availableTrunk;

	private String usedTrunk;

	private String resStr1;

	private String resStr2;

	public String getTrunkID() {
		return trunkID;
	}

	public void setTrunkID(String trunkID) {
		this.trunkID = trunkID;
	}

	public String getTrunkDesc() {
		return trunkDesc;
	}

	public void setTrunkDesc(String trunkDesc) {
		this.trunkDesc = trunkDesc;
	}

	public String getAvailableTrunk() {
		return availableTrunk;
	}

	public void setAvailableTrunk(String availableTrunk) {
		this.availableTrunk = availableTrunk;
	}

	public String getUsedTrunk() {
		return usedTrunk;
	}

	public void setUsedTrunk(String usedTrunk) {
		this.usedTrunk = usedTrunk;
	}

	public String getResStr1() {
		return resStr1;
	}

	public void setResStr1(String resStr1) {
		this.resStr1 = resStr1;
	}

	public String getResStr2() {
		return resStr2;
	}

	public void setResStr2(String resStr2) {
		this.resStr2 = resStr2;
	}
}
