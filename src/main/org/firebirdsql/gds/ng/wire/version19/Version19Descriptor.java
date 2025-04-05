// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version18.Version18Descriptor;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 19 protocol. This version applies to Firebird 5.0.3, but also
 * works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public class Version19Descriptor extends Version18Descriptor implements ProtocolDescriptor {

    public Version19Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION19,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                true,
                8);
    }

    protected Version19Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(WireDatabaseConnection connection) {
        return new V19Database(connection, this);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V19Statement(database);
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V19WireOperations(connection, defaultWarningMessageCallback);
    }

}
