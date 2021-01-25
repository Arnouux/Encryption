package main.network;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import main.common.Protocol;

import java.lang.Thread;
import java.math.BigInteger;


public class Server extends Thread {
	private MongoClient client;
	private MongoDatabase db;
	
	private HashMap<String, Integer> connectedByName;
	private HashMap<Integer, String> connectedByPort;
	
	public Server() {
		client = MongoClients.create("mongodb://127.0.0.1:27017/");
		db = client.getDatabase("keys");
		connectedByName = new HashMap<String, Integer>();
		connectedByPort = new HashMap<Integer, String>();
	}
	
	private Socket socket;
	private ServerSocket server;
	private String message;
	
	private boolean STOP = false;

	
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
		MongoCollection<Document> coll = db.getCollection("users");

		try {
			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			
			// Server receiving user name
			int size = inputStream.readInt();
			byte[] bytesName = new byte[size];
			for(int i=0; i<size; i++) {
				bytesName[i] = inputStream.readByte();
			}
			String name = new String(bytesName);

			System.out.println("Registering as " + name);
			// Server receiving public key
			size = inputStream.readInt();
			byte[] keyEncoded = new byte[size];
			for(int i=0; i<size; i++) {
				keyEncoded[i] = inputStream.readByte();
			}
			
			String b64key = new String(keyEncoded);
			
//			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//			X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(certEncoded));
//			
//			KeyStore ks = KeyStore.getInstance("JCEKS");
//			ks.load(new FileInputStream("storeServer.ks"),"abc123".toCharArray());
//			ks.setCertificateEntry(name, cert);
//			FileOutputStream fos = new FileOutputStream("storeServer.ks");
//			ks.store(fos, "abc123".toCharArray());
			/*
			 * keytool -list -keystore storeServer.ks -storepass abc123
			 */
			
			Document doc = new Document()
					.append("_id", new String(bytesName))
					.append("publicKey", b64key);
			
			OutputStream writer = connection.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			
			// Server sends info about registration
			if(coll.updateOne(eq("_id",name), combine(setOnInsert("_id", name), setOnInsert("publicKey", b64key)), new UpdateOptions().upsert(true)).getUpsertedId() != null) {
				outputStream.writeInt(Protocol.OK);
			} else {
				outputStream.writeInt(Protocol.KO);
			}
		
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doConnect(Socket connection) {
		InputStream reader;
		try {
			reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			int port = inputStream.readInt();
			
			// Server receiving user name
			int size = inputStream.readInt();
			byte[] bytesName = new byte[size];
			for(int i=0; i<size; i++) {
				bytesName[i] = inputStream.readByte();
			}
			String name = new String(bytesName);
			
			connectedByName.put(name, port);
			connectedByPort.put(port, name);
			
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void doGetPublicKey(Socket connection) {
		try {
			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			
			// Server receiving target's name
			int nameSize = inputStream.readInt();
			byte[] nameBytes = new byte[nameSize];
			for(int i=0; i<nameSize; i++) {
				nameBytes[i] = inputStream.readByte();
			}
			String nameTarget = new String(nameBytes);
			
			// Search in storeServer.ks for key
//			KeyStore ks = KeyStore.getInstance("JCEKS");
//			ks.load(new FileInputStream("storeServer.ks"),"abc123".toCharArray());
//	        Enumeration<String> aliases = ks.aliases();
//	        RSAPublicKey key = null;
//	        while(aliases.hasMoreElements()) {
//	            String alias = aliases.nextElement();
//	            if (alias.contentEquals(nameTarget) && ks.isCertificateEntry(alias)) {
//	    			key = (RSAPublicKey) ks.getCertificate(nameTarget).getPublicKey();
//
//	    			//key = (RSAPublicKey) cert.getPublicKey();
//	            }
//	        }

			MongoCollection<Document> coll = db.getCollection("users");
			Document doc = coll.find(eq("_id",nameTarget)).first();
			String b64key = (String) doc.get("publicKey");

			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(b64key)));

			// Server sending key
			OutputStream writer = connection.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			if (publicKey != null) {
				outputStream.writeInt(Protocol.REPLY_PUBLIC_KEY);
				byte[] modulus = publicKey.getModulus().toByteArray();
				outputStream.writeInt(modulus.length);
				for(int i=0; i<modulus.length; i++)
					outputStream.writeByte(modulus[i]);
				byte[] exp = publicKey.getPublicExponent().toByteArray();
				outputStream.writeInt(exp.length);
				for(int i=0; i<exp.length; i++)
					outputStream.writeByte(exp[i]);
			} else {
				outputStream.writeInt(Protocol.KO);
			}


		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void doSendText(Socket connection) {
		try {
			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			//senderId = inputStream.readLong();
			//receiverId = inputStream.readLong();
			int portSender = inputStream.readInt();
			
			// Server reading target name
			int nameSize = inputStream.readInt();
			byte[] nameBytes = new byte[nameSize];
			for(int i=0; i<nameSize; i++) {
				nameBytes[i] = inputStream.readByte();
			}
			String nameTarget = new String(nameBytes);
			
			int size = inputStream.readInt();
			byte[] bytes = new byte[size];
			for(int i=0; i<size; i++) {
				bytes[i] = inputStream.readByte();
			}
			
			System.out.println("Server reads : " + new String(bytes,StandardCharsets.UTF_8) +
							   " (from " + this.connectedByPort.get(portSender) + ")");
			socket = new Socket("localhost", this.connectedByName.get(nameTarget));
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
