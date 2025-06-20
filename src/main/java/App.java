import gui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Главный класс приложения JavaFX.
 * Является точкой входа для JavaFX-runtime и отвечает за создание
 * и отображение основного окна приложения (Stage).
 */
public class App extends Application {

    /**
     * Главный метод, который вызывается при запуске JavaFX приложения.
     * Здесь мы создаем главный вид, сцену и настраиваем основное окно.
     *
     * @param primaryStage главное окно (контейнер) приложения, предоставляемое JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow();

        // Создаем "сцену", помещая в нее корневой элемент окна
        Scene scene = new Scene(mainWindow.getView(), 800, 600); // Начальный размер 800x600

        // Подключаем CSS файл для стилизации интерфейса
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        // Настраиваем главное окно
        primaryStage.setTitle("File Deduplicator"); // Заголовок окна
        // Устанавливаем иконку приложения
        try {
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/app_icon.png"))));
        } catch (Exception e) {
            System.err.println("Could not load application icon.");
            e.printStackTrace();
        }

        primaryStage.setScene(scene); // Устанавливаем сцену в окно
        primaryStage.show(); // Показываем окно пользователю
    }

    /**
     * Основной метод main, который запускает JavaFX приложение.
     *
     * @param args аргументы командной строки (в данном приложении не используются).
     */
    public static void main(String[] args) {
        launch(args);
    }
}