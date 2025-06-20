package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CustomHashMapTest {

    private CustomHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new CustomHashMap<>();
    }

    @Test
    void testPutAndGetBasic() {
        map.put("one", 1);
        map.put("two", 2);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
    }

    @Test
    void testGetNonExistentKeyReturnsNull() {
        assertNull(map.get("non-existent"));
    }

    @Test
    void testPutOverridesExistingValue() {
        map.put("key", 100);
        assertEquals(100, map.get("key"));

        map.put("key", 200); // Перезаписываем значение
        assertEquals(200, map.get("key"));
    }

    @Test
    void testHandlesNullKey() {
        map.put(null, 555);
        assertEquals(555, map.get(null));

        map.put(null, 999); // Перезапись для null ключа
        assertEquals(999, map.get(null));
    }

    /**
     * Этот тест проверяет обработку коллизий.
     * Мы создаем два класса с одинаковым hashCode, но не равных через equals().
     * Они должны попасть в один бакет, но храниться в разных узлах связного списка.
     */
    @Test
    void testCollisionHandling() {
        class CollisionKey {
            private final String id;
            public CollisionKey(String id) { this.id = id; }
            @Override
            public int hashCode() { return 42; } // Всегда возвращаем один и тот же хеш-код
            @Override
            public boolean equals(Object obj) { // Но equals() проверяет id
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                return id.equals(((CollisionKey) obj).id);
            }
        }

        CustomHashMap<CollisionKey, String> collisionMap = new CustomHashMap<>();
        CollisionKey key1 = new CollisionKey("A");
        CollisionKey key2 = new CollisionKey("B");

        collisionMap.put(key1, "Value A");
        collisionMap.put(key2, "Value B");

        assertEquals("Value A", collisionMap.get(key1));
        assertEquals("Value B", collisionMap.get(key2));
    }

    @Test
    void testResizeFunctionality() {
        // Используем маленький capacity и load factor, чтобы легко спровоцировать resize
        CustomHashMap<Integer, String> resizeMap = new CustomHashMap<>(2, 0.75f);

        // size >= 2 * 0.75, то есть size >= 1.5. При добавлении второго элемента должен быть resize
        resizeMap.put(1, "A");
        resizeMap.put(2, "B"); // Здесь должен произойти resize до capacity 4

        // Добавляем еще, чтобы убедиться, что все работает после resize
        resizeMap.put(3, "C");
        resizeMap.put(4, "D");
        resizeMap.put(5, "E");

        assertEquals("A", resizeMap.get(1));
        assertEquals("B", resizeMap.get(2));
        assertEquals("C", resizeMap.get(3));
        assertEquals("D", resizeMap.get(4));
        assertEquals("E", resizeMap.get(5));
    }

    @Test
    void testValuesMethod() {
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        List<Integer> values = map.values();

        assertEquals(3, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
    }
}