/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.InlineBlob;
import org.firebirdsql.gds.ng.wire.InlineBlobResponse;
import org.firebirdsql.gds.ng.wire.version18.V18Statement;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 */
public class V19Statement extends V18Statement {

    /**
     * Creates a new instance of V19Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V19Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected void sendExecuteMsg(XdrOutputStream xdrOut, int operation, RowValue parameters)
            throws IOException, SQLException {
        super.sendExecuteMsg(xdrOut, operation, parameters);
        xdrOut.writeInt(getMaxInlineBlobSize()); //p_sqldata_inline_blob_size
    }

    protected int getMaxInlineBlobSize() {
        return getDatabase().getConnectionProperties().getMaxInlineBlobSize();
    }

    @Override
    protected void handleInlineBlobResponse(InlineBlobResponse inlineBlobResponse) {
        FbWireDatabase database = getDatabase();
        if (database instanceof V19Database) {
            InlineBlob inlineBlob = inlineBlobResponse.toInlineBlob(database);
            ((V19Database) database).registerInlineBlob(getTransaction(), inlineBlob);
        }
    }

}
