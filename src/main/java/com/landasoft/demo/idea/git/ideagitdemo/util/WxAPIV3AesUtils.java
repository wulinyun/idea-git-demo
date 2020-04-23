package com.landasoft.demo.idea.git.ideagitdemo.util;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @Author wulinyun
 * @Version 1.0
 * @JdkVesion 1.7
 * @Description 微信APIv3回调通知解密算法工具类
 * @Date 2020/4/23 13:09
 */
public class WxAPIV3AesUtils {
    static final int KEY_LENGTH_BYTE = 32;
    static final int TAG_LENGTH_BIT = 128;
    private final byte[] aesKey;

    /**
     *
     * @param key 用商户平台上设置的APIv3密钥
     */
    public WxAPIV3AesUtils(byte[] key) {
        if (key.length != KEY_LENGTH_BYTE) {
            throw new IllegalArgumentException("无效的ApiV3Key，长度必须为32个字节");
        }
        this.aesKey = key;
    }

    /**
     *
     * @param associatedData 附加数据
     * @param nonce 随机串
     * @param ciphertext 数据密文
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public String decryptToString(byte[] associatedData, byte[] nonce, String ciphertext)
            throws GeneralSecurityException, IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);

            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData);

            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
