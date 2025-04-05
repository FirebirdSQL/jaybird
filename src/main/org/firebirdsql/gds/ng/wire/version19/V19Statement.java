// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version19;

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
    protected void sendExecute(int operation, RowValue parameters) throws IOException, SQLException {
        super.sendExecute(operation, parameters);
        getXdrOut().writeInt(getMaxInlineBlobSize()); //p_sqldata_inline_blob_size
    }

    protected int getMaxInlineBlobSize() {
        return getDatabase().getConnectionProperties().getMaxInlineBlobSize();
    }

    @Override
    protected void handleInlineBlobResponse(InlineBlobResponse inlineBlobResponse) {
        if (getDatabase() instanceof V19Database database) {
            InlineBlob inlineBlob = inlineBlobResponse.toInlineBlob(database);
            database.registerInlineBlob(getTransaction(), inlineBlob);
        }
    }

}
