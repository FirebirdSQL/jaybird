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
