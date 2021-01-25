package main.common;

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
}
