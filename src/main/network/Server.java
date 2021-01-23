package main.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import main.common.Protocol;

import java.lang.Thread;


public class Server extends Thread {
	
	private Socket socket;
	private ServerSocket server;
	private String message;
	
	private boolean STOP = false;
	
	long senderId;
	long receiverId;
	
	public String getMessage() {
		return this.message;
	}

	public void stopServer() {
		this.STOP = true;
	}
	
	public void run() {
		message = "";
		try {
			server = new ServerSocket(8888);
			Socket connection = server.accept();
			System.out.println("Server listening");
			while(true) {
				if(STOP) {
					break;
				}
				if(connection.isClosed()) {
					System.out.println("Waiting for connection");
					connection = server.accept();
				}
				InputStream reader = connection.getInputStream();
				DataInputStream inputStream = new DataInputStream(reader);
				
				int type = inputStream.readInt();
				switch(type) {
				case Protocol.REQ_CONNECT :
					doConnect(connection);
					break;
				case Protocol.REQ_TEXT :
					doSendText(connection);
					break;
				case Protocol.REQ_PUBLIC_KEY:
					doGetPublicKey(connection);
					break;
				case Protocol.REQ_REGISTER :
					doRegister(connection);
					break;
				default:
					break;
				}
//				Thread.currentThread().interrupt();
//				break;
				
			}
			connection.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
//		System.out.println("Server stopped");
//		Thread.currentThread().interrupt();
	}
	
	
	private void doRegister(Socket connection) {
		// TODO Auto-generated method stub
		//client = MongoClients;
	}

	private void doConnect(Object connect) {
		// TODO Auto-generated method stub
		
	}

	
	public void doGetPublicKey(Socket connection) {
		try {
			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			receiverId = inputStream.readLong();
			
			socket = new Socket("localhost", (int) receiverId);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			outputStream.writeInt(Protocol.REQ_PUBLIC_KEY);


			reader = socket.getInputStream();
			inputStream = new DataInputStream(reader);

			int type = inputStream.readInt(); // Protocol
			if(type == Protocol.REPLY_PUBLIC_KEY) {
				int sizeMod = inputStream.readInt();
				byte[] bytesMod = new byte[sizeMod];
				for (int i=0; i<sizeMod; i++)
					bytesMod[i] = inputStream.readByte();
				int sizeExp = inputStream.readInt();
				byte[] bytesExp = new byte[sizeMod];
				for (int i=0; i<sizeExp; i++)
					bytesExp[i] = inputStream.readByte();
				writer = connection.getOutputStream();
				outputStream = new DataOutputStream(writer);
				outputStream.writeInt(Protocol.REPLY_PUBLIC_KEY);
				outputStream.writeInt(sizeMod);
				for(int i=0; i<sizeMod; i++)
					outputStream.writeByte(bytesMod[i]);
				outputStream.writeInt(sizeExp);
				for(int i=0; i<sizeMod; i++)
					outputStream.writeByte(bytesExp[i]);
			} else {
				System.out.println("Server received nothing");
				outputStream.writeInt(Protocol.KO);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void doSendText(Socket connection) {
		try {
			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			senderId = inputStream.readLong();
			receiverId = inputStream.readLong();
			int size = inputStream.readInt();
			byte[] bytes = new byte[size];
			for(int i=0; i<size; i++) {
				bytes[i] = inputStream.readByte();
			}
			
			System.out.println("Server reads : " + new String(bytes,StandardCharsets.UTF_8) +
							   " (from " + this.senderId + ")");
			socket = new Socket("localhost", (int) receiverId);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			outputStream.writeInt(Protocol.REPLY_TEXT);
			outputStream.writeInt(bytes.length);
			//byte[] bytes = message.getBytes();
			for(int i=0; i<size; i++) {
				outputStream.writeByte(bytes[i]);
			}
			//writer.close();
			this.message = new String(bytes);

			connection.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}
	
}
