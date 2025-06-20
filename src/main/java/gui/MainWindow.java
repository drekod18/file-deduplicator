package gui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import model.FileInfo;
import service.FileDeduplicationService;
import service.ScanTask;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, отвечающий за создание и управление главным окном приложения.
 * Он содержит всю логику, связанную с отображением элементов интерфейса (GUI),
 * и обработку действий пользователя.
 */
public class MainWindow {

    // UI Элементы

    /** Корневой контейнер окна, располагающий элементы вертикально. */
    private final VBox root;
    /** Метка для отображения пути к выбранной папке. */
    private final Label folderLabel;
    /** Метка для отображения статуса текущей операции (сканирование, удаление). */
    private final Label statusLabel;
    /** Кнопка для вызова диалога выбора директории. */
    private final Button chooseButton;
    /** Кнопка для запуска процесса сканирования. */
    private final Button scanButton;
    /** Кнопка для удаления выбранных дубликатов. */
    private final Button deleteButton;
    /** Индикатор прогресса для длительных операций. */
    private final ProgressBar progressBar;
    /** Таблица для отображения найденных файлов-дубликатов. */
    private final TableView<FileInfo> resultTable;


    /** Путь к папке, выбранной пользователем для сканирования. */
    private Path selectedFolder;
    /** Экземпляр сервиса для выполнения операции удаления файлов. */
    private final FileDeduplicationService service;
    /** Список найденных дубликатов, который является источником данных для таблицы. */
    private List<FileInfo> foundDuplicates = new ArrayList<>();

    /**
     * Конструктор главного окна. Инициализирует все UI-компоненты,
     * собирает их в единый вид и настраивает обработчики событий.
     */
    public MainWindow() {
        root = new VBox(10); // VBox располагает элементы в столбец с отступом в 10 пикселей
        root.setPadding(new Insets(15)); // Внешние отступы для всего окна
        root.setId("root"); // ID для стилизации через CSS

        folderLabel = new Label("Папка не выбрана");
        statusLabel = new Label();
        chooseButton = new Button("Выбрать папку");
        scanButton = new Button("Сканировать");
        deleteButton = new Button("Удалить дубликаты");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE); // Растягиваем прогресс-бар на всю ширину
        progressBar.setVisible(false); // По умолчанию он скрыт

        resultTable = createTable(); // Создаем и настраиваем таблицу
        service = new FileDeduplicationService();

        // Настройка обработчиков событий
        chooseButton.setOnAction(e -> chooseDirectory());
        scanButton.setOnAction(e -> scan());
        deleteButton.setOnAction(e -> delete());

        // Кнопки располагаем горизонтально в контейнере HBox
        HBox buttons = new HBox(10, chooseButton, scanButton, deleteButton);
        root.getChildren().addAll(folderLabel, buttons, progressBar, statusLabel, resultTable);

