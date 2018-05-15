package com.delfino.util;

import static org.junit.Assert.assertEquals;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.junit.Test;

public class CryptUtilTest {

	@Test
	public void testEncryptDecrypt() {
		
		Arrays.asList("A", "", "      ",
			"The quick brown fox jumps over the lazy dog", 
			"!@#(DSF@(#$   54953<>?~~++_=-",
			"你就足夠/다른 남자 말고 너 ").stream().forEach(text -> {
				
				try {
					String enc = CryptUtil.encrypt(text);
					assertEquals(text, CryptUtil.decrypt(enc));
				} catch (GeneralSecurityException e) {
					throw new IllegalStateException(e);
				}
			});
	}
}
