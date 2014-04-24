package app.ctiServer.connector.protocol.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.ctiServer.connector.Request;

import component.util.Util;
import component.web.impl.ResourceLoaderServlet;


public class HttpConnectorActionImpl implements HttpConnectorAction {

	public HttpProtocolMgr protocolMgr;
	
	public void setProtocolMgr(HttpProtocolMgr protocolMgr) {
		this.protocolMgr = protocolMgr;
	}

	@SuppressWarnings("unchecked")
	public void dealRequest(){
		HttpServletRequest request = ResourceLoaderServlet.request.get();
		HttpServletResponse response = ResourceLoaderServlet.response.get();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		Request request2 = new Request();
		Enumeration<String> paramNames = request.getParameterNames();
		List<Object> params = new ArrayList<Object>();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if("method".equals(paramName)){
				request2.method = request.getParameter(paramName);
				continue;
			}
			params.add(request.getParameter(paramName));
		}
		
		request2.params = params.toArray();
		request2.object = "cti";
		String result = null;
		try {
			result = protocolMgr.processRequest(request2);
		} catch (Throwable e1) {
			Util.trace(this, e1.getMessage(), "");
		}
		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			Util.trace(this, e.getMessage(), "");
		}
	}
}
