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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireOutputBlob extends AbstractFbWireBlob {

    protected AbstractFbWireOutputBlob(FbWireDatabase database, FbWireTransaction transaction) {
        super(database, transaction, FbBlob.NO_BLOB_ID);
    }

    @Override
    public final boolean isOutput() {
        return true;
    }

    @Override
    public final byte[] getSegment(int sizeRequested) throws SQLException {
        throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_read).toSQLException();
    }

    @Override
    public final void seek(int offset, SeekMode seekMode) throws SQLException {
        // This assumes seeks are not (nor in the future) supported on output blobs
        throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_read).toSQLException();
    }
}
