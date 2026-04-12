package com.openjar.user_service.utils;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyGeneratorUtil {

    public static void main(String[] args) {
        try {
            // 1. Create the directory if it doesn't exist
            Path path = Paths.get("src/main/resources/certs");
            Files.createDirectories(path);

            // 2. Generate the RSA Key Pair (2048 bit is enterprise standard)
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 3. Save Public Key (X.509 format)
            try (FileOutputStream fos = new FileOutputStream("src/main/resources/certs/public.der")) {
                fos.write(keyPair.getPublic().getEncoded());
            }

            // 4. Save Private Key (PKCS#8 format)
            try (FileOutputStream fos = new FileOutputStream("src/main/resources/certs/private.der")) {
                fos.write(keyPair.getPrivate().getEncoded());
            }

            System.out.println("Keys generated successfully in src/main/resources/certs/");
            System.out.println("Public Key Format: " + keyPair.getPublic().getFormat()); // X.509
            System.out.println("Private Key Format: " + keyPair.getPrivate().getFormat()); // PKCS#8

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}