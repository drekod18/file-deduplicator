package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

/**
 * Класс-модель, представляющий информацию об одном файле.
 * Этот класс используется для хранения данных о файлах и для их отображения в {@link javafx.scene.control.TableView}.
 * Используются JavaFX Properties ({@link StringProperty}, {@link LongProperty} и т.д.)
 * для того, чтобы позволить элементам интерфейса "подписываться" на изменения в этих полях
 * и автоматически обновляться.
 */
public class FileInfo {

    /** Абсолютный путь к файлу в файловой системе. */
    private final Path path;

    /** Свойство JavaFX для хранения пути к файлу в виде строки. Используется для привязки к колонке в TableView. */
    private final StringProperty pathString;

    /** Свойство JavaFX для хранения размера файла в байтах. */
    private final LongProperty size;

    /** Свойство JavaFX для хранения вычисленной хеш-суммы файла. */
    private final StringProperty hash;

    /** Свойство JavaFX, указывающее, выбран ли данный файл для удаления (например, с помощью чекбокса в таблице). */
    private final BooleanProperty selected;

    /**
     * Конструктор для создания нового объекта FileInfo.
     *
     * @param path путь к файлу.
     * @param size размер файла в байтах.
     * @param hash вычисленная хеш-сумма файла в виде строки.
     */
    public FileInfo(Path path, long size, String hash) {
        this.path = path;
        // Инициализируем JavaFX Properties
        this.pathString = new SimpleStringProperty(path.toString());
        this.size = new SimpleLongProperty(size);
        this.hash = new SimpleStringProperty(hash);
        this.selected = new SimpleBooleanProperty(false); // По умолчанию файл не выбран для удаления
    }

    // Геттеры для доступа к данным

    /**
     * Возвращает путь к файлу.
     * @return объект {@link Path}, представляющий путь.
     */
    public Path getPath() { return path; }

    /**
     * Возвращает размер файла в байтах.
     * @return размер файла.
     */
    public long getSize() { return size.get(); }

    /**
     * Возвращает хеш-сумму файла.
     * @return хеш в виде строки.
     */
    public String getHash() { return hash.get(); }

    /**
     * Проверяет, выбран ли файл для удаления.
     * @return true, если файл выбран, иначе false.
     */
    public boolean isSelected() {
        return selected.get();
    }

    /**
     * Устанавливает статус "выбран для удаления".
     * @param selected true, чтобы выбрать файл, false, чтобы снять выбор.
     */
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }


    // Геттеры для JavaFX Properties
    // Эти методы необходимы для механизма привязки данных в JavaFX.
    // TableView использует их, чтобы "слушать" изменения в модели.

    /**
     * Возвращает свойство пути к файлу.
     * @return объект {@link StringProperty}.
     */
    public StringProperty pathProperty() { return pathString; }

    /**
     * Возвращает свойство размера файла.
     * @return объект {@link LongProperty}.
     */
    public LongProperty sizeProperty() { return size; }

    /**
     * Возвращает свойство хеш-суммы файла.
     * @return объект {@link StringProperty}.
     */
    public StringProperty hashProperty() { return hash; }

    /**
     * Возвращает свойство "выбран".
     * @return объект {@link BooleanProperty}.
     */
    public BooleanProperty selectedProperty() {
        return selected;
    }
}