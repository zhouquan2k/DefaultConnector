package app.ctiClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.testng.annotations.Test;


public class TestUDP {
	
	@Test
	 public void testClient() {  	
	
	        try {  
	        	byte[] buf = new byte[]{0X1E,0X40,0X40,0x34,0x31,0x32,0x33,0x20,0x30,0x33,0x34,0x35,0x36,0x01,0x32,0x30,0x35,0x38,0x01,0x35,0x38,0x39,0x30,0x01,0x30,0x37,0x30,0x38,0x01,0x31,0x1F};
	            InetAddress address = InetAddress.getByName("localhost");  //服务器地址  
	            int port = 8123;  //服务器的端口号  
	            //创建发送方的数据报信息  
	            DatagramPacket dataGramPacket = new DatagramPacket(buf, buf.length, address, port);  
	          
	            DatagramSocket socket = new DatagramSocket(8122);  //创建套接字  

	            socket.send(dataGramPacket);  //通过套接字发送数据  
	              
	            //接收服务器反馈数据  
	            byte[] backbuf = new byte[1024];  
	            DatagramPacket backPacket = new DatagramPacket(backbuf, backbuf.length);  
	            socket.receive(backPacket);  //接收返回数据  
	            String backMsg = new String(backbuf, 0, backPacket.getLength());  
	            System.out.println("服务器返回的数据为:" + backMsg);  
	              
	            socket.close();  
	              
	        } catch (UnknownHostException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
 
}
