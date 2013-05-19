/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.wire.AbstractProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireConnection;

/**
 * The {@link ProtocolDescriptor} for the Firebird version 10 protocol. This version applies to Firebird 1.x and 2.0,
 * but
 * also works with newer Firebird versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class Version10Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version10Descriptor() {
        super(
                WireProtocolConstants.PROTOCOL_VERSION10,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc,
                WireProtocolConstants.ptype_batch_send,
                1);
    }

    @Override
    public FbWireDatabase createDatabase(WireConnection connection) {
        return new V10Database(connection, this);
    }

    @Override
    public FbTransaction createTransaction(FbWireDatabase database) {
        return new V10Transaction(database);
    }
}
