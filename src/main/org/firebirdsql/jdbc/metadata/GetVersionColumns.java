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
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.MetadataPattern.escapeWildcards;

/**
 * @author Mark Rotteveel
 */
public class GetVersionColumns {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(8, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_SHORT, 0, "SCOPE", "VERSIONCOL").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "VERSIONCOL").addField()
            .at(2).simple(SQL_LONG, 0, "DATA_TYPE", "VERSIONCOL").addField()
            .at(3).simple(SQL_VARYING, 31, "TYPE_NAME", "VERSIONCOL").addField()
            .at(4).simple(SQL_LONG, 0, "COLUMN_SIZE", "VERSIONCOL").addField()
            .at(5).simple(SQL_LONG, 0, "BUFFER_LENGTH", "VERSIONCOL").addField()
            .at(6).simple(SQL_SHORT, 0, "DECIMAL_DIGITS", "VERSIONCOL").addField()
            .at(7).simple(SQL_SHORT, 0, "PSEUDO_COLUMN", "VERSIONCOL").addField()
            .toRowDescriptor();

    private final DbMetadataMediator mediator;

    private GetVersionColumns(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        if (table == null || "".equals(table)) {
            return new FBResultSet(ROW_DESCRIPTOR, emptyList());
        }

        FBDatabaseMetaData dbmd = mediator.getMetaData();
        try (ResultSet pseudoColumns = dbmd.getPseudoColumns(catalog, schema, escapeWildcards(table), "%")) {
            if (!pseudoColumns.next()) {
                return new FBResultSet(ROW_DESCRIPTOR, emptyList());
            }

            List<RowValue> rowValues = new ArrayList<>(2);
            RowValueBuilder rowValueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
            do {
                String columnName = pseudoColumns.getString(4);
                boolean isDbKey = "RDB$DB_KEY".equals(columnName);
                boolean isRecordVersion = !isDbKey && "RDB$RECORD_VERSION".equals(columnName);
                // Protect against future addition of other pseudo columns
                if (!(isDbKey || isRecordVersion)) continue;

                rowValueBuilder
                        .at(0).set(null)
                        .at(1).setString(columnName)
                        .at(2).setInt(pseudoColumns.getInt(5))
                        .at(3).setString(isDbKey ? "CHAR" : "BIGINT")
                        .at(4).setInt(pseudoColumns.getInt(6))
                        .at(5).setInt(isDbKey ? pseudoColumns.getInt(11) : 8)
                        .at(6).setShort(isRecordVersion ? 0 : null)
                        .at(7).setShort(DatabaseMetaData.versionColumnPseudo);
                rowValues.add(rowValueBuilder.toRowValue(false));
            } while (pseudoColumns.next());

            return new FBResultSet(ROW_DESCRIPTOR, rowValues);
        }
    }

    public static GetVersionColumns create(DbMetadataMediator mediator) {
        return new GetVersionColumns(mediator);
    }

}
