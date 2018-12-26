package com.indix.gocd.utils.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Maps {
    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<K, V>();
    }
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map.isEmpty();
    }

    public static class MapBuilder<K, V> {
        private Map<K, V> internal = new HashMap<K, V>();

        public MapBuilder<K, V> with(K key, V value) {
            internal.put(key, value);
            return this;
        }

        public MapBuilder<K, V> remove(K key) {
            internal.remove(key);
            return this;
        }

        public Map<K, V> build() {
            return Collections.unmodifiableMap(internal);
        }
    }
}
