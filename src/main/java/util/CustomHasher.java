package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Класс для вычисления 128-битного хеша для файлов.
 * Предоставляет высокую производительность и низкий шанс коллизий
 * при работе с файлами, но не предназначен для использования в криптографических целях.
 * Основан на идеях смешивающих функций, таких как FNV и MurmurHash.
 */
public class CustomHasher {

    /** Размер буфера для чтения файла в байтах. */
    private static final int BUFFER_SIZE = 4096;

    /**
     * "Соль" — фиксированное значение, используемое в хешировании для усложнения хеш-функции.
     * Получено путем хеширования строки "deduplicator" с помощью алгоритма FNV-1a.
     */
    private static final long SALT = fnv64("deduplicator");

    /** Первое начальное 64-битное значение для хеш-функции. Выбрано как константа FNV-1a. */
    private static final long INITIAL1 = 0xcbf29ce484222325L;
    /** Второе начальное 64-битное значение. Является инвертированной версией первой константы. */
    private static final long INITIAL2 = 0x84222325cbf29ce4L;

    /**
     * Вычисляет 128-битный хеш для содержимого файла и возвращает его в виде шестнадцатеричной строки.
     * Метод читает файл по частям, чтобы эффективно работать с большими файлами, не загружая их целиком в память.
     *
     * @param path путь к файлу, для которого нужно вычислить хеш.
     * @return 128-битный хеш в виде 32-символьной шестнадцатеричной строки.
     * @throws IOException если возникает ошибка при чтении файла.
     */
    public static String hash(Path path) throws IOException {
        long hash1 = INITIAL1;
        long hash2 = INITIAL2;

        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            long totalBytes = 0;

            // Читаем файл по частям
            while ((len = in.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    int b = buffer[i] & 0xFF; // Получаем беззнаковое значение байта

                    // Каждый байт влияет на оба хеша

                    // Обновление первого хеша
                    hash1 ^= b ^ (SALT & 0xFF); // XOR с байтом и частью соли
                    hash1 *= 0x100000001B3L;     // Умножение на простое число FNV
                    hash1 = Long.rotateLeft(hash1, 13); // Битовый циклический сдвиг

                    // Обновление второго хеша
                    hash2 += b + (hash1 ^ totalBytes); // Зависимость от первого хеша и количества байт
                    hash2 *= 0xC6A4A7935BD1E995L;   // Умножение на простое число MurmurHash
                    hash2 = Long.rotateRight(hash2, 17); // Другой битовый сдвиг

                    totalBytes++;
                }
            }

            // Дополнительное смешивание в конце, чтобы распределить биты еще лучше
            long finalHash = (hash1 ^ hash2) ^ (totalBytes * SALT);
            long secondHash = Long.rotateLeft(hash1, 32) ^ Long.rotateRight(hash2, 32);

            // Объединяем два 64-битных хеша в одну 128-битную строку
            return Long.toHexString(finalHash) + Long.toHexString(secondHash);
        }
    }

    /**
     * Вспомогательная функция для вычисления 64-битного хеша строки по алгоритму FNV-1a.
     * Используется для генерации соли из строки.
     *
     * @param input строка для хеширования.
     * @return 64-битное хеш-значение.
     */
    private static long fnv64(String input) {
        final long FNV_PRIME = 0x100000001B3L;
        long hash = 0xcbf29ce484222325L; // Начальное значение FNV-1a
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            hash ^= b;
            hash *= FNV_PRIME;
        }

        return hash;
    }
}