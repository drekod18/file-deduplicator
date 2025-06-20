package service;

import model.FileInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit-тесты для класса DuplicateScanner.
 * Эти тесты проверяют основную бизнес-логику поиска дубликатов в изоляции от JavaFX.
 */
class DuplicateScannerTest {

    /**
     * JUnit 5 автоматически создаст временную директорию перед каждым тестом
     * и удалит ее после, обеспечивая чистоту тестовой среды.
     */
    @TempDir
    Path rootDir;

    // Поля для хранения путей к тестовым файлам
    private Path subDir;
    private Path file1_original, file1_dup, file1_deep_dup;
    private Path file2_original, file2_dup;
    private Path uniqueFile;

    /**
     * Метод, помеченный @BeforeEach, выполняется перед каждым тестовым методом.
     * Он создает сложную файловую структуру для тестирования различных сценариев.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Создаем структуру папок и файлов
        subDir = Files.createDirectory(rootDir.resolve("sub"));

        // Группа дубликатов 1 (3 файла с одинаковым контентом)
        file1_original = createFile(rootDir.resolve("original1.txt"), "Это первый дубликат");
        file1_dup = createFile(rootDir.resolve("duplicate1.txt"), "Это первый дубликат");
        file1_deep_dup = createFile(subDir.resolve("deep_duplicate1.txt"), "Это первый дубликат");

        // Группа дубликатов 2 (2 файла с другим одинаковым контентом)
        file2_original = createFile(rootDir.resolve("original2.dat"), "Это второй дубликат");
        file2_dup = createFile(subDir.resolve("duplicate2.dat"), "Это второй дубликат");

        // Уникальный файл, который не должен быть найден как дубликат
        uniqueFile = createFile(rootDir.resolve("unique.txt"), "Это уникальный файл");
    }

    /**
     * Проверяет, что сканер корректно находит все группы дубликатов
     * и не включает в результат уникальные файлы.
     */
    @Test
    void testFindsAllDuplicates() throws Exception {
        // Arrange (Подготовка)
        // Создаем сканер с "пустыми" слушателями, так как в этом тесте нас не интересует
        // обновление прогресса или сообщений.
        DuplicateScanner scanner = new DuplicateScanner(progress -> {}, message -> {});

        // Act (Действие)
        List<FileInfo> duplicates = scanner.findDuplicates(rootDir);

        // Assert (Проверка)
        Assertions.assertNotNull(duplicates, "Список дубликатов не должен быть null.");
        // Должно быть найдено 3+2=5 файлов, которые являются дубликатами
        assertEquals(5, duplicates.size(), "Должно быть найдено ровно 5 файлов-дубликатов.");

        // Проверяем, что уникального файла нет в списке
        boolean uniqueFound = duplicates.stream().anyMatch(f -> f.getPath().equals(uniqueFile));
        assertFalse(uniqueFound, "Уникальный файл не должен попадать в список дубликатов.");

        // Сгруппируем результаты по хешу, чтобы проверить структуру найденных групп
        Map<String, List<FileInfo>> groups = duplicates.stream().collect(Collectors.groupingBy(FileInfo::getHash));
        assertEquals(2, groups.size(), "Должно быть найдено ровно 2 группы дубликатов.");
        Assertions.assertTrue(groups.values().stream().anyMatch(list -> list.size() == 3), "Должна быть найдена группа из 3-х дубликатов.");
        Assertions.assertTrue(groups.values().stream().anyMatch(list -> list.size() == 2), "Должна быть найдена группа из 2-х дубликатов.");
    }

    /**
     * Проверяет, что сканер автоматически помечает для удаления все файлы в группе, кроме одного.
     */
    @Test
    void testPreselectsFilesForDeletion() throws Exception {
        // Arrange
        DuplicateScanner scanner = new DuplicateScanner(progress -> {}, message -> {});

        // Act
        List<FileInfo> duplicates = scanner.findDuplicates(rootDir);

        // Assert
        // В первой группе (3 файла) должно быть выбрано 2, во второй (2 файла) - 1. Итого: 3.
        long selectedCount = duplicates.stream().filter(FileInfo::isSelected).count();
        assertEquals(3, selectedCount, "Должно быть выбрано для удаления 3 файла.");

        // Проверим, что в каждой группе не выбран ровно один файл
        Map<String, List<FileInfo>> groups = duplicates.stream().collect(Collectors.groupingBy(FileInfo::getHash));
        for (List<FileInfo> group : groups.values()) {
            long unselectedCount = group.stream().filter(f -> !f.isSelected()).count();
            assertEquals(1, unselectedCount, "В каждой группе дубликатов ровно один файл должен быть НЕ выбран.");
        }
    }

    /**
     * Проверяет сценарий, когда в директории нет дубликатов.
     * Результат должен быть пустым списком.
     */
    @Test
    void testFindsNoDuplicatesInDirectoryWithUniqueFiles() throws Exception {
        // Arrange
        Path uniqueDir = Files.createDirectory(rootDir.resolve("uniqueDir"));
        createFile(uniqueDir.resolve("a.txt"), "a");
        createFile(uniqueDir.resolve("b.txt"), "b");
        DuplicateScanner scanner = new DuplicateScanner(progress -> {}, message -> {});

        // Act
        List<FileInfo> duplicates = scanner.findDuplicates(uniqueDir);

        // Assert
        Assertions.assertNotNull(duplicates, "Список дубликатов не должен быть null, даже если он пуст.");
        Assertions.assertTrue(duplicates.isEmpty(), "В папке с уникальными файлами не должно быть найдено дубликатов.");
    }

    /**
     * Вспомогательный метод для создания файла с заданным контентом.
     * @param path Путь к файлу.
     * @param content Содержимое файла.
     * @return Путь к созданному файлу.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    private Path createFile(Path path, String content) throws IOException {
        return Files.writeString(path, content);
    }

    /**
     * Проверяет, что файлы с одинаковым размером, но разным содержимым,
     * не считаются дубликатами. Это напрямую тестирует, что после
     * группировки по размеру происходит корректное хеширование.
     */
    @Test
    void testFilesWithSameSizeButDifferentContentAreNotDuplicates() throws Exception {
        // Arrange
        Path sameSizeDir = Files.createDirectory(rootDir.resolve("sameSizeDir"));

        String contentA = "12345";
        String contentB = "abcde";
        // Убедимся, что контент разный, но размер в байтах одинаковый
        assertEquals(contentA.getBytes().length, contentB.getBytes().length);

        Path fileA = createFile(sameSizeDir.resolve("fileA.txt"), contentA);
        Path fileB = createFile(sameSizeDir.resolve("fileB.txt"), contentB);

        DuplicateScanner scanner = new DuplicateScanner(progress -> {}, message -> {});

        // Act
        // Сканируем всю корневую папку, чтобы убедиться, что эти файлы не смешались
        // с другими дубликатами и не попали в результат.
        List<FileInfo> duplicates = scanner.findDuplicates(rootDir);

        // Assert
        // Проверяем, что ни один из этих двух файлов не попал в итоговый список дубликатов.
        boolean fileAFound = duplicates.stream().anyMatch(f -> f.getPath().equals(fileA));
        boolean fileBFound = duplicates.stream().anyMatch(f -> f.getPath().equals(fileB));

        assertFalse(fileAFound, "Файл с уникальным контентом не должен быть в списке дубликатов.");
        assertFalse(fileBFound, "Файл с уникальным контентом не должен быть в списке дубликатов.");
    }
}