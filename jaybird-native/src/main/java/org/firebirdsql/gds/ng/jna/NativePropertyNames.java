// SPDX-FileCopyrightText: Copyright 2023-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
