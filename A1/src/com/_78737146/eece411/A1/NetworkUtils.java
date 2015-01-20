package com._78737146.eece411.A1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;

public class NetworkUtils {

	
	/** 
	 * Read list of Network Interfaces and return device IPv4
	 * From project Android-Activity-Tracker.
	 * 
	 * @return the IP address of the device. 
	 */
	public static InetAddress getHostIP(){
		try {
			for (Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf=en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr=intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress=enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						return inetAddress;
					}
				}
			}
		} catch (  SocketException ex) {
			System.err.println("Error getting IP");
		}
		return null;
	}	
	
	/** 
	 * Fill-in UDP Packet
	 * Unique ID[16]: |hostIP3|...|hostIP0|Port1|Port0|Rand1|Rand0|miliSec7|...|miliSec0|
	 * Little-Endian
	 * 		1001			1000
	 * MSB		LSB		MSB		LSB
	 * 
	 *  
	 * @return int hdrPacket pointing to packet header (how many bytes written) 
	 *  
	*/
	public static int genUniqueID(byte packet[], short port){		
		
		int hdrPacket = 0;
		byte[] hostIP = NetworkUtils.getHostIP().getAddress();
		packet[0] = hostIP[3];
		packet[1] = hostIP[2];
		packet[2] = hostIP[1];
		packet[3] = hostIP[0];
		hdrPacket+=4;
		
		ByteOrder.writeBytes(port,packet,hdrPacket,2);
		hdrPacket+=2;
		
		Random rand = new Random();
		ByteOrder.writeBytes((short)rand.nextInt(65535),packet,hdrPacket,2);
		hdrPacket+=2;
		
		ByteOrder.writeBytes(System.currentTimeMillis(),packet,hdrPacket,8);
		hdrPacket+=8;
		
		if(A1Client.DEV_MODE){
			 InetAddress hostIPAddress;
			try {
				hostIPAddress = InetAddress.getByAddress(hostIP);
			} catch (UnknownHostException e) {
				System.err.println("Host IP format error");
			}
			 System.out.println(hostIPAddress.toString());
		}
		
		
		return hdrPacket;
		
	}
	
	/** 
	 * Fill-in UDP Packet
	 * Unique ID[16]: |hostIP3|...|hostIP0|Port1|Port0|Rand1|Rand0|miliSec7|...|miliSec0|
	 * Little-Endian
	 * 		1001			1000
	 * MSB		LSB		MSB		LSB
	 * 
	 * @param reply byte array for server reply
	 * @param packet byte array for client request
	 * @param IPAddress server IP
	 * @param port socket port
	 * @param size of packet
	 * @param timeOutms configurable retry timeout in miliseconds
	 * @return int hdrPacket pointing to packet header (how many bytes written) 
	 *  
	*/
	public static boolean packetSend(byte reply[], byte packet[], InetAddress IPAddress, short port, int lengthPacket, int timeOutms){		

		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e1) {
			System.err.println("Failed Opening Socket");
			System.exit(1);
		}
		DatagramPacket receivePacket = new DatagramPacket(reply,reply.length);
		String replyPacket;
		boolean matchReqRep = false;
		int retryCnt = 0;
		do{
			/*-------------------------Send-------------------------------*/
			DatagramPacket sendPacket = new DatagramPacket(packet,lengthPacket, IPAddress, port);
			try {
				clientSocket.send(sendPacket);
			} catch (IOException e1) {
				System.err.println("Failed Sending Packet");
				System.exit(1);
			}
			
			/*------------------------Receive-----------------------------*/
			try {
				clientSocket.setSoTimeout(timeOutms);
			} catch (SocketException e1) {
				System.err.println("Failure Setting Socket");
				System.exit(1);
			}
			
			timeOutms *= 2;
			
			try{
				clientSocket.receive(receivePacket);

			} catch(IOException e){
				if(A1Client.DEV_MODE){
					System.err.println("Server not responding. Retrying(" + retryCnt +")...");
				}
			}
			replyPacket = StringUtils.byteArrayToHexString(receivePacket.getData(),A1Client.HDR_SIZE);
			if(A1Client.DEV_MODE){
				System.out.println("Packet is: ");
				System.out.println(StringUtils.byteArrayToHexString(packet,lengthPacket));
				System.out.println("Reply is:");
				System.out.println(replyPacket);
			}
			
			/*---------------------Match Unique ID------------------------*/
			matchReqRep = replyPacket.equals(StringUtils.byteArrayToHexString(packet,A1Client.HDR_SIZE));
			retryCnt++;			
		}while(matchReqRep==false && retryCnt<4);

		clientSocket.close();
		
		return matchReqRep;	
	}
}
