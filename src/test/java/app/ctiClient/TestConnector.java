package app.ctiClient;

import java.util.concurrent.ExecutorService;

import app.ctiServer.connector.protocol.socket.SocketCTIAPIFactory;

import component.cti.protocol.Session;
import component.spring.BootSpring;
import component.util.ThreadPoolExecutor;
import component.util.Util;


class RunTask implements Runnable
{
	private SimAgentStation sim;
	RunTask(SimAgentStation sim)
	{
		this.sim=sim;
	}
	@Override
	public void run() {
		try
		{
		sim.init();
		while (true)
			sim.run();
		}
		catch (Throwable e)
		{
			Util.error(this,e,null);
		}		
	}
}

public class TestConnector {
	
	private SocketCTIAPIFactory factory;
	//private SessionApi ctiApi;
	private ExecutorService executor;
	//String url="172.16.2.2:3535";
	String url="localhost:8123";
	
	void init() throws Throwable
	{
		BootSpring.initContext("spring-test.xml");
		factory=new SocketCTIAPIFactory();
		factory.init();
		
		executor=ThreadPoolExecutor.newThreadPoolExecutor("executor", 2,2, 2000, 100);
		
	}
	
	void exit()
	{
		factory.exit();
	}
	
	void test() throws Throwable
	{
		Session session=factory.getCtiApi(url,"3331");
		
		//session.close();
		
		
		SimAgentStation sim=new SimAgentStation(session,"3331","3332");
		this.executor.execute(new RunTask(sim));
		
		//SimAgentStation sim2=new SimAgentStation(this.ctiApi,"3333","3334");
		//this.executor.execute(new RunTask(sim2));
		
		synchronized (this)
		{
			this.wait();
		}
	}
	

	public static void main(String[] args) throws Throwable
	{
		TestConnector test=new TestConnector();
		test.init();
		test.test();
		test.exit();
	}
}
