package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class CustomHasher {

    // Размер буфера для чтения файлов
    private static final int BUFFER_SIZE = 4096;

    // Фиксированная соль (например, на основе строки "deduplicator")
    private static final long SALT = fnv64("deduplicator");

    // Фиксированное начальное состояние
    private static final long INITIAL1 = 0xcbf29ce484222325L;
    private static final long INITIAL2 = 0x84222325cbf29ce4L;

    public static String hash(Path path) throws IOException {
        long hash1 = INITIAL1;
        long hash2 = INITIAL2;

        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            long totalBytes = 0;

            while ((len = in.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    int b = buffer[i] & 0xFF;

                    // Основное смешивание байтов
                    hash1 ^= b ^ (SALT & 0xFF);
                    hash1 *= 0x100000001B3L;
                    hash1 = Long.rotateLeft(hash1, 13);

                    hash2 += b + (hash1 ^ totalBytes);
                    hash2 *= 0xC6A4A7935BD1E995L;
                    hash2 = Long.rotateRight(hash2, 17);

                    totalBytes++;
                }
            }

            // Финализация
            long finalHash = (hash1 ^ hash2) ^ (totalBytes * SALT);
            long secondHash = Long.rotateLeft(hash1, 32) ^ Long.rotateRight(hash2, 32);

            return Long.toHexString(finalHash) + Long.toHexString(secondHash);
        }
    }

    // Вспомогательная функция для хеширования строки в 64-битное значение (FNV-1a)
    private static long fnv64(String input) {
        final long FNV_PRIME = 0x100000001B3L;
        long hash = 0xcbf29ce484222325L;
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            hash ^= b;
            hash *= FNV_PRIME;
        }

        return hash;
    }
}
