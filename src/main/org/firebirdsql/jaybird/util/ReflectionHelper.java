// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class containing static helper methods for reflective access.
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
@InternalApi
public final class ReflectionHelper {

    private ReflectionHelper() {
    }

    /**
     * Get all implemented interfaces by the class.
     *
     * @param clazz
     *         class to inspect.
     * @return array of all implemented interfaces.
     */
    public static Class<?>[] getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();
        do {
            Collections.addAll(result, clazz.getInterfaces());
            clazz = clazz.getSuperclass();
        } while (clazz.getSuperclass() != null); // Scan until clazz is Object (so skip Object itself)
        return result.toArray(new Class<?>[0]);
    }

    /**
     * Helper function to find specified method in a specified class.
     *
     * @param clazz
     *         class in which we look for a specified method.
     * @param name
     *         name of the method.
     * @param args
     *         types of method params.
     * @return instance of {@link Method} corresponding to specified name and
     * param types.
     */
    public static @Nullable Method findMethod(Class<?> clazz, String name, Class<?>[] args) {
        try {
            return clazz.getMethod(name, args);
        } catch (NoSuchMethodException nmex) {
            return null;
        }
    }

}
