package main.app;

import main.network.Client;
import main.network.Server;

public class App {
	
	public Server server;
	public Client clientSender;
	public Client clientReceiver;
	
	public void start() {
		server = new Server();
		clientSender = new Client(0, 7000);
		clientReceiver = new Client(1, 7001);

//		server.run();
//		clientReceiver.run();
//		clientSender.run();
		new Thread(server).start();
		new Thread(clientSender).start();
		new Thread(clientReceiver).start();
		
		clientSender.register("user1");
		clientReceiver.register("user2");
		
		clientSender.send("user2", "salut de 0");
		clientReceiver.send(7000, "et là ça marche tjours ?");
		//clientReceiver.stopClient();
		System.out.println("--------------------");
		clientSender.send(7001, "salut de 0 une 2e fois");
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
