/**
 * Класс-обертка для корректного запуска JavaFX-приложения из jar-with-dependencies.
 * Современные версии Java (11+) разделили JavaFX от основного JDK.
 * Это привело к проблеме: если запустить fat-jar напрямую (`java -jar app.jar`),
 * ClassLoader не может найти главный класс, который наследует {@link javafx.application.Application}.
 * Этот Launcher является "обычным" Java-классом без наследования от Application.
 * JVM может без проблем найти и запустить его метод `main`. А уже из этого метода мы вызываем
 * `App.main(args)`, который корректно инициализирует и запускает JavaFX-runtime.
 * В `pom.xml` в плагине `maven-assembly-plugin` этот класс указан как `<mainClass>`.
 */
public class Launcher {
    /**
     * Точка входа в приложение при запуске из JAR-файла.
     * @param args аргументы командной строки.
     */
    public static void main(String[] args) {
        // Передаем управление настоящему главному классу JavaFX
        App.main(args);
    }
}