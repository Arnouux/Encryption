package main.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Utility {
	
	public static void writeString(String text, DataOutputStream outputStream) {
		byte[] bytes = text.getBytes();
		int size = bytes.length;
		try {
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(bytes[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeBytes(byte[] bytes, DataOutputStream outputStream) {
		int size = bytes.length;
		try {
			outputStream.writeInt(size);
			for (int i=0; i<size; i++) {
				outputStream.writeByte(bytes[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String readString(DataInputStream inputStream) {
		byte[] bytesText = null;
		try {
			int size = inputStream.readInt();
			bytesText = new byte[size];
			for(int i=0; i<size; i++) {
				bytesText[i] = inputStream.readByte();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(bytesText);
	}


	public static byte[] readBytes(DataInputStream inputStream) {
		byte[] bytesText = null;
		try {
			int size = inputStream.readInt();
			bytesText = new byte[size];
			for(int i=0; i<size; i++) {
				bytesText[i] = inputStream.readByte();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytesText;
	}
}
