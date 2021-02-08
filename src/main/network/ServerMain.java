package main.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import main.common.Protocol;

public class ServerMain extends Thread {
    static final int PORT = 8888;
	private MongoClient client;
	private MongoDatabase db;
	private Map<Server, Socket> servers;
	private boolean STOP = false;
	
	// map of connection
	private Map<String, Server> connections = new HashMap<String, Server>();
	public Map<String, Server> getConnections() {
		return connections;
	}
	
	public void stopServer() {
		this.STOP = true;
	}
	
	public void run() {
        ServerSocket serverSocket = null;
        Socket connection = null;
        
        // database access
		client = MongoClients.create("mongodb://127.0.0.1:27017/");
		db = client.getDatabase("keys");

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
			if(STOP) {
				break;
			}
            try {
            	connection = serverSocket.accept();
				InputStream reader = connection.getInputStream();
				DataInputStream inputStream = new DataInputStream(reader);
				
				int type = inputStream.readInt();
				switch(type) {
				case Protocol.REQ_CONNECT :
		            Server s = new Server(connection, db);
		            s.setServerMain(this);
					String name = s.doConnect(connection);
					connections.put(name, s);
		            s.start();
					break;
				default:
					System.out.println("Connection error (server side)");
					break;
				}
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            
        }
        try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}