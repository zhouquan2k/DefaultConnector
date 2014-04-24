package app.ctiServer.connector.protocol.udp.pdu;

import java.util.ArrayList;
import java.util.List;

public class ResponseQueryVDNListPDU extends ResponsePDU {

	private static final long serialVersionUID = 1L;

	private int endCode;

	private String vdnLen;

	private List<String> vdnList = new ArrayList<String>();

	private List<String> vdnDesc = new ArrayList<String>();

	public String getVdnLen() {
		return vdnLen;
	}

	public void setVdnLen(String vdnLen) {
		this.vdnLen = vdnLen;
	}

	public int getEndCode() {
		return endCode;
	}

	public void setEndCode(int endCode) {
		this.endCode = endCode;
	}

	public List<String> getVdnList() {
		return vdnList;
	}

	public void setVdnList(String vdn) {
		this.vdnList.add(vdn);
	}

	public List<String> getVdnDesc() {
		return vdnDesc;
	}

	public void setVdnDesc(String vdnDesc) {
		this.vdnDesc.add(vdnDesc);
	}
}
