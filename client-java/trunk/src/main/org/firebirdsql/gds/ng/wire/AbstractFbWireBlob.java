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

    protected AbstractFbWireBlob(FbWireDatabase database, FbWireTransaction transaction, long blobId, boolean output) {
        super(database, transaction, blobId, output);
    }

    @Override
    protected FbWireDatabase getDatabase() {
        return (FbWireDatabase) super.getDatabase();
    }

    /**
     * Release this blob with the specified operation.
     *
     * @param releaseOperation
     *         Either {@link WireProtocolConstants#op_close_blob} or {@link WireProtocolConstants#op_cancel_blob}
     * @throws SQLException
     */
    protected abstract void releaseBlob(int releaseOperation) throws SQLException;

    @Override
    public void close() throws SQLException {
        releaseBlob(WireProtocolConstants.op_close_blob);
    }

    @Override
    public void cancel() throws SQLException {
        releaseBlob(WireProtocolConstants.op_cancel_blob);
    }
}
