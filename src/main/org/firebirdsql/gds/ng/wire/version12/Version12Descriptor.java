// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version11.Version11Descriptor;

/**
 * The {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} for the Firebird version 12 protocol. This version
 * applies to Firebird 2.5, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class Version12Descriptor extends Version11Descriptor implements ProtocolDescriptor {

    public Version12Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION12,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                false,
                3);
    }

    protected Version12Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V12Database(connection, this);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V12Statement(database);
    }

    @Override
    protected ParameterConverter<WireDatabaseConnection, WireServiceConnection> getParameterConverter() {
        return new V12ParameterConverter();
    }

}
