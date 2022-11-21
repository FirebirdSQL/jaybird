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

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getUDTs(String, String, String, int[])}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class GetUDTs {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(7, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "UDT").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "UDT").addField()
            .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "UDT").addField()
            .at(3).simple(SQL_VARYING, 31, "CLASS_NAME", "UDT").addField()
            .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "UDT").addField()
            .at(5).simple(SQL_VARYING, 31, "REMARKS", "UDT").addField()
            .at(6).simple(SQL_SHORT, 0, "BASE_TYPE", "UDT").addField()
            .toRowDescriptor();

    private GetUDTs() {
    }

    public ResultSet getUDTs() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }


    @SuppressWarnings("unused")
    public static GetUDTs create(DbMetadataMediator mediator) {
        return new GetUDTs();
    }

}
