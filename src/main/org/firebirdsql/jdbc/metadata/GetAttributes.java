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
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getAttributes(String, String, String, String)}.
 * 
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetAttributes {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(21, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_CAT", "ATTRIBUTES").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_SCHEM", "ATTRIBUTES").addField()
            .at(2).simple(SQL_VARYING, 31, "TYPE_NAME", "ATTRIBUTES").addField()
            .at(3).simple(SQL_VARYING, 31, "ATTR_NAME", "ATTRIBUTES").addField()
            .at(4).simple(SQL_LONG, 0, "DATA_TYPE", "ATTRIBUTES").addField()
            .at(5).simple(SQL_VARYING, 31, "ATTR_TYPE_NAME", "ATTRIBUTES").addField()
            .at(6).simple(SQL_LONG, 0, "ATTR_SIZE", "ATTRIBUTES").addField()
            .at(7).simple(SQL_LONG, 0, "DECIMAL_DIGITS", "ATTRIBUTES").addField()
            .at(8).simple(SQL_LONG, 0, "NUM_PREC_RADIX", "ATTRIBUTES").addField()
            .at(9).simple(SQL_LONG, 0, "NULLABLE", "ATTRIBUTES").addField()
            .at(10).simple(SQL_VARYING, 80, "REMARKS", "ATTRIBUTES").addField()
            .at(11).simple(SQL_VARYING, 31, "ATTR_DEF", "ATTRIBUTES").addField()
            .at(12).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "ATTRIBUTES").addField()
            .at(13).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "ATTRIBUTES").addField()
            .at(14).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "ATTRIBUTES").addField()
            .at(15).simple(SQL_SHORT, 0, "ORDINAL_POSITION", "ATTRIBUTES").addField()
            .at(16).simple(SQL_VARYING, 31, "IS_NULLABLE", "ATTRIBUTES").addField()
            .at(17).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_CATALOG", "ATTRIBUTES").addField()
            .at(18).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_SCHEMA", "ATTRIBUTES").addField()
            .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_TABLE", "ATTRIBUTES").addField()
            .at(20).simple(SQL_SHORT, 0, "SOURCE_DATA_TYPE", "ATTRIBUTES").addField()
            .toRowDescriptor();

    private GetAttributes() {
    }

    public ResultSet getAttributes() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    @SuppressWarnings("unused")
    public static GetAttributes create(DbMetadataMediator mediator) {
        return new GetAttributes();
    }
}
