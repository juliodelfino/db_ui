package com.delfino.util;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.delfino.adaptor.ExceptionAdaptor;

public class CryptUtil {
	
	private static final String AES = "AES";
	private static final Key AESKEY = new SecretKeySpec(
			ExceptionAdaptor.class.getSimpleName().getBytes(), AES); // 128 bit key
	

	public static String encrypt(String plainText) throws GeneralSecurityException {

		// Create key and cipher
         Cipher cipher = Cipher.getInstance(AES);
         // encrypt the text
         cipher.init(Cipher.ENCRYPT_MODE, AESKEY);
         byte[] encrypted = cipher.doFinal(plainText.getBytes());
         return new String(Base64.getEncoder().encode(encrypted));
	}
	
	public static String decrypt(String b64encryptedText) throws GeneralSecurityException {
		
        // Create key and cipher
        Cipher cipher = Cipher.getInstance(AES);

        // decrypt the text
        cipher.init(Cipher.DECRYPT_MODE, AESKEY);
        return new String(cipher.doFinal(Base64.getDecoder().decode(b64encryptedText.getBytes())));
	}
}
