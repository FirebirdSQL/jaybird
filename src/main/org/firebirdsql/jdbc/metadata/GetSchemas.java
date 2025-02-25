// SPDX-FileCopyrightText: Copyright 2001-2023 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getSchemas(String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetSchemas {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(2)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLESCHEMAS").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CATALOG", "TABLESCHEMAS").addField()
            .toRowDescriptor();

    private GetSchemas() {
    }

    public ResultSet getSchemas() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    @SuppressWarnings("unused")
    public static GetSchemas create(DbMetadataMediator mediator) {
        return new GetSchemas();
    }

}
