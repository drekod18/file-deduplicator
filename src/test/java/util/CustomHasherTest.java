package util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CustomHasherTest {

    @TempDir
    Path tempDir;

    @Test
    void testIdenticalFilesHaveSameHash() throws IOException {
        // Arrange (Подготовка)
        Path fileA = tempDir.resolve("fileA.txt");
        Path fileB = tempDir.resolve("fileB.txt");
        String content = "Это одинаковый контент для проверки хеша.";

        Files.writeString(fileA, content);
        Files.writeString(fileB, content);

        // Act (Действие)
        String hashA = CustomHasher.hash(fileA);
        String hashB = CustomHasher.hash(fileB);

        // Assert (Проверка)
        Assertions.assertNotNull(hashA);
        Assertions.assertEquals(hashA, hashB, "Хеши одинаковых файлов должны совпадать.");
    }

    @Test
    void testDifferentFilesHaveDifferentHashes() throws IOException {
        // Arrange
        Path fileA = tempDir.resolve("fileA.txt");
        Path fileB = tempDir.resolve("fileB.txt");

        Files.writeString(fileA, "Это контент файла А.");
        Files.writeString(fileB, "Это совершенно другой контент файла Б.");

        // Act
        String hashA = CustomHasher.hash(fileA);
        String hashB = CustomHasher.hash(fileB);

        // Assert
        Assertions.assertNotNull(hashA);
        Assertions.assertNotNull(hashB);
        Assertions.assertNotEquals(hashA, hashB, "Хеши разных файлов не должны совпадать.");
    }

    @Test
    void testEmptyFileHasConsistentHash() throws IOException {
        // Arrange
        Path fileA = tempDir.resolve("emptyA.txt");
        Path fileB = tempDir.resolve("emptyB.txt");
        Files.createFile(fileA); // Создаем пустые файлы
        Files.createFile(fileB);

        // Act
        String hashA = CustomHasher.hash(fileA);
        String hashB = CustomHasher.hash(fileB);

        // Assert
        Assertions.assertNotNull(hashA);
        Assertions.assertFalse(hashA.isEmpty());
        Assertions.assertEquals(hashA, hashB, "Хеши пустых файлов должны быть одинаковыми и консистентными.");
    }
}