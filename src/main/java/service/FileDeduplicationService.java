package service;

import model.FileInfo;
import java.awt.Desktop;
import java.util.List;

/**
 * Сервисный класс, отвечающий за удаление файлов.
 */
public class FileDeduplicationService {

    /**
     * Удаляет файлы, отмеченные для удаления, путем перемещения их в системную корзину.
     * Метод выполняет удаление, только если операционная система
     * поддерживает перемещение в корзину. Если поддержка отсутствует (например, в среде без GUI),
     * метод не будет производить никаких действий и вернет 0, чтобы гарантировать,
     * что файлы не будут удалены необратимо без ведома пользователя.
     *
     * @param allFiles список объектов FileInfo, содержащий информацию о файлах,
     *                 среди которых могут быть отмеченные для удаления (isSelected() == true).
     * @return количество успешно перемещенных в корзину файлов.
     */
    public int deleteSelectedDuplicates(List<FileInfo> allFiles) {
        if (allFiles == null || allFiles.isEmpty()) {
            return 0;
        }

        // Проверяем, поддерживается ли перемещение в корзину
        if (isTrashSupported()) {
            int deletedCount = 0;
            for (FileInfo fileInfo : allFiles) {
                if (fileInfo.isSelected()) {
                    if (Desktop.getDesktop().moveToTrash(fileInfo.getPath().toFile())) {
                        deletedCount++;
                    }
                }
            }
            return deletedCount;
        } else {
            // Если корзина не поддерживается, выводим сообщение для разработчика в консоль
            // и НЕ делаем ничего с файлами. Возвращаем 0.
            System.err.println("Warning: Move to trash is not supported on this platform. No files were deleted.");
            return 0;
        }
    }

    /**
     * Вспомогательный публичный метод, который позволяет UI заранее проверить,
     * будет ли работать функция удаления.
     * Кнопка "Удалить" станет неактивной, если этот метод вернет false.
     *
     * @return true, если перемещение в корзину поддерживается, иначе false.
     */
    public boolean isTrashSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH);
    }
}