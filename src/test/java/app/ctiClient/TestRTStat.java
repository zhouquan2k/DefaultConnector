package app.ctiClient;


import java.util.Vector;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import app.ctiServer.connector.protocol.socket.SocketCTIAPIFactory;

import component.cti.rtstat.IRealtimeStat;
import component.cti.rtstat.StatConfiguration.StatDefinition;
import component.cti.rtstat.StatConfiguration.StatDefinition.StatDefItem;
import component.spring.BootSpring;
import component.util.Event;
import component.util.RemoteEventListener;

@Test
public class TestRTStat implements RemoteEventListener
{
	private SocketCTIAPIFactory factory;
	private IRealtimeStat rtStatApi;
	
	
	@BeforeTest
	public void init() throws Throwable
	{
		BootSpring.initContext("spring-test.xml");
		factory=new SocketCTIAPIFactory();
		factory.init();
		this.rtStatApi=factory.getRTStatApi("localhost:8123");
	}
	
	@Test
	public void test1() throws Throwable
	{
		try
		{
			this.rtStatApi.setEventListener(this);
			
			StatDefinition def=new StatDefinition();
			def.name="test";
			def.items=new Vector<StatDefItem>();
			StatDefItem item=new StatDefItem();
			item.pattern="agentDefault";
			item.subjects="301,302";
			def.items.add(item);
			this.rtStatApi.defineSubscription(def);
			
			this.rtStatApi.subscribe("test", 3000);
			
			synchronized(this)
			{
				this.wait();
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@AfterTest
	public void exit() throws InterruptedException
	{
		Thread.sleep(10000);
		factory.exit();
	}

	@Override
	public void onEvent(Event event) {
		//Util.trace(this,">>>>>>>>>>>>>> "+event);
		
	}
}
