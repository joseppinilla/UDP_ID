package com._78737146.eece411.A1;

import java.net.*;
import java.util.*;

/* UDP Client for new student ID code
 * Program creates data packet for UDP transmission using unique ID and checks reply for
 * Unique ID and payload containing secret code
 * */

class Assignment1UDP {
	
	public static final boolean DEV_MODE = false;
	public static final int HDR_SIZE = 16;
	//import static Assignment1UDP.DEV_MODE;
	
	public static void main(String args[]) throws Exception {		
		/* Parse and assign arguments
		 * i.e. java Assignment1UDP ip_address port student_ID [retry_timeout_ms]
		 * */
				
		InetAddress IPAddress = InetAddress.getByName("127.0.0.0");
		short port = 5627;
		int studentID = 909090;
		int timeOutms = 100;
		
		if (args.length > 0){
			try {

				IPAddress = InetAddress.getByName(args[0]);
				port = Short.parseShort(args[1]);
				studentID = Integer.parseInt(args[2]);
			
			}catch (UnknownHostException | NumberFormatException e) {
				System.err.println("Arguments not valid");
				System.err.println("Usage syntax: Assignment1UDP ip_address port student_ID [retry_timeout_ms]");
				System.exit(1);
			}
		}
		else{
			System.err.println("Using Default Arguments: 127.0.0.0 9867 909090");
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
	
		/* Fill-in UDP Packet
		 * Unique ID[16]: |hostIP3|...|hostIP0|Port1|Port0|Rand1|Rand0|miliSec7|...|miliSec0|
		 * Little-Endian
		 * 		1001			1000
		 * MSB		LSB		MSB		LSB 
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
		
		/* DEV: Test Host IP Address
		 */
		if(DEV_MODE){
			 InetAddress hostIPAddress = InetAddress.getByAddress(hostIP);
			 System.out.println(hostIPAddress.toString());
		}
		
		/* Add Payload
		 * Add integer (4 bytes) payload 
		 * */
		
		writeBytes(studentID,packetUDP,hdrPacket,4);
		hdrPacket+=4;
			
		DatagramSocket clientSocket = new DatagramSocket();
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
		String replyPacket;
		boolean matchReqRep = false;
		int retryCnt = 0;
		System.out.println("Sending ID: " + studentID);
		do{
			/*-------------------------Send-------------------------------*/
			DatagramPacket sendPacket = new DatagramPacket(packetUDP,hdrPacket, IPAddress, port);
			clientSocket.send(sendPacket);
			
			/*------------------------Receive-----------------------------*/
			clientSocket.setSoTimeout(timeOutms);
			try{
				clientSocket.receive(receivePacket);
			}
			catch(SocketTimeoutException e){
				if(DEV_MODE){
					System.err.println("Server not responding. Retrying(" + retryCnt +")...");
				}
			}
			replyPacket = StringUtils.byteArrayToHexString(receivePacket.getData(),hdrPacket+32);
			if(DEV_MODE){
				System.out.println("Packet is: ");
				System.out.println(StringUtils.byteArrayToHexString(packetUDP,hdrPacket));
				System.out.println("Reply is:");
				System.out.println(replyPacket);
			}
			
			/*---------------------Match Unique ID------------------------*/
			matchReqRep = replyPacket.startsWith(StringUtils.byteArrayToHexString(packetUDP,HDR_SIZE));
			retryCnt++;			
		}while(matchReqRep==false && retryCnt<4);

		clientSocket.close();
		
		if (matchReqRep){
			
			//readInt();
			
			int codeLength = ByteOrder.leb2int(receivePacket.getData(), HDR_SIZE);
						
			System.out.println("Secret code length: " + codeLength);		
			System.out.println("Secret: " + replyPacket.substring((HDR_SIZE+4)*2, (HDR_SIZE+4)*2+(codeLength*2)));
		}
		else{
			System.out.println("Server not responding");
		}
		
		//return matchReqRep;
	}
	
	public static void writeBytes(long dataIn, byte packet[], int hdrPacket, int bytes){	
		for(char i=0; i<bytes; i++){
			packet[hdrPacket++] = (byte)(dataIn>>(8*i));
        }		
	}
			
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
	  }
	 catch (  SocketException ex) {
		 System.out.println("Error getting IP");
	  }
	  return null;
	}
	 
	
	
}


