package com._78737146.eece411.A1;

import java.net.*;
import java.util.*;

class Assignment1UDP {
	public static void main(String args[]) throws Exception {		
		/* Parse and assign args
		 * java Assignment1UDP <ip.string.of.server> <port_num> <ID>
		 * */
				
		InetAddress IPAddress = InetAddress.getByName("127.0.0.0");
		short port = 9876;
		int studentID = 909090;
		int timeOutms = 100;
		
		if (args.length > 0){
			try {

				IPAddress = InetAddress.getByName(args[0]);
				port = Short.parseShort(args[1]); //9876 , 5627
				studentID = Integer.parseInt(args[2]);
			
			}catch (UnknownHostException | NumberFormatException e) {
				System.err.println("Arguments not valid");
				System.err.println("Usage syntax: Assignment1UDP ip_address port student_ID [retry_timeout_ms]");
				System.exit(1);
			}
		}
		else{
			System.err.println("Arguments missing");
			System.exit(1);
		}
		
		if (args.length==4){
			try {
				timeOutms = Integer.parseInt(args[3]);
			}
			catch (NumberFormatException e) {
				System.err.println("Timeout value not valid");
				System.err.println("Usage syntax: Assignment1UDP ip_address port student_ID [retry_timeout_ms]");
				System.exit(1);
			}
		}
		
		
		System.out.println(args.length);
		System.out.println(IPAddress.toString()); //DEBUG
		System.out.println("Sending ID: " + studentID);
					
		
		/* FILL-IN UDP PACKET
		 * 
		 * Unique ID[16]: |hostIP3|...|hostIP0|Port1|Port0|Rand1|Rand0|miliSec7|...|miliSec0|
		 * 
		 * Little-Endian
		 * 		1001			1000
		 * MSB		LSB		MSB		LSB
		 * 
		*/
	
		byte[] packetUDP = new byte[1024];
		int hdrPacket = 0;
		
		byte[] hostIP = getHostIP().getAddress();
		packetUDP[0] = hostIP[3];
		packetUDP[1] = hostIP[2];
		packetUDP[2] = hostIP[1];
		packetUDP[3] = hostIP[0];
		hdrPacket+=4;
		
		writeBytes(port,packetUDP,hdrPacket,2);
		hdrPacket+=2;
		
		Random rand = new Random();
		writeBytes((short)rand.nextInt(65535),packetUDP,hdrPacket,2);
		hdrPacket+=2;
		
		writeBytes(System.currentTimeMillis(),packetUDP,hdrPacket,8);
		hdrPacket+=8;
		
		/* ----Test Host IP Address
		 * InetAddress hostIPAddress = InetAddress.getByAddress(hostIP);
		 * System.out.println(hostIPAddress.toString());
		 */
		
		/* Add Payload
		 * 
		 * */
		
		writeBytes(studentID,packetUDP,hdrPacket,4);
		hdrPacket+=4;

		System.out.println("Header of Packet " + hdrPacket);
		
		System.out.println("Packet is: ");
		System.out.println(StringUtils.byteArrayToHexString(packetUDP,hdrPacket));
		
		
		DatagramSocket clientSocket = new DatagramSocket();
		boolean matchReqRep = false;
		int retryCnt = 0;
		do{
			/*-------------------------Send-------------------------------*/
			DatagramPacket sendPacket = new DatagramPacket(packetUDP,hdrPacket, IPAddress, port);
			clientSocket.send(sendPacket);
			
			/*------------------------Receive-----------------------------*/
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
			clientSocket.setSoTimeout(timeOutms);
			clientSocket.receive(receivePacket);
			String replyPacket = StringUtils.byteArrayToHexString(receivePacket.getData(),hdrPacket+32);
			System.out.println("FROM SERVER:"); //DEBUG
			System.out.println(replyPacket); //DEBUG
			
			/*-------------------------Match-----------------------------*/
			matchReqRep = replyPacket.startsWith(StringUtils.byteArrayToHexString(packetUDP,16));
			retryCnt++;
		}while(matchReqRep==false && retryCnt<3);
				
		clientSocket.close();
		
		//return matchReqRep;
	}
	
	public static void writeBytes(long dataIn, byte packet[], int hdrPacket, int bytes){	
		for(char i=0; i<bytes; i++){
			packet[hdrPacket++] = (byte)(dataIn>>(8*i));
        }		
	}
	
	/** 
	 * @return the IP address of the device.
	 * From project Android-Activity-Tracker
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
	  }
	 catch (  SocketException ex) {
		 System.out.println("Error getting IP");
	  }
	  return null;
	}
	 
	
	
}


