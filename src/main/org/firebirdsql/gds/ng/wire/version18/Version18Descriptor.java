// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version16.Version16Descriptor;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 18 protocol. This version
 * applies to Firebird 5, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class Version18Descriptor extends Version16Descriptor implements ProtocolDescriptor {

    public Version18Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION18,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                true,
                7);
    }

    protected Version18Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(WireDatabaseConnection connection) {
        return new V18Database(connection, this);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V18Statement(database);
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V18WireOperations(connection, defaultWarningMessageCallback);
    }
}
