package app.ctiServer.connector.protocol.udp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.ctiServer.connector.protocol.udp.pdu.RequestPDU;
import component.util.Util;

public class CleanRequestMapHandler extends Thread {

	private final static long TimeOut = 20000;

	private Map<String, RequestPDU> requestMaps;

	public CleanRequestMapHandler(Map<String, RequestPDU> requestMaps) {
		this.requestMaps = requestMaps;
	}

	@Override
	public void run() {

		Util.trace(this, "CleanRequestMap handler start ...");

		while (true) {

			if (requestMaps != null && requestMaps.size() > 0) {

				synchronized (requestMaps) {

					Set<String> requestSet = requestMaps.keySet();
					List<String> tempRequestList = new ArrayList<String>();
					for (String requestID : requestSet) {
						RequestPDU requestPDU = requestMaps.get(requestID);
						if (requestPDU != null) {
							long delay = (new Date().getTime() - requestPDU
									.getMessageTimeStamp().getTime());
							if (delay > TimeOut) {
								tempRequestList.add(requestID);
							}
						}
					}

					if (tempRequestList != null && tempRequestList.size() > 0) {
						for (String rID : tempRequestList) {
							requestMaps.remove(rID);
						}
					}
				}
			}
			
			try {
				Thread.sleep(TimeOut);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

	}

}
