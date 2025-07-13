// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Best-effort string deduplicator.
 * <p>
 * Given this class uses an LRU-evicted map with a maximum capacity internally, 100% deduplication of strings is
 * <em>not</em> guaranteed.
 * </p>
 * <p>
 * This class is <em>not</em> thread-safe.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 7
 */
public final class StringDeduplicator {

    private static final int DEFAULT_MAX_CAPACITY = 50;

    private final Map<String, String> cache;

    private StringDeduplicator(int maxCapacity, Collection<String> preset) {
        cache = new StringCache(maxCapacity);
        // NOTE: if preset.size() is greater than maxCapacity, prefix will be immediately evicted
        preset.forEach(v -> cache.put(v, v));
    }

    /**
     * Deduplicates this value if already cached, otherwise caches and returns {@code value}.
     *
     * @param value
     *         value to deduplicate (can be {@code null})
     * @return previous cached value equal to {@code value}, or {@code value} itself
     */
    public @Nullable String get(@Nullable String value) {
        if (value == null) return null;
        if (value.isEmpty()) return "";
        return cache.computeIfAbsent(value, Function.identity());
    }

    /**
     * @return a string deduplicator with a default max capacity
     */
    public static StringDeduplicator of() {
        return of(DEFAULT_MAX_CAPACITY, List.of());
    }

    /**
     * @param preset
     *         values to initially add to the cache
     * @return a string deduplicator with a default max capacity, initialized with {@code preset}
     */
    public static StringDeduplicator of(String... preset) {
        return of(Arrays.asList(preset));
    }

    /**
     * @param preset
     *         values to initially add to the cache
     * @return a string deduplicator with a default max capacity, initialized with {@code preset}
     */
    public static StringDeduplicator of(Collection<String> preset) {
        return of(DEFAULT_MAX_CAPACITY, preset);
    }

    /**
     * @param maxCapacity
     *         maximum capacity
     * @param preset
     *         values to initially add to the cache
     * @return a string deduplicator with max capacity {@code maxCapacity}, initialized with {@code preset}
     */
    public static StringDeduplicator of(int maxCapacity, String... preset) {
        return of(maxCapacity, Arrays.asList(preset));
    }

    /**
     * @param maxCapacity
     *         maximum capacity
     * @param preset
     *         values to initially add to the cache
     * @return a string deduplicator with max capacity {@code maxCapacity}, initialized with {@code preset}
     */
    public static StringDeduplicator of(int maxCapacity, Collection<String> preset) {
        return new StringDeduplicator(maxCapacity, preset);
    }

    /**
     * String cache with LRU (Least-Recently Used) eviction order.
     */
    private static final class StringCache extends LinkedHashMap<String, String> {

        @Serial
        private static final long serialVersionUID = -201579959301651333L;

        private final int maxCapacity;

        private StringCache(int maxCapacity) {
            super(initialCapacity(maxCapacity), 0.75f, true);
            if (maxCapacity <= 0) {
                throw new IllegalArgumentException("maxCapacity must be greater than 0, was: " + maxCapacity);
            }
            this.maxCapacity = maxCapacity;
        }

        private static int initialCapacity(int maxCapacity) {
            return Math.max(8, maxCapacity / 2);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > maxCapacity;
        }

    }

}
