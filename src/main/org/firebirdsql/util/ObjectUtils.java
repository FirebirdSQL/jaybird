/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.util;

import java.util.Arrays;

/**
 * Helper methods for comparing objects.
 * <p>
 * This class provides similar functionality as <code>java.util.Objects</code> in Java 7, but provided as we
 * want to support Java 6 as well (nor do we want to depend on commons-lang)
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class ObjectUtils {

    private ObjectUtils() {
        // Prevent instantiation
    }

    /**
     * Returns <code>true</code> if both arguments are equal or both are <code>null</code>. Returns <code>false</code>
     * if one argument is null. Otherwise uses {@link Object#equals(Object)} of the first argument.
     *
     * @param a
     *         object
     * @param b
     *         second object
     * @return <code>true</code> if equal, <code>false</code> otherwise
     */
    public static boolean equals(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    /**
     * Returns the {@link Object#hashCode()} for the supplied object or 0 if the object is <code>null</code>.
     *
     * @param obj
     *         object
     * @return The <code>hashCode</code>
     */
    public static int hashCode(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }

    /**
     * Generates a hash code for a collection of objects using {@link Arrays#hashCode(Object[])}.
     *
     * @param objects
     *         Collection of objects
     * @return A hash value
     */
    public static int hash(Object... objects) {
        return Arrays.hashCode(objects);
    }
}
