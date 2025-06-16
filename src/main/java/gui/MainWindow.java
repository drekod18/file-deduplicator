package gui;

import model.FileInfo;
import service.FileDeduplicationService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MainWindow {
    private final VBox root;
    private final Label folderLabel;
    private final Button chooseButton;
    private final Button scanButton;
    private final Button deleteButton;
    private final TextArea resultArea;

    private Path selectedFolder;
    private final FileDeduplicationService service;

    public MainWindow() {
        root = new VBox(10);
        root.setPadding(new Insets(15));

        folderLabel = new Label("Выберите папку...");
        chooseButton = new Button("Выбрать папку");
        scanButton = new Button("Сканировать на дубликаты");
        deleteButton = new Button("Удалить дубликаты");
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(false);

        service = new FileDeduplicationService();

        chooseButton.setOnAction(e -> chooseDirectory());
        scanButton.setOnAction(e -> scan());
        deleteButton.setOnAction(e -> delete());

        HBox buttons = new HBox(10, chooseButton, scanButton, deleteButton);
        root.getChildren().addAll(folderLabel, buttons, resultArea);
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите директорию");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            selectedFolder = dir.toPath();
            folderLabel.setText("Папка: " + selectedFolder.toString());
        }
    }

    private void scan() {
        if (selectedFolder == null) {
            showAlert("Ошибка", "Сначала выберите папку.");
            return;
        }

        resultArea.clear();
        List<FileInfo> duplicates = service.findDuplicates(selectedFolder);

        if (duplicates.isEmpty()) {
            resultArea.setText("Дубликаты не найдены.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Найдено дубликатов: ").append(duplicates.size()).append("\n\n");
            for (FileInfo f : duplicates) {
                sb.append(f.getPath()).append(" (").append(f.getSize()).append(" байт, hash=").append(f.getHash()).append(")\n");
            }
            resultArea.setText(sb.toString());
        }
    }

    private void delete() {
        int count = service.deleteFoundDuplicates();
        showAlert("Удаление завершено", "Удалено дубликатов: " + count);
        resultArea.appendText("\nУдалено файлов: " + count);
    }

    public VBox getView() {
        return root;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
