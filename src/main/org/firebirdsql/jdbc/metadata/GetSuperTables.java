// SPDX-FileCopyrightText: Copyright 2001-2024 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getSuperTables(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetSuperTables {

    private static final String SUPERTABLES = "SUPERTABLES";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(4)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", SUPERTABLES).addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", SUPERTABLES).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", SUPERTABLES).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTABLE_NAME", SUPERTABLES).addField()
            .toRowDescriptor();

    private GetSuperTables() {
    }

    public ResultSet getSuperTables() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, Collections.emptyList());
    }

    @SuppressWarnings("unused")
    public static GetSuperTables create(DbMetadataMediator mediator) {
        return new GetSuperTables();
    }
}
