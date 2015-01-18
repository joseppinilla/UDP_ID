package com._78737146.eece411.A1;

import java.net.*;

/* UDP Client for new student ID code
 * Program creates data packet for UDP transmission using unique ID and checks reply for
 * Unique ID and payload containing secret code
 * */

class A1Client {
	
	public static final boolean DEV_MODE = false;
	public static final int HDR_SIZE = 16;
	
	public static void main(String args[]) throws Exception {		
		/* Parse and assign arguments
		 * i.e. java A1Client ip_address port student_ID [retry_timeout_ms]
		 * */
				
		InetAddress serverIP = InetAddress.getByName("127.0.0.0");
		short port = 5627;
		int studentID = 909090;
		int timeOutms = 100;
		
		if (args.length > 0){
			try {
				serverIP = InetAddress.getByName(args[0]);
				port = Short.parseShort(args[1]);
				studentID = Integer.parseInt(args[2]);		
			}catch (UnknownHostException | NumberFormatException e) {
				System.err.println("Arguments not valid");
				System.err.println("Usage syntax: A1Client ip_address port student_ID [retry_timeout_ms]");
				System.exit(1);
			}
		}
		else{
			System.out.println("Using Default Arguments: 127.0.0.0 9867 909090");
		}
		
		if (args.length==4){
			try {
				timeOutms = Integer.parseInt(args[3]);
			}
			catch (NumberFormatException e) {
				System.err.println("Timeout value not valid");
				System.err.println("Usage syntax: A1Client ip_address port student_ID [retry_timeout_ms]");
				System.exit(1);
			}
		}
	
		/* Application Layer creates packet and fills with UniqueUD
		 * Size of UniqueID may change, getUniqueID returns position of packet header 
		 * */
		
		byte[] requestUDP = new byte[1024];
		int indexPacket = NetworkUtils.genUniqueID(requestUDP,port);
					
		/* Add Payload
		 * Add integer (4 bytes) payload
		 * Manual change to packet index for this exercise (+=4)
		 * For complex payloads return int indexPacket with added offset 
		 * */
		
		System.out.println("Sending ID: " + studentID);
		ByteOrder.writeBytes(studentID,requestUDP,indexPacket,4);
		indexPacket+=4;
		
		/* UDP Send and Receive
		 * Transparency from Application, only end result is visible
		 * or failed comm
		 * offset for code length byte (+4) manual
		 * TODO: Automate offset of response header (code length=4) for more complex exercises  
		 * */
		byte[] replyUDP = new byte[1024];
		if (!NetworkUtils.packetSend(replyUDP, requestUDP, serverIP, port, indexPacket, timeOutms)){
			System.err.println("Communication Failed");
		}	
		else{

			int codeLength = ByteOrder.leb2int(replyUDP, HDR_SIZE);
			System.out.println("Secret code length: " + codeLength);
			String secretCode = StringUtils.byteArrayToHexString(replyUDP,(HDR_SIZE+codeLength+4));
			System.out.println("Secret: " + secretCode.substring((HDR_SIZE+4)*2, (HDR_SIZE+4)*2+(codeLength*2)));
		}
		
	}	
}


