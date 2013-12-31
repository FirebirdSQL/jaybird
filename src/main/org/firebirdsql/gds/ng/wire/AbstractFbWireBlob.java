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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.AbstractFbBlob;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireBlob extends AbstractFbBlob implements FbWireBlob {

    protected AbstractFbWireBlob(FbWireDatabase database, FbWireTransaction transaction, long blobId) {
        super(database, transaction, blobId);
    }

    @Override
    protected FbWireDatabase getDatabase() {
        return (FbWireDatabase) super.getDatabase();
    }

    /**
     * Release this blob with the specified operation.
     * <p>
     * Implementations <strong>should only</strong> do the operation and not perform any further clean up or checks
     * on attached database and active transaction, as those checks and clean up should be done by the caller.
     * </p>
     *
     * @param releaseOperation
     *         Either {@link WireProtocolConstants#op_close_blob} or {@link WireProtocolConstants#op_cancel_blob}
     * @throws SQLException
     *         For database communication errors.
     */
    protected void releaseBlob(int releaseOperation) throws SQLException {
        synchronized (getSynchronizationObject()) {
            getDatabase().releaseObject(releaseOperation, getHandle());
        }
    }

    @Override
    protected void closeImpl() throws SQLException {
        releaseBlob(WireProtocolConstants.op_close_blob);
    }

    @Override
    protected void cancelImpl() throws SQLException {
        releaseBlob(WireProtocolConstants.op_cancel_blob);
    }
}
