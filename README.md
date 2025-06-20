# File Deduplicator - утилита для поиска и удаления дубликатов файлов

Десктопное приложение для поиска дубликатов файлов по их содержимому в указанной директории.

---

## Основные возможности

*   **Интуитивный графический интерфейс:** Чистый и простой UI, созданный с помощью JavaFX, делает приложение доступным для любого пользователя.
*   **Эффективное сканирование:** Используется многоэтапный процесс, который начинается с предварительной фильтрации по размеру для быстрого отсеивания уникальных файлов перед выполнением более ресурсоемких операций.
*   **Хеширование файлов:** Применяется кастомный алгоритм хеширования, гарантирующий высокую скорость и низкий шанс коллизий.
*   **Отзывчивый интерфейс:** Длительная операция сканирования выполняется в фоновом потоке, что гарантирует, что пользовательский интерфейс остается отзывчивым в любое время.
*   **Детализированные результаты:** Найденные дубликаты отображаются в наглядной таблице с указанием пути, размера и хеш-суммы.
*   **Удаление дубликатов:** Файлы перемещаются в системную **Корзину**, а не удаляются навсегда.

---

## Техническое описание

### 1. Обход файлов и предварительная фильтрация по размеру
При запуске сканирования приложение рекурсивно обходит дерево каталогов для сбора списка всех файлов. В качестве первого и важнейшего шага оптимизации эти файлы группируются по размеру. Любой файл с уникальным размером заведомо не может быть дубликатом и немедленно исключается из дальнейшей обработки. Это значительно сокращает количество файлов, которые необходимо хешировать.

### 2. Хеширование содержимого
Для групп файлов с одинаковым размером приложение переходит к этапу хеширования. Алгоритм читает файлы по частям, чтобы эффективно работать с большими файлами, не потребляя излишнюю память. Он использует комбинацию побитовых операций (XOR, циклические сдвиги) и умножения на простые числа для генерации уникальной сигнатуры для каждого файла на основе его содержимого.

### 3. Обнаружение дубликатов
Вычисленные хеши используются в качестве ключей. Файлы хранятся в списках, связанных с их хешем. Если список содержит более одного файла, это означает, что все файлы в нем идентичны по содержанию, и они помечаются как группа дубликатов.

### 4. Логика удаления
Список дубликатов выводятся на экран, и выбранные пользователем для удаления файлы перемещаются в корзину.

---

##  Стек технологий

*   **Язык:** Java 17
*   **Фреймворк:** JavaFX 21 (для GUI)
*   **Система сборки:** Apache Maven
*   **Тестирование:** JUnit 5

---

## Как установить и использовать

Приложение доступно в виде нативного установщика (`.msi` для Windows).

1.  **Скачайте:** Перейдите на страницу [Релизы](https://github.com/drekod18/file-deduplicator/releases) и скачайте файл `File-Deduplicator-1.0.msi`.
2.  **Установите:** Дважды щелкните по скачанному `.msi` файлу и следуйте инструкциям на экране. Приложение будет установлено, а на вашем рабочем столе и в меню "Пуск" появится ярлык. **Установка Java не требуется!**
3.  **Запустите:** Откройте приложение с помощью ярлыка.
4.  **Выберите папку:** Нажмите кнопку "Выбрать папку" и укажите директорию, которую хотите просканировать.
5.  **Сканируйте:** Нажмите кнопку "Сканировать". Индикатор прогресса покажет статус операции.
6.  **Удалите:** После завершения сканирования просмотрите список дубликатов. Отметьте галочками файлы, которые хотите удалить, и нажмите кнопку "Удалить дубликаты".

