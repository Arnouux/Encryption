package main.app;

import java.util.concurrent.TimeUnit;

import main.network.Client;
import main.network.Server;

public class App {
	
	public Server server;
	public Client clientSender;
	public Client clientReceiver;
	public Client clientStranger;
	
	public void start() {
		server = new Server();
		clientSender = new Client(0, "user1");
		clientReceiver = new Client(1, "user2");;
		clientStranger = new Client(2, "user3");

//		server.run();
//		clientReceiver.run();
//		clientSender.run();
		new Thread(server).start();
		
		clientSender.register("user1");
		clientReceiver.register("user2");
		clientSender.addContact("user2");
		clientReceiver.addContact("user1");
		
//		// SLEEP
//		try {
//			TimeUnit.SECONDS.sleep(2);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		new Thread(clientSender).start();
		new Thread(clientReceiver).start();
		
		// SLEEP
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		clientSender.send("user2", "salut de 0");
		clientReceiver.send("user1", "et là ça marche tjours ?");
		//clientReceiver.stopClient();
		clientStranger.send("user2", "salut de stranger");
		//clientReceiver = new Client(1, 7001);
		//clientSender.stopClient();
		//server.stopServer();
		//clientReceiver.stopClient();
	}
	
	public static void main(String[] args) {
		App app = new App();
		app.start();
		//clientReceiver.send(7000, "coucou de 1");
		//new Thread(clientReceiver).start();
//		server.run();
//		client.run();
		//System.out.println(server.getMessage());
		//System.out.println(Thread.activeCount());
		
	}
}
