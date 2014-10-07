package roxtools.crypto;

import org.apache.commons.io.IOUtils;
import roxtools.crypto.AESKeyGenerator.AESMode;
import roxtools.crypto.RSAKeyGenerator.RSAMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;

public class TestCrypto {

    private static void clientCrypt(final byte[] data) throws IOException {
        AESKeyGenerator keyGeneratorAES = new AESKeyGenerator(AESMode.AES_128);
        Key keyAES = keyGeneratorAES.createKey();
        AESCodec aesCodec = new AESCodec(keyAES);

        byte[] iv = CryptoUtils.createInitializationVector(4376356712312L);
        System.out.println("iv> " + new String(iv));

        byte[] dataCrypt = aesCodec.encrypt(data, iv);
        System.out.println("dataCrypt> " + new String(dataCrypt));

        RSAKeyGenerator keyGeneratorRSA = new RSAKeyGenerator(RSAMode.RSA_1024);
        KeyPair keyPairRSA = keyGeneratorRSA.createKeyPair();
        RSACodec rsaCodec = new RSACodec(keyPairRSA.getPrivate());
        byte[] privateKeyCrypt = rsaCodec.encrypt(aesCodec.getKeyEncoded());
        System.out.println("privateKeyCrypt> " + new String(privateKeyCrypt));

        byte[] publicKeyEncoded = CryptoUtils.encodeKey(keyPairRSA.getPublic());
        System.out.println("publicKeyEncoded> " + new String(publicKeyEncoded));

        serverDecrypt(publicKeyEncoded, dataCrypt, privateKeyCrypt);
    }

    private static void serverDecrypt(byte[] publicKeyEncoded, byte[] dataCrypt, byte[] privateKeyCrypt) throws IOException {
        byte[] iv = CryptoUtils.createInitializationVector(4376356712312L);
        System.out.println("iv> " + Arrays.toString(iv));

        RSACodec rsaCodec = new RSACodec(publicKeyEncoded, false);
        byte[] dataKey = rsaCodec.decrypt(privateKeyCrypt);
        System.out.println("dataKeyDecrypt> " + new String(dataKey));

        AESCodec aesCodec = new AESCodec(dataKey);
        byte[] decrypt = aesCodec.decrypt(dataCrypt, iv);

        String decryptStr = new String(decrypt);
        System.out.println("decrypt> " + decryptStr + " >> " + decryptStr.hashCode());
    }

    private static void clientFileCrypt(final File file) throws Exception {
        AESKeyGenerator keyGeneratorAES = new AESKeyGenerator(AESMode.AES_128);
        Key keyAES = keyGeneratorAES.createKey();
        AESCodec codecAES = new AESCodec(keyAES);

        RSAKeyGenerator keyGeneratorRSA = new RSAKeyGenerator(RSAMode.RSA_1024);
        KeyPair keyPairRSA = keyGeneratorRSA.createKeyPair();
        RSACodec codecRSA = new RSACodec(keyPairRSA.getPrivate());

        long seed = 4376356712312L;
        byte[] data = IOUtils.toByteArray(new FileInputStream(file)),
               publicKeyEncoded = CryptoUtils.encodeKey(keyPairRSA.getPublic()),
               privateKeyCrypt = codecRSA.encrypt(codecAES.getKeyEncoded()),
               iv = CryptoUtils.createInitializationVector(seed),
               dataCrypt = codecAES.encrypt(data, iv);

        System.out.println("iv> " + Arrays.toString(iv));

        File publicKey = new File("/home/acactown/tmp/encrypt/publicKey.key");
        IOUtils.write(publicKeyEncoded, new FileOutputStream(publicKey));

        File privateKey = new File("/home/acactown/tmp/encrypt/privateKey.key");
        IOUtils.write(privateKeyCrypt, new FileOutputStream(privateKey));

        File encrypted = new File("/home/acactown/tmp/encrypt/logentries-encrypted");
        IOUtils.write(dataCrypt, new FileOutputStream(encrypted));

        serverFileDecrypt(publicKey, encrypted, privateKey, seed);
    }

    private static void serverFileDecrypt(File publicKeyEncodedFile, File encrypted, File privateKeyCryptFile, long seed) throws IOException {
        byte[] publicKeyEncoded = IOUtils.toByteArray(new FileInputStream(publicKeyEncodedFile)),
               privateKeyCrypt = IOUtils.toByteArray(new FileInputStream(privateKeyCryptFile)),
               dataCrypt = IOUtils.toByteArray(new FileInputStream(encrypted)),
               iv = CryptoUtils.createInitializationVector(seed);

        System.out.println("iv> " + Arrays.toString(iv));

        RSACodec rsaCodec = new RSACodec(publicKeyEncoded, false);
        byte[] dataKey = rsaCodec.decrypt(privateKeyCrypt);
        AESCodec aesCodec = new AESCodec(dataKey);

        byte[] decrypt = aesCodec.decrypt(dataCrypt, iv);
        File file = new File("/home/acactown/tmp/encrypt/logentries-new.zip");
        IOUtils.write(decrypt, new FileOutputStream(file));
    }

    private static void testCrypt(byte[] data, String password) {
        String dataStr = new String(data);
        System.out.println("data> " + dataStr + " >> " + dataStr.hashCode());

        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator(AESMode.AES_128);

        Key key = aesKeyGenerator.createKey(password, CryptoUtils.createInitializationVector(123));
        AESCodec aesCodec = new AESCodec(key);

        byte[] encrypt = aesCodec.encrypt(data, CryptoUtils.createInitializationVector(34763416723L));

        testDecrypt(encrypt, password);
    }


    private static void testDecrypt(byte[] encrypt, String password) {
        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator(AESMode.AES_128);
        Key key = aesKeyGenerator.createKey(password, CryptoUtils.createInitializationVector(123));
        AESCodec aesCodec = new AESCodec(key);

        byte[] decrypt = aesCodec.decrypt(encrypt, CryptoUtils.createInitializationVector(34763416723L));
        String decryptStr = new String(decrypt);

        System.out.println("decrypt> " + decryptStr + " >> " + decryptStr.hashCode());
    }


    public static void main(String[] args) throws Exception {
        //byte[] data = "TESTE: Andres Camilo Amado Cardenas".getBytes();

        //String dataStr = new String(data);
        //System.out.println("data> " + dataStr + " >> " + dataStr.hashCode());

        //clientCrypt(data);

        //testCrypt(data, "zdfjg384278sds");
        File file = new File("/home/acactown/tmp/encrypt/logentries.zip");
        clientFileCrypt(file);
    }

}


