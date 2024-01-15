/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng.jna;

/**
 * Property names which are exclusively for use with jaybird-native.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@SuppressWarnings("java:S115")
public final class NativePropertyNames {

    // NOTE: Only used/works for first native or embedded connection
    public static final String nativeLibraryPath = "nativeLibraryPath";

    private NativePropertyNames() {
        // no instances
    }

}
