// SPDX-FileCopyrightText: Copyright 2019-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version15.Version15Descriptor;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 16 protocol. This version
 * applies to Firebird 4, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class Version16Descriptor extends Version15Descriptor implements ProtocolDescriptor {

    public Version16Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION16,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                true,
                6);
    }

    protected Version16Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V16Database(connection, this);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V16Statement(database);
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V16WireOperations(connection, defaultWarningMessageCallback);
    }
}
