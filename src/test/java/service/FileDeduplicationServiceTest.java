package service;

import model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDeduplicationServiceTest {

    @TempDir
    Path tempDir;

    private FileDeduplicationService service;
    private List<FileInfo> files;
    private Path fileToDelete1, fileToDelete2, fileToKeep;

    @BeforeEach
    void setUp() throws IOException {
        service = new FileDeduplicationService();
        files = new ArrayList<>();

        // Создаем файлы для теста
        fileToDelete1 = Files.createFile(tempDir.resolve("delete_me_1.txt"));
        fileToDelete2 = Files.createFile(tempDir.resolve("delete_me_2.txt"));
        fileToKeep = Files.createFile(tempDir.resolve("keep_me.txt"));

        // Создаем список FileInfo, имитирующий результат сканирования
        FileInfo info1 = new FileInfo(fileToDelete1, 10, "hash123");
        info1.setSelected(true); // Помечаем на удаление

        FileInfo info2 = new FileInfo(fileToDelete2, 20, "hash456");
        info2.setSelected(true); // Помечаем на удаление

        FileInfo info3 = new FileInfo(fileToKeep, 30, "hash789");
        info3.setSelected(false); // НЕ помечаем на удаление

        files.add(info1);
        files.add(info2);
        files.add(info3);
    }

    /**
     * Этот тест проверяет удаление (перемещение в корзину).
     * Он будет пропущен в CI/CD средах на Linux, где нет GUI и корзины.
     * Для локальной разработки на Windows/macOS он должен работать.
     */
    @Test
    @DisabledOnOs(OS.LINUX) // Пропускаем на Linux, т.к. AWT/Desktop может быть недоступен
    void testDeleteSelectedDuplicates() {
        // Act
        int deletedCount = service.deleteSelectedDuplicates(files);

        // Assert
        assertEquals(2, deletedCount, "Должно быть удалено 2 файла.");

        // Проверяем, что файлы действительно удалены (или перемещены)
        assertFalse(Files.exists(fileToDelete1), "Файл 'delete_me_1.txt' должен быть удален.");
        assertFalse(Files.exists(fileToDelete2), "Файл 'delete_me_2.txt' должен быть удален.");
        assertTrue(Files.exists(fileToKeep), "Файл 'keep_me.txt' должен остаться.");
    }

    @Test
    void testDeleteWithNoFilesSelected() {
        // Arrange
        // Снимаем выбор со всех файлов
        files.forEach(f -> f.setSelected(false));

        // Act
        int deletedCount = service.deleteSelectedDuplicates(files);

        // Assert
        assertEquals(0, deletedCount, "Ни один файл не должен быть удален.");
        assertTrue(Files.exists(fileToDelete1));
        assertTrue(Files.exists(fileToDelete2));
        assertTrue(Files.exists(fileToKeep));
    }

    @Test
    void testDeleteWithEmptyList() {
        // Act
        int deletedCount = service.deleteSelectedDuplicates(new ArrayList<>());

        // Assert
        assertEquals(0, deletedCount);
    }

}