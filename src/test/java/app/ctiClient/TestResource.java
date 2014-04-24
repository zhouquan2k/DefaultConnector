package app.ctiClient;

import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import app.ctiServer.connector.protocol.socket.SocketCTIAPIFactory;

import component.org.OrgService;
import component.org.TOrg;
import component.resource.Agent;
import component.resource.Resource;
import component.resource.ResourceMgr;
import component.security.SecurityService;
import component.spring.BootSpring;
import component.util.UserException;
import component.util.Util;

public class TestResource {

	private SocketCTIAPIFactory factory;
	private ResourceMgr resourceMgr;
	private OrgService orgService;
	private SecurityService securityService;
	//private SecurityMgr securityMgr;
	
	@BeforeTest
	public void init() throws Throwable
	{
		BootSpring.initContext("spring-test.xml");
		factory=new SocketCTIAPIFactory();
		factory.init();
		this.resourceMgr=factory.getResourceApi("localhost:8123");
		this.orgService=(OrgService)factory.getApi("localhost:8123", OrgService.class, "org");
		this.securityService=(SecurityService)factory.getApi("localhost:8123", SecurityService.class, "security");
		
	}
	
	@Test
	public void test1() throws Throwable
	{
		try
		{
			List<Resource> res=this.resourceMgr.queryResources("Agent","rtstat");
			Util.trace(this,"resources:"+res);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void test2() throws Throwable
	{
		try
		{
			List<Agent> res=this.resourceMgr.queryAgentsByOrg("abcdedf","cti","Agent");
			Util.trace(this,"resources:"+res);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test public void test3() throws Throwable
	{
		try
		{
			List<TOrg> res=this.orgService.queryOrgs(null, null);
			Util.trace(this,"resources:"+res);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void test4() throws Throwable
	{
		try
		{
			String sessionId=this.securityService.login("username", "password");
			Util.trace(this,"sessionId:"+sessionId);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			if (e.getMessage()!=null&&e.getMessage().indexOf("invalid user")<0) throw e;
		}
		

	}
	
	@Test void test5()
	{
		String sessionId=this.securityService.login("zhouquan", "1234");
		String permission="test";
		boolean b=this.securityService.hasPermission(sessionId, permission);
		Util.trace(this, "has permission: %s %s",permission,b);
		//Util.trace(this,"user:"+user);
	}
	
	@Test void test6()
	{
		String sessionId=this.securityService.login("admin", "admin");
		String permission="xxx";
		boolean b=this.securityService.hasPermission(sessionId, permission);
		Util.trace(this, "has permission: %s %s",permission,b);
		
		this.securityService.touch(sessionId);
		//Util.trace(this,"user:"+user);
	}
	
	@Test void test7() throws UserException
	{
		this.resourceMgr.changeAgentPassword("40280881406e3cc501406e5159f70003", "111", "222");
	}
	
	
}
