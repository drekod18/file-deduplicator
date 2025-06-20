package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Реализация HashMap с использованием метода цепочек для разрешения коллизий.
 * Эта структура данных позволяет хранить пары "ключ-значение" и обеспечивает быстрый доступ,
 * вставку и удаление элементов в среднем за время O(1).
 *
 * @param <K> тип ключей, поддерживаемых этой картой.
 * @param <V> тип значений, отображаемых на ключи.
 */
public class CustomHashMap<K, V> {

    /**
     * Внутренний статический класс, представляющий узел в связном списке (цепочке).
     * Каждый узел хранит ключ, значение и ссылку на следующий узел в той же "корзине".
     *
     * @param <K> тип ключа.
     * @param <V> тип значения.
     */
    private static class Entry<K, V> {
        /** Ключ. Помечен как final, так как не должен меняться после создания узла. */
        final K key;
        /** Значение, связанное с ключом. Может быть обновлено. */
        V value;
        /** Ссылка на следующий узел в цепочке коллизий. */
        Entry<K, V> next;

        /**
         * Конструктор для создания нового узла.
         *
         * @param key   ключ.
         * @param value значение.
         * @param next  ссылка на следующий узел.
         */
        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    /** Начальная емкость хеш-таблицы по умолчанию, если не указана другая. */
    private static final int DEFAULT_CAPACITY = 16;
    /** Коэффициент загрузки по умолчанию. Определяет, когда нужно увеличивать размер таблицы. */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** Массив "корзин", где каждая корзина является головой связного списка узлов. */
    private Entry<K, V>[] table;
    /** Текущее количество пар "ключ-значение" в хеш-таблице. */
    private int size = 0;
    /** Коэффициент загрузки, при превышении которого происходит увеличение размера таблицы. */
    private final float loadFactor;

    /**
     * Конструктор по умолчанию. Создает хеш-таблицу с начальной емкостью и коэффициентом загрузки по умолчанию.
     */
    public CustomHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Конструктор с указанием начальной емкости и коэффициента загрузки.
     *
     * @param initialCapacity начальная емкость таблицы.
     * @param loadFactor      коэффициент загрузки.
     */
    public CustomHashMap(int initialCapacity, float loadFactor) {
        this.table = new Entry[initialCapacity];
        this.loadFactor = loadFactor;
    }

    /**
     * Добавляет или обновляет элемент в хеш-таблице.
     * Если ключ уже существует, его значение будет обновлено.
     * Если таблица слишком заполнена, ее размер будет увеличен.
     *
     * @param key   ключ, с которым будет связано значение.
     * @param value значение, которое будет связано с ключом.
     */
    public void put(K key, V value) {
        // Проверяем, не пора ли увеличить размер таблицы
        if (size >= table.length * loadFactor) {
            resize();
        }

        int index = getIndex(key);
        Entry<K, V> entry = table[index];

        // Проходим по цепочке в поисках существующего ключа
        while (entry != null) {
            if (Objects.equals(entry.key, key)) {
                entry.value = value; // Ключ найден, обновляем значение и выходим
                return;
            }
            entry = entry.next;
        }

        // Если ключ не найден, создаем новый узел и добавляем его в начало цепочки
        Entry<K, V> newEntry = new Entry<>(key, value, table[index]);
        table[index] = newEntry;
        size++;
    }

    /**
     * Возвращает значение, связанное с указанным ключом, или null, если ключ не найден.
     *
     * @param key ключ, значение для которого нужно получить.
     * @return значение, связанное с ключом, или null.
     */
    public V get(K key) {
        int index = getIndex(key);
        Entry<K, V> entry = table[index];

        // Проходим по цепочке в поисках нужного ключа
        while (entry != null) {
            if (Objects.equals(entry.key, key)) {
                return entry.value; // Ключ найден, возвращаем значение
            }
            entry = entry.next;
        }

        return null; // Ключ не найден
    }

    /**
     * Возвращает список всех значений, хранящихся в хеш-таблице.
     * Порядок значений не гарантируется.
     *
     * @return список всех значений.
     */
    public List<V> values() {
        List<V> values = new LinkedList<>();
        // Проходим по каждой корзине
        for (Entry<K, V> entry : table) {
            // Проходим по всей цепочке в этой корзине
            while (entry != null) {
                values.add(entry.value);
                entry = entry.next;
            }
        }
        return values;
    }

    /**
     * Вспомогательный метод для вычисления индекса корзины для заданного ключа.
     *
     * @param key ключ.
     * @return индекс в массиве `table`.
     */
    private int getIndex(K key) {
        if (key == null) return 0; // null ключи всегда кладем в 0-ую корзину
        // Используем остаток от деления хеш-кода на размер таблицы.
        // Math.abs() нужен, чтобы избежать отрицательных индексов, т.к. hashCode() может быть отрицательным.
        return Math.abs(key.hashCode()) % table.length;
    }

    /**
     * Вспомогательный метод для увеличения размера таблицы (перехеширования).
     * Вызывается, когда количество элементов превышает (емкость * коэффициент загрузки).
     * Создает новую таблицу вдвое большего размера и переносит в нее все старые элементы.
     */
    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = new Entry[oldTable.length * 2]; // Удваиваем емкость
        size = 0; // Сбрасываем размер, так как put будет его инкрементировать

        // Проходим по старой таблице и перехешируем все элементы в новую
        for (Entry<K, V> entry : oldTable) {
            while (entry != null) {
                put(entry.key, entry.value); // Метод put сам найдет правильный новый индекс
                entry = entry.next;
            }
        }
    }
}