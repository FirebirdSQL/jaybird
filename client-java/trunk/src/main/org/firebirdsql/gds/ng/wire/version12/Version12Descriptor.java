/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.DefaultBlrCalculator;
import org.firebirdsql.gds.ng.wire.version10.V10InputBlob;
import org.firebirdsql.gds.ng.wire.version10.V10OutputBlob;
import org.firebirdsql.gds.ng.wire.version10.V10Transaction;

/**
 * The {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} for the Firebird version 12 protocol. This version
 * applies to Firebird 2.5, but also works with newer Firebird versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class Version12Descriptor extends AbstractProtocolDescriptor implements ProtocolDescriptor {

    public Version12Descriptor() {
        super(
                WireProtocolConstants.PROTOCOL_VERSION12,
                WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_lazy_send, // Protocol implementation expects lazy send
                WireProtocolConstants.ptype_lazy_send,
                2);
    }

    @Override
    public FbWireDatabase createDatabase(final WireConnection connection) {
        return new V12Database(connection, this);
    }

    @Override
    public FbWireTransaction createTransaction(final FbWireDatabase database, final int transactionHandle,
            final TransactionState initialState) {
        return new V10Transaction(database, transactionHandle, initialState);
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return new V12Statement(database);
    }

    @Override
    public BlrCalculator createBlrCalculator(final FbWireDatabase database) {
        final short connectionDialect = database.getConnectionDialect();
        return connectionDialect == ISCConstants.SQL_DIALECT_V6 ? DefaultBlrCalculator.CALCULATOR_DIALECT_3 : new DefaultBlrCalculator(connectionDialect);
    }

    @Override
    public FbWireBlob createOutputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        return new V10OutputBlob(database, transaction, blobParameterBuffer);
    }

    @Override
    public FbWireBlob createInputBlob(FbWireDatabase database, FbWireTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) {
        return new V10InputBlob(database, transaction, blobParameterBuffer, blobId);
    }

    @Override
    protected ParameterConverter getParameterConverter() {
        return new V12ParameterConverter();
    }
}
