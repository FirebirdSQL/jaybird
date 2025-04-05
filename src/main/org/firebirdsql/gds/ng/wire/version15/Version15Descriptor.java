// SPDX-FileCopyrightText: Copyright 2019-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version13.Version13Descriptor;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 15 protocol. This version
 * applies to Firebird 3.0.2, but also works with newer Firebird versions.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class Version15Descriptor extends Version13Descriptor implements ProtocolDescriptor {

    public Version15Descriptor() {
        this(
                WireProtocolConstants.PROTOCOL_VERSION15,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                true,
                5);
    }

    protected Version15Descriptor(int version, int architecture, int minimumType, int maximumType,
            boolean supportsWireCompression, int weight) {
        super(version, architecture, minimumType, maximumType, supportsWireCompression, weight);
    }

    @Override
    public FbWireDatabase createDatabase(final WireDatabaseConnection connection) {
        return new V15Database(connection, this);
    }

    @Override
    public FbWireOperations createWireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback) {
        return new V15WireOperations(connection, defaultWarningMessageCallback);
    }
}
