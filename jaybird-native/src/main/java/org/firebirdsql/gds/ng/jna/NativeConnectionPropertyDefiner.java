// SPDX-FileCopyrightText: Copyright 2023-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi;

import java.util.stream.Stream;

import static org.firebirdsql.gds.ng.jna.NativePropertyNames.nativeLibraryPath;
import static org.firebirdsql.jaybird.props.def.ConnectionProperty.builder;

/**
 * Property definer for connection properties which exist exclusively for jaybird-native.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class NativeConnectionPropertyDefiner implements ConnectionPropertyDefinerSpi {

    @Override
    public Stream<ConnectionProperty> defineProperties() {
        return Stream.of(
                builder(nativeLibraryPath)
        ).map(ConnectionProperty.Builder::build);
    }

    @Override
    @SuppressWarnings("java:S4274")
    public void notRegistered(ConnectionProperty connectionProperty) {
        // Built-in connection properties must be registered, if they cannot be registered,
        // there is something wrong in the implementation
        assert false : "Failed to define built-in connection property: " + connectionProperty;
    }

}
