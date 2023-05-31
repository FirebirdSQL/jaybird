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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
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

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(4, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "SUPERTABLES").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "SUPERTABLES").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "SUPERTABLES").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SUPERTABLE_NAME", "SUPERTABLES").addField()
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
