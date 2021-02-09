package main.app;

import java.util.concurrent.TimeUnit;

import main.network.Client;
import main.network.Server;
import main.network.ServerMain;
import main.ui.ClientUI;

public class App {
	
	public ServerMain server;
	public Client clientSender;
	public Client clientReceiver;
	public Client clientStranger;
	
	public void start() {
		server = new ServerMain();
		clientSender = new Client("user1");
		clientReceiver = new Client("user2");
		clientStranger = new Client("user3");


		
//		server.run();
//		clientReceiver.run();
//		clientSender.run();
		new Thread(server).start();
		
//		clientSender.register("user1");
//		clientReceiver.register("user2");
		
//		new Thread(clientSender).start();
		ClientUI ui = new ClientUI("user1");
		String[] user = new String[1];
		user[0] = "user1";
		ui.main(user);
		new Thread(clientReceiver).start();
//		clientSender.addContact("user2");
//		clientReceiver.addContact("user1");
		
//		// SLEEP
//		try {
//			TimeUnit.SECONDS.sleep(2);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		// SLEEP
//		try {
//			TimeUnit.SECONDS.sleep(5);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("CONTACTS LIST");
//		clientSender.getContacts();
//		System.out.println("END CONTACTS LIST");
//
//		
//		clientSender.send("user2", "salut de 0");
//		
//		clientReceiver.send("user1", "et là ça marche tjours ?");
//		clientSender.send("user2", "salut de 0 v22222");
		//clientStranger.send("user2", "salut de stranger");
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