        // Указываем, что таблица должна растягиваться по вертикали, занимая все доступное место
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        // Начальная настройка состояния кнопок
        updateButtonStates();
    }

    /**
     * Создает и настраивает {@link TableView} для отображения информации о файлах.
     * @return настроенный объект TableView.
     */
    private TableView<FileInfo> createTable() {
        TableView<FileInfo> table = new TableView<>();
        table.setPlaceholder(new Label("Дубликаты не найдены или папка еще не просканирована."));
        table.setEditable(true); // Разрешаем редактирование таблицы (нужно для CheckBox)

        // Колонка с чекбоксами для выбора файлов на удаление
        TableColumn<FileInfo, Boolean> selectCol = new TableColumn<>("Удалить");
        // Привязываем значение ячейки к свойству 'selected' в модели FileInfo
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        // Используем специальную фабрику ячеек для отображения CheckBox
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true); // Разрешаем редактирование этой колонки

        TableColumn<FileInfo, String> pathCol = new TableColumn<>("Путь к файлу");
        pathCol.setCellValueFactory(cellData -> cellData.getValue().pathProperty());

        TableColumn<FileInfo, Number> sizeCol = new TableColumn<>("Размер (байты)");
        sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());

        TableColumn<FileInfo, String> hashCol = new TableColumn<>("Хеш");
        hashCol.setCellValueFactory(cellData -> cellData.getValue().hashProperty());

        // Настройка ширины колонок
        selectCol.setPrefWidth(70);
        selectCol.setResizable(false);
        sizeCol.setPrefWidth(120);
        sizeCol.setResizable(false);
        hashCol.setPrefWidth(250);
        hashCol.setResizable(false);

        // Привязываем ширину колонки с путем к оставшемуся месту в таблице
        pathCol.prefWidthProperty().bind(
                table.widthProperty()
                        .subtract(selectCol.widthProperty())
                        .subtract(sizeCol.widthProperty())
                        .subtract(hashCol.widthProperty())
                        .subtract(20) // Небольшой запас на полосу прокрутки
        );

        table.getColumns().addAll(selectCol, pathCol, sizeCol, hashCol);
        return table;
    }

    /**
     * Открывает системное диалоговое окно для выбора директории сканирования.
     */
    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите директорию для сканирования");
        File dir = chooser.showDialog(root.getScene().getWindow()); // Привязываем диалог к главному окну
        if (dir != null) {
            selectedFolder = dir.toPath();
            folderLabel.setText("Выбранная папка: " + selectedFolder.toString());
        }
    }

    /**
     * Запускает процесс сканирования в фоновом потоке, чтобы не блокировать UI.
     */
    private void scan() {
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Папка не выбрана", "Пожалуйста, сначала выберите папку для сканирования.");
            return;
        }

        ScanTask scanTask = new ScanTask(selectedFolder);

        // Задача успешно завершилась
        scanTask.setOnSucceeded(event -> {
            foundDuplicates = scanTask.getValue(); // Получаем результат из фонового потока
            resultTable.getItems().setAll(foundDuplicates);
            statusLabel.textProperty().unbind(); // Отвязываем метку от задачи
            statusLabel.setText("Сканирование завершено. Найдено дубликатов: " + foundDuplicates.size());
            updateButtonStates(); // Обновляем состояние кнопок
        });

        // Задача завершилась с ошибкой
        scanTask.setOnFailed(event -> {
            scanTask.getException().printStackTrace(); // Выводим ошибку в консоль для отладки
            showAlert(Alert.AlertType.ERROR, "Ошибка сканирования", "Произошла ошибка во время сканирования файлов.");
            statusLabel.textProperty().unbind();
            updateButtonStates();
        });

        // В любом случае (успех, ошибка, отмена)
        scanTask.runningProperty().addListener((obs, wasRunning, isRunning) -> {
            if (!isRunning) {
                progressBar.setVisible(false);
                scanButton.setDisable(false);
            }
        });

        // Привязка UI к состоянию задачи
        progressBar.progressProperty().bind(scanTask.progressProperty());
        statusLabel.textProperty().bind(scanTask.messageProperty());

        // Подготовка UI к запуску задачи
        progressBar.setVisible(true);
        scanButton.setDisable(true);
        resultTable.getItems().clear();
        foundDuplicates.clear();

        new Thread(scanTask).start(); // Запускаем задачу в новом потоке
    }

    /**
     * Запускает процесс удаления выбранных дубликатов.
     */
    private void delete() {
        long selectedCount = foundDuplicates.stream().filter(FileInfo::isSelected).count();
        if (selectedCount == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Файлы не выбраны", "Пожалуйста, отметьте галочками файлы, которые нужно удалить.");
            return;
        }

        int count = service.deleteSelectedDuplicates(foundDuplicates);
        showAlert(Alert.AlertType.INFORMATION, "Удаление завершено", "Перемещено в корзину: " + count + " файлов.");

        // Обновляем UI, удаляя из модели и таблицы только что удаленные файлы
        foundDuplicates.removeIf(FileInfo::isSelected);
        resultTable.getItems().setAll(foundDuplicates);

        updateButtonStates();
    }

    /**
     * Централизованно обновляет состояние кнопок в зависимости от состояния приложения.
     */
    private void updateButtonStates() {
        boolean duplicatesFound = foundDuplicates != null && !foundDuplicates.isEmpty();
        // Кнопка удаления активна только если найдены дубликаты и удаление поддерживается системой
        deleteButton.setDisable(!duplicatesFound || !service.isTrashSupported());
    }

    /**
     * Возвращает корневой элемент VBox для его отображения в главной Scene приложения.
     * @return корневой VBox.
     */
    public VBox getView() {
        return root;
    }

    /**
     * Вспомогательный метод для отображения информационных/диалоговых окон.
     * @param type тип окна (ошибка, предупреждение, информация).
     * @param title заголовок окна.
     * @param message текст сообщения.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("File Deduplicator");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}