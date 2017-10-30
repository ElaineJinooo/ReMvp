package com.remvp.library.util.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密解密帮助类
 */
public class AESUtil {

    /**
     * AES加密
     *
     * @param src           待加密串
     * @param encryptionKey encryptionKey
     * @param encryptionIv  encryptionIv
     * @return
     */
    public static String encrypt(String src, String encryptionKey, String encryptionIv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, makeKey(encryptionKey), makeIv(encryptionIv));
            return Base64.encodeBytes(cipher.doFinal(src.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     *
     * @param src           待解密串
     * @param encryptionKey encryptionKey
     * @param encryptionIv  encryptionIv
     * @return
     */
    public static String decrypt(String src, String encryptionKey, String encryptionIv) {
        String decrypted = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, makeKey(encryptionKey), makeIv(encryptionIv));
            decrypted = new String(cipher.doFinal(Base64.decode(src)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return decrypted;
    }

    static AlgorithmParameterSpec makeIv(String encryptionIv) {
        try {
            return new IvParameterSpec(encryptionIv.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Key makeKey(String encryptionKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(encryptionKey.getBytes("UTF-8"));
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
