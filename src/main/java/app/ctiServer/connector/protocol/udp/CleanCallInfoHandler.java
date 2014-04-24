package app.ctiServer.connector.protocol.udp;

import component.util.Util;

public class CleanCallInfoHandler extends Thread {

	private CallInfoManager callInfoManager;

	public CleanCallInfoHandler(CallInfoManager callInfoManager) {
		this.callInfoManager = callInfoManager;
	}

	@Override
	public void run() {

		Util.trace(this, "Clean callInfo handler started ... ");

		/**
		 * Modify ConcurrentModificationException
		 */
		while (true) {
			try {
				//Util.trace(this,"!!!!!!!!! CleanHandler start to clean callInfo ... ");

				callInfoManager.updateCallDeviceInfo();
				
				Thread.sleep(2000);
				
			} catch (Exception e) {
				Util.error(this, e,"");
				e.printStackTrace();
			}
		}
	}

}
