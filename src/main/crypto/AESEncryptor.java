package main.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

import base.Constants;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class AESEncryptor {
	
    public static void main(String[] args) throws Exception {
    		String key = Constants.Enryption.KEY_VALUE_1;
    		
    		byte b[] = CBCEncryptByte("hello",key);
    		String ciphertext = new String(b, "UTF-8");
    		System.out.println("original : " + b.length);
    		System.out.println("original : " + ciphertext.getBytes("UTF-8").length);
    		
    		//System.out.println(CBCDecryptByte(ciphertext.getBytes(), key));
    		
    		//System.out.println(CBCDecryptByte(ciphertext.getBytes(), key));
//    		String parts[] = "000001094¥0000016416".split("¥");
//    		System.out.println(parts.length);
//    		RandomAccessFile file = new RandomAccessFile("output/cloud2/data.txt", "rw");
//    		RandomAccessFile file = new RandomAccessFile("output/cloud1/ltuples/101.txt", "rw");
//    		byte[] b = new byte[48]; // binary data size
//    		byte[] b = new byte[64]; // binary ltuple size
//    		long len = file.length();
//    		System.out.println("File len = " + len);
//    		for (int i = 0; i < 100; i++) {
//    			//file.seek(0);
//        		file.read(b);
//        		System.out.println(i + ":" + CBCDecryptByte(b, Constants.Enryption.KEY_VALUE_2));
////        		System.out.println(i + ":" + Base64.encodeBase64String(b));
//        		len = file.getFilePointer();
//    		}
//    		file.close();
//		System.out.println("00001;2015-01-01 00:00:00;1.39".length());
//    		String clean = "00001;2015-01-01 00:00:00;1.39";
//    		System.out.println("Len : " + clean.length());
//    		//String cipherText = CBCEncrypt(clean, key);
//		String cipherText = "lef2J0G6Oae80swtfl9Jjzt1UgDFGaSzenE0lBFg4klQHNvrZQXxfy8ma4/OPnHB";
//        System.out.println("Len : " + Base64.decodeBase64(cipherText).length);
//        System.out.println(CBCDecrypt(cipherText, key));
//        // write/read bytes to file
//        OutputStream os = new FileOutputStream("output/test.txt"); 
//	    // Starts writing the bytes in file
//	    os.write(encrypted); 
//	    os.close();
//	    byte[] array = Files.readAllBytes(Paths.get("output/test.txt"));
//        System.out.println(array.length);

//        System.out.println(CBCDecryptByte(array, key));
        // size of ciphertexts
//        int output_size = clean.length() + (16 - (clean.length() % 16)) + 16;
//        System.out.println(output_size);
    }
	
    public static byte[] CBCEncryptByte(String plainText, String key) throws Exception {
        byte[] clean = plainText.getBytes();
        // Generating IV
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);
        
        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);
        //System.out.println(Base64.encodeBase64String(encryptedIVAndText) + "\n");
        return encryptedIVAndText;
    }
    
    public static String CBCEncrypt(String plainText, String key) throws Exception {
        byte[] clean = plainText.getBytes();
        // Generating IV
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);
        
        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);
        return Base64.encodeBase64String(encryptedIVAndText);
    }
    
    public static String CBCDecryptByte(byte[] bytes, String key) throws Exception {
        int ivSize = 16;
        int keySize = 16;
        byte[] encryptedIvTextBytes = bytes; 
        // Extract IV.
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }

    public static String CBCDecrypt(String encryptedText, String key){
	    	int ivSize = 16;
	    	int keySize = 16;
	    	byte[] decrypted;
	    	byte[] encryptedIvTextBytes = Base64.decodeBase64(encryptedText);
	    	try {
	    		// Extract IV.
	    		byte[] iv = new byte[ivSize];
	    		System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
	    		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	
	    		// Extract encrypted part.
	    		int encryptedSize = encryptedIvTextBytes.length - ivSize;
	    		byte[] encryptedBytes = new byte[encryptedSize];
	    		System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);
	
	    		// Hash key.
	    		byte[] keyBytes = new byte[keySize];
	    		MessageDigest md = MessageDigest.getInstance("SHA-256");
	    		md.update(key.getBytes());
	    		System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
	    		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
	
	    		// Decrypt.
	    		Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    		cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
	    		decrypted = cipherDecrypt.doFinal(encryptedBytes);
	    		return new String(decrypted);
	    	}catch(Exception e) {
	    		System.out.println("encryptedText: " + encryptedText);
	    		e.printStackTrace();
	    	}
	    	return "";
    }
}