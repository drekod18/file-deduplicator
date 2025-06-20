package service;

import javafx.concurrent.Task;
import model.FileInfo;
import java.nio.file.Path;
import java.util.List;

/**
 * JavaFX {@link Task} для выполнения операции сканирования в фоновом потоке.
 * Этот класс является мостом между долгой задачей (сканирование) и UI-потоком,
 * позволяя избежать "зависания" графического интерфейса.
 * Он оборачивает {@link DuplicateScanner} и передает его обновления в UI-совместимом виде.
 */
public class ScanTask extends Task<List<FileInfo>> {

    /** Корневая директория для сканирования. */
    private final Path root;

    /**
     * Конструктор задачи сканирования.
     *
     * @param root путь к директории, которую необходимо просканировать.
     */
    public ScanTask(Path root) {
        this.root = root;
    }

    /**
     * Основной метод, который будет выполняться в фоновом потоке.
     *
     * @return список найденных файлов-дубликатов.
     * @throws Exception если в процессе сканирования произошла ошибка.
     *                   Это исключение будет поймано и обработано в методе setOnFailed() в UI.
     */
    @Override
    protected List<FileInfo> call() throws Exception {
        // Создаем наш чистый сканер.
        // В качестве слушателей (колбэков) мы передаем ему методы этого Task'а:
        // - updateProgress() для обновления ProgressBar
        // - updateMessage() для обновления Label со статусом
        DuplicateScanner scanner = new DuplicateScanner(
                progress -> updateProgress(progress, 1.0),
                this::updateMessage
        );

        // Запускаем логику и возвращаем результат, который будет доступен
        // через getValue() в обработчике setOnSucceeded.
        return scanner.findDuplicates(root);
    }
}