package dev.jpcode.kits.config;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import org.jetbrains.annotations.NotNull;

class SortedProperties extends Properties {

    public void storeSorted(Writer out, String comments) throws IOException {
        Properties sortedProps = new Properties() {
            @Override
            public @NotNull Set<Map.Entry<Object, Object>> entrySet() {
                /*
                 * Using comparator to avoid the following exception on jdk >=9:
                 * java.lang.ClassCastException: java.base/java.util.concurrent.ConcurrentHashMap$MapEntry cannot be cast to java.base/java.lang.Comparable
                 */
                Set<Map.Entry<Object, Object>> sortedSet = new TreeSet<>(Comparator.comparing(o -> o.getKey().toString())
                );
                sortedSet.addAll(super.entrySet());
                return sortedSet;
            }

            @Override
            public @NotNull Set<Object> keySet() {
                return new TreeSet<>(super.keySet());
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }

        };
        sortedProps.putAll(this);
        sortedProps.store(out, comments);
    }
}

