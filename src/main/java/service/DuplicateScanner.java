package service;

import model.FileInfo;
import util.CustomHashMap;
import util.CustomHasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Основной класс, реализующий логику сканирования и поиска файлов-дубликатов.
 * Он не зависит от UI и сообщает о своем прогрессе и статусе через колбэки (Consumers).
 */
public class DuplicateScanner {

    /** Колбэк для обновления прогресса сканирования (значение от 0.0 до 1.0). */
    private final Consumer<Double> progressConsumer;
    /** Колбэк для отправки текстовых сообщений о текущем статусе операции. */
    private final Consumer<String> messageConsumer;

    /**
     * Конструктор сканера.
     *
     * @param progressConsumer функция, которая будет вызываться для обновления прогресса.
     * @param messageConsumer  функция, которая будет вызываться для обновления статусных сообщений.
     */
    public DuplicateScanner(Consumer<Double> progressConsumer, Consumer<String> messageConsumer) {
        this.progressConsumer = progressConsumer;
        this.messageConsumer = messageConsumer;
    }

    /**
     * Главный метод, запускающий поиск дубликатов в указанной директории.
     * Процесс состоит из трех этапов:
     * 1. Сбор всех файлов в директории.
     * 2. Группировка файлов по размеру.
     * 3. Хеширование файлов в группах с одинаковым размером и поиск дубликатов по хешу.
     *
     * @param root корневая директория для сканирования.
     * @return список всех файлов, которые являются частью группы дубликатов.
     * @throws IOException если возникает ошибка при доступе к файлам.
     */
    public List<FileInfo> findDuplicates(Path root) throws IOException {
        messageConsumer.accept("Сбор списка файлов...");
        List<Path> allFiles;
        try (Stream<Path> walk = Files.walk(root)) {
            allFiles = walk.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        final int totalFiles = allFiles.size();
        if (totalFiles == 0) {
            messageConsumer.accept("Файлы не найдены.");
            progressConsumer.accept(1.0);
            return new ArrayList<>();
        }
        int processedFiles = 0;

        // Группировка по размеру
        messageConsumer.accept("Группировка файлов по размеру...");
        CustomHashMap<Long, List<Path>> filesBySizemap = new CustomHashMap<>();
        for (Path file : allFiles) {
            try {
                long size = Files.size(file);
                List<Path> list = filesBySizemap.get(size);
                if (list == null) {
                    list = new ArrayList<>();
                    filesBySizemap.put(size, list);
                }
                list.add(file);
            } catch (IOException e) {
                System.err.println("Could not get file size for: " + file + ". Skipping file.");
            }
        }

        // Хеширование и группировка по хешу
        CustomHashMap<String, List<FileInfo>> filesByHashMap = new CustomHashMap<>();
        messageConsumer.accept("Хеширование файлов и поиск дубликатов...");

        for (List<Path> group : filesBySizemap.values()) {
            if (group.size() < 2) {
                processedFiles += group.size();
                updateProgress(processedFiles, totalFiles);
                continue;
            }

            for (Path file : group) {
                try {
                    String hash = CustomHasher.hash(file);
                    long size = Files.size(file);
                    FileInfo info = new FileInfo(file, size, hash);

                    List<FileInfo> list = filesByHashMap.get(hash);
                    if (list == null) {
                        list = new ArrayList<>();
                        filesByHashMap.put(hash, list);
                    }
                    list.add(info);
                } catch (IOException e) {
                    System.err.println("Could not read or hash file: " + file + ". Skipping file.");
                }
                processedFiles++;
                updateProgress(processedFiles, totalFiles);
            }
        }

        // Формирование списка дубликатов
        List<FileInfo> result = new ArrayList<>();
        for (List<FileInfo> duplicateGroup : filesByHashMap.values()) {
            if (duplicateGroup.size() > 1) {
                duplicateGroup.get(0).setSelected(false);
                for (int i = 1; i < duplicateGroup.size(); i++) {
                    duplicateGroup.get(i).setSelected(true);
                }
                result.addAll(duplicateGroup);
            }
        }

        messageConsumer.accept("Сканирование завершено.");
        return result;
    }

    /**
     * Метод для корректного обновления прогресса.
     * @param processedFiles количество обработанных файлов.
     * @param totalFiles общее количество файлов.
     */
    private void updateProgress(int processedFiles, int totalFiles) {
        if (totalFiles > 0) {
            progressConsumer.accept((double) processedFiles / totalFiles);
        }
    }
}