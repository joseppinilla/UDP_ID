package com._78737146.eece411.A1;

import java.net.*;

/* UDP Test Server
 * Program acts as server and echoes UDP packets
 * Server never closes, testing only.
 * */

class A1ServerTest {
	public static void main(String args[]) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(5627);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = StringUtils.byteArrayToHexString(receivePacket.getData(),16);
			System.out.println("Message In: " + sentence);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}
	}
}
