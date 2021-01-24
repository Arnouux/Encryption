package main.network;

import java.io.BufferedInputStream;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import main.common.Protocol;

import java.lang.Thread;
import java.math.BigInteger;

public class Client extends Thread {
	
	private long id;
	private int port;
	private Socket socket;
	private ServerSocket server;
	private String message = "a";
	
	public Client(long id, int port) {
		this.id = id;
		this.port = port;
	}
	
	public void send(String targetName, String text) {
		try {
//			if(server != null) {
//				server.close();
//			}
			
			Socket socket = new Socket("localhost", 8888);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			
			RSAPublicKey key = getPublicKeyFromServer(targetName, socket);
			byte[] encrypted = null;
			Cipher cipher = null;
			//text = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_16);
			try {
				cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, key);
				encrypted = cipher.doFinal(text.getBytes());
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CipherOutputStream cipherOutputStream = new CipherOutputStream(writer, cipher);

			System.out.println("Client " + this.getName() +" writes : " + text);
			outputStream.writeInt(Protocol.REQ_TEXT);
			outputStream.writeLong(this.id);
			
			// TODO server receive
			byte[] targetNameBytes = targetName.getBytes();
			outputStream.writeInt(targetNameBytes.length);
			for(int i=0; i<targetNameBytes.length; i++) {
				outputStream.writeByte(targetNameBytes[i]);
			}
			outputStream.writeInt(encrypted.length);
			for(int i=0; i<encrypted.length; i++) {
				outputStream.writeByte(encrypted[i]);
			}
			socket.close();
//			System.out.println("Client " + this.getName() + " stopped");
//			Thread.currentThread().interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private RSAPublicKey getPublicKeyFromServer(String targetName, Socket socket) {
		RSAPublicKey keyTarget = null;
		try {
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			outputStream.writeInt(Protocol.REQ_PUBLIC_KEY);
			
			// TODO server receive
			byte[] targetNameBytes = targetName.getBytes();
			outputStream.writeInt(targetNameBytes.length);
			for(int i=0; i<targetNameBytes.length; i++) {
				outputStream.writeByte(targetNameBytes[i]);
			}
			
			InputStream reader = socket.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);

			int type = inputStream.readInt();
			switch(type) {
			case Protocol.REPLY_PUBLIC_KEY:
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				int sizeMod = inputStream.readInt();
				byte[] bytesMod = new byte[sizeMod];
				for(int i=0; i<sizeMod; i++) {
					bytesMod[i] = inputStream.readByte();
				}
				int sizeExp = inputStream.readInt();
				byte[] bytesExp = new byte[sizeExp];
				for(int i=0; i<sizeExp; i++) {
					bytesExp[i] = inputStream.readByte();
				}
				RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(bytesMod), new BigInteger(bytesExp));
				keyTarget = (RSAPublicKey) keyFactory.generatePublic(keySpec);
				break;
			case Protocol.KO:
				System.out.println("KO");
				break;
			}	
			//reader.close();
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return keyTarget;
		
	}
	
	public void sendPublicKey(Socket socket) {
		try {
			//socket = new Socket("localhost", 8888);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			
			outputStream.writeInt(Protocol.REPLY_PUBLIC_KEY);
			
			// TODO get key and send
			RSAPublicKey key = null;
			try {
				KeyStore ks = KeyStore.getInstance("JCEKS");
				ks.load(new FileInputStream("store.ks"),"abc123".toCharArray());
				key = (RSAPublicKey) ks.getCertificate("key1").getPublicKey();
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] modulus = key.getModulus().toByteArray();
			outputStream.writeInt(modulus.length);
			for(int i=0; i<modulus.length; i++)
				outputStream.writeByte(modulus[i]);
			byte[] exp = key.getPublicExponent().toByteArray();
			outputStream.writeInt(exp.length);
			for(int i=0; i<exp.length; i++)
				outputStream.writeByte(exp[i]);
			
			writer.close();
			//socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean STOP = false;
	public void stopClient() {
		this.STOP = true;
	}
	
	public void connect(String name) {
		try {
			Socket socket = new Socket("localhost", 8888);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			
			outputStream.writeInt(Protocol.REQ_CONNECT);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void register(String name) {
		try {
			Socket socket = new Socket("localhost", 8888);
			OutputStream writer = socket.getOutputStream();
			DataOutputStream outputStream = new DataOutputStream(writer);
			outputStream.writeInt(Protocol.REQ_REGISTER);
			
			// Client sending user name
			byte[] bytes = name.getBytes();
			int size = bytes.length;
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(bytes[i]);
			}
			
			// Client generating new key pair
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();
			byte[] ePubKey = kp.getPublic().getEncoded();
			byte[] ePriKey = kp.getPrivate().getEncoded();
			
			// Client saving private key in store.ks
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream("store.ks"),"abc123".toCharArray());
			ks.setKeyEntry("key_"+name, ePriKey, null);
			FileOutputStream fos = new FileOutputStream("store.ks");
			ks.store(fos, "abc123".toCharArray());
			/*
			 * keytool -list -keystore store.ks -storepass abc123
			 */
			
			// Client sending public key
			size = ePubKey.length;
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(ePubKey[i]);
			}
			
			InputStream reader = socket.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			if(inputStream.readInt() == Protocol.OK) {
				System.out.println("OK");
			} else {
				System.out.println("KO");
			}
			socket.close();
		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Client " + this.getName() +" listening");
		try {
			server = new ServerSocket(this.port);
			while(true) {
				if(STOP) {
					break;
				}
				Socket connection = server.accept();
				InputStream reader = connection.getInputStream();
				DataInputStream inputStream = new DataInputStream(reader);
				
				int type = inputStream.readInt();
				switch(type) {
				case Protocol.REPLY_TEXT :
					getText(connection);
					break;
				case Protocol.REQ_PUBLIC_KEY :
					sendPublicKey(connection);
					break;
				default:
					break;
				}
				connection.close();
				//reader.close();
//				outputStream.close();
//				inputStream.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Client " + this.getName() + " stopped");
		//Thread.currentThread().interrupt();
	}
	
	public void getText(Socket connection) {
		try {
			RSAPrivateKey key = null;
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream("store.ks"),"abc123".toCharArray());
			key = (RSAPrivateKey) ks.getKey("key1", "abc123".toCharArray());

			InputStream reader = connection.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			
			int size = inputStream.readInt();
			byte[] bytes = new byte[size];
			for(int i=0; i<size; i++) {
				bytes[i] = inputStream.readByte();
			}

	        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
	        cipher.init(Cipher.DECRYPT_MODE, key);
	        this.message = new String(cipher.doFinal(bytes));

			System.out.println("Client " +
					this.getName()+ " reads : " + this.message);
			//this.message = "";
			//Thread.currentThread().interrupt();
			//break;
			
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public static void main(String[] args) {
//		Client client = new Client();
//		client.run();
	}
}
