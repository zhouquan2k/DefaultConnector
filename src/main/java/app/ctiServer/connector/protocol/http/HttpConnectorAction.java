package app.ctiServer.connector.protocol.http;

import component.util.annotation.Controller;

/**
 * 
 * @author Administrator
 *  通过http请求查询CTI提供的接口Action
 */
@Controller
public interface HttpConnectorAction {

	public void dealRequest();
}
