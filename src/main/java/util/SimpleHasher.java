package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleHasher {
    public static String hash(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        int hash = 0;
        for (byte b : bytes) {
            hash = (hash * 31 + (b & 0xFF)) % 1_000_000_007;
        }
        return Integer.toHexString(hash);
    }
}
