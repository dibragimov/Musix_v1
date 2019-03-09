package il.co.pelephone.musix.UI.MediaPlayer;

import il.co.pelephone.musix.UI.utility.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class Decrypter {
	
	private boolean isFirst; ////to detect whether it is first byte (padding) or not
	
	private byte secretKey0;
    private byte secretKey1;
    private byte feedBack0;
    private byte feedBack1;
	
	String TAG="Decrypter";
	
	/*
	 * init() method initializes the class with the encryption key
	 * the length of the encryption key is 4 bytes 
	 * the encryption key is 0342
	 * 
	 * when new file needs to be decrypted one should call init method
	 */
	public void init(String decryptionKey){
		byte[] decr = decryptionKey.getBytes();
		Log.d(TAG, "length of the byte array: "+decr.length);
		
		isFirst = true;
		
		byte[] tmp = decryptionKey.getBytes();

        secretKey0 = tmp[0];
        secretKey1 = tmp[1];
        feedBack0 = tmp[2];
        feedBack1 = tmp[3];
	}
	
	/*
	 * helper method to decrypt a file and store the decrypted content to a new file
	 */
	public File decryptToFile(File chunkFile){
		File newTempFile = new File(new File(chunkFile.getParent()), chunkFile.getName()+Constants.Strings.DECRYPTED_PART);
		byte[] content = decryptOld(chunkFile);
		if(content != null){
			try {
				FileOutputStream fos = new FileOutputStream(newTempFile);
				fos.write(content);
				fos.flush();
				fos.close();
				Log.d(TAG, "decrypt to File. length: "+newTempFile.length());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(newTempFile.exists()){
			return newTempFile;
		}
		return null;
	}
	
	public File decryptToFile(File chunkFile, File destinationFile){
		File newTempFile = destinationFile; //new File(new File(chunkFile.getParent()), chunkFile.getName()+Constants.Strings.DECRYPTED_PART);
//		byte[] content = decryptOld(chunkFile);
//		if(content != null){
			try {
				int totalBytesRead = 0;
				byte[] b = new byte[4096];
				FileOutputStream fos = new FileOutputStream(newTempFile);
				FileInputStream fis = new FileInputStream(chunkFile);
				int len = fis.available();
				Log.d(TAG, "file length = "+len);
				int padding = 0;
				if(isFirst){
					padding =  fis.read();
					isFirst = false;
					len--;
				}
				
				while( len-totalBytesRead>0 ){
					int z = 0;
					if(len-totalBytesRead > 4096)
						z=4096;
					else if(len-totalBytesRead > 0 && len-totalBytesRead < 4096)
						z= len - totalBytesRead;
					//z = len;//(len - (len % 2));
//					Log.d(TAG, "z = "+z+" length of the file: "+len+" total bytes read: "+totalBytesRead);
		            int n = fis.read(b, 0, z);
		
		            // decrypt buffer
		            // ---------------
		            byte s0 = secretKey0;
		            byte s1 = secretKey1;
		            byte f0 = 0;
		            byte f1 = 0;
		
		            if (n>2) {
		                f0 = b[n-2];
		                f1 = b[n-1];
		            }
		            //off = off + 1;
		            byte a0 = b[0];
		            byte a1 = b[1];
		
		            try {
		            for (int i = n-2;;i-=2) {
		                b[i] ^= (s0 ^ b[i-2]);
		                b[i+1] ^= (s1 ^ b[i-1]);
		            }
		            } catch (Exception e){
		            }
		
		            b[0] = a0;
		            b[1] = a1;
		
		            b[0] ^= (s0 ^ feedBack0);
		            b[1] ^= (s1 ^ feedBack1);
		
		            feedBack0 = f0;
		            feedBack1 = f1;
		
		            
		            if (n < z&& n!=-1) { // last
		                n-=padding;
		            }
		            //System.arraycopy(b, 0, decryptedBytes, totalBytesRead, n);
		            fos.write(b);
		            totalBytesRead += n;
				}
				////----end of the following sneppet is taken from EncryptedPlayer.java file
				fis.close();
				
				
				fos.flush();
				fos.close();
				Log.d(TAG, "decrypt to File. length: "+newTempFile.length());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
		if(newTempFile.exists()){
			return newTempFile;
		}
		return null;
	}
	
	/*
	 * the method decrypts the file (part of file)
	 */
	public byte[] decryptOld(File chunkFile){
		byte[] b = new byte[4096];
		byte[] decryptedBytes = null;
		int totalBytesRead = 0;
		FileInputStream str;
		try {
			str = new FileInputStream(chunkFile);
			int len = str.available();
			Log.d(TAG, "file length = "+len);
			int padding = 0;
			if(isFirst){
				decryptedBytes = new byte[len-1];
				padding =  str.read();
				isFirst = false;
				len--;
			}
			else
				decryptedBytes = new byte[len];
			
			////----the following sneppet is taken from EncryptedPlayer.java file
			while( len-totalBytesRead>0 ){
				int z = 0;
				if(len-totalBytesRead > 4096)
					z=4096;
				else if(len-totalBytesRead > 0 && len-totalBytesRead < 4096)
					z= len - totalBytesRead;
				//z = len;//(len - (len % 2));
//				Log.d(TAG, "z = "+z+" length of the file: "+len+" total bytes read: "+totalBytesRead);
	            int n = str.read(b, 0, z);
	
	            // decrypt buffer
	            // ---------------
	            byte s0 = secretKey0;
	            byte s1 = secretKey1;
	            byte f0 = 0;
	            byte f1 = 0;
	
	            if (n>2) {
	                f0 = b[n-2];
	                f1 = b[n-1];
	            }
	            //off = off + 1;
	            byte a0 = b[0];
	            byte a1 = b[1];
	
	            try {
	            for (int i = n-2;;i-=2) {
	                b[i] ^= (s0 ^ b[i-2]);
	                b[i+1] ^= (s1 ^ b[i-1]);
	            }
	            } catch (Exception e){
	            }
	
	            b[0] = a0;
	            b[1] = a1;
	
	            b[0] ^= (s0 ^ feedBack0);
	            b[1] ^= (s1 ^ feedBack1);
	
	            feedBack0 = f0;
	            feedBack1 = f1;
	
	            
	            if (n < z&& n!=-1) { // last
	                n-=padding;
	            }
	            System.arraycopy(b, 0, decryptedBytes, totalBytesRead, n);
	            totalBytesRead += n;
			}
			////----end of the following sneppet is taken from EncryptedPlayer.java file
			str.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return decryptedBytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return decryptedBytes;
		}
		Log.d(TAG, "chunk decrypted ");
		return decryptedBytes;
	}
}
