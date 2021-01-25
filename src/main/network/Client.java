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
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.ocsp.Signature;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import main.common.Protocol;

import java.lang.Thread;
import java.math.BigInteger;

public class Client extends Thread {
	
	private long id;
	private String name;
	private int port;
	private ServerSocket server;
	private String message = "a";
	
	public Client(long id, String name) {
		this.id = id;
		this.name = name;
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
			//outputStream.writeLong(this.id);
			
			// TODO server receive
			outputStream.writeInt(this.port);
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
				System.out.println("getKeyFromServer went KO");
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
			port = server.getLocalPort();
			System.out.println("Port used : " + port);
			outputStream.writeInt(port);
			
			// Client sending user name
			byte[] bytes = name.getBytes();
			int size = bytes.length;
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(bytes[i]);
			}
			

			socket.close();
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
			
			// Client sending public key
			Provider bcProvider = new BouncyCastleProvider();
			X500Name dnName = new X500Name("CN="+name+", OU=Hybrid, O=Hybrid, L=Hybrid, ST=FR, C=FR");
			long now = System.currentTimeMillis();
			Date startDate = new Date(now);
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(startDate);
		    calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity
		    Date endDate = calendar.getTime();
			BigInteger certSerialNumber = new BigInteger(Long.toString(now));
			JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, kp.getPublic());
		    ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(kp.getPrivate());
			X509CertificateHolder certHolder = certBuilder.build(contentSigner);
			X509Certificate cert = new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certHolder);
			byte[] certEncoded = cert.getEncoded();
//					
//			size = certEncoded.length;
//			outputStream.writeInt(size);
//			for (int i=0; i<size; i++) {
//				outputStream.writeByte(certEncoded[i]);
//			}
			
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			String b64publicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
			byte[] encoded = b64publicKey.getBytes();
			size = encoded.length;
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(encoded[i]);
			}
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(b64publicKey)));
			
			InputStream reader = socket.getInputStream();
			DataInputStream inputStream = new DataInputStream(reader);
			if(inputStream.readInt() == Protocol.OK) {
				System.out.println("OK");
				
				// Client saving private key in store.ks
				KeyStore ks = KeyStore.getInstance("JCEKS");
				ks.load(new FileInputStream("store.ks"),"abc123".toCharArray());
				Certificate[] certChain = new Certificate[1];
				certChain[0] = cert;
				ks.setKeyEntry("key_"+name, kp.getPrivate(), "abc123".toCharArray(), certChain);
				FileOutputStream fos = new FileOutputStream("store.ks");
				ks.store(fos, "abc123".toCharArray());
				/*
				 * keytool -list -keystore store.ks -storepass abc123
				 */
				this.name = name;
			} else {
				System.out.println("Register went KO");
			}
			socket.close();
		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | OperatorCreationException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Client " + this.getName() +" listening");
		try {
			server = new ServerSocket(0);
			connect(this.name);
			// TODO add if/else connected to server
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
			key = (RSAPrivateKey) ks.getKey("key_"+this.name, "abc123".toCharArray());
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
