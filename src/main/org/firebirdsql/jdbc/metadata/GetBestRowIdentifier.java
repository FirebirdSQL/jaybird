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
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.CS_BINARY;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.char_type;
import static org.firebirdsql.jdbc.metadata.MetadataPattern.escapeWildcards;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHARSET_ID;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHAR_LEN;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_LENGTH;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_PRECISION;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SCALE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SUB_TYPE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_TYPE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.getDataTypeName;

/**
 * Provides the implementation of
 * {@link java.sql.DatabaseMetaData#getBestRowIdentifier(String, String, String, int, boolean)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetBestRowIdentifier extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(8)
            .at(0).simple(SQL_SHORT, 0, "SCOPE", "ROWIDENTIFIER").addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "ROWIDENTIFIER").addField()
            .at(2).simple(SQL_LONG, 0, "DATA_TYPE", "ROWIDENTIFIER").addField()
            .at(3).simple(SQL_VARYING, 31, "TYPE_NAME", "ROWIDENTIFIER").addField()
            .at(4).simple(SQL_LONG, 0, "COLUMN_SIZE", "ROWIDENTIFIER").addField()
            .at(5).simple(SQL_LONG, 0, "BUFFER_LENGTH", "ROWIDENTIFIER").addField()
            .at(6).simple(SQL_SHORT, 0, "DECIMAL_DIGITS", "ROWIDENTIFIER").addField()
            .at(7).simple(SQL_SHORT, 0, "PSEUDO_COLUMN", "ROWIDENTIFIER").addField()
            .toRowDescriptor();

    //@formatter:off
    private static final String GET_BEST_ROW_IDENT_START =
            "select\n"
            + "  RF.RDB$FIELD_NAME as COLUMN_NAME,\n"
            + "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n"
            + "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n"
            + "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n"
            + "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n"
            + "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n"
            + "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n"
            + "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + "\n"
            + "from RDB$RELATION_CONSTRAINTS RC\n"
            + "inner join RDB$INDEX_SEGMENTS IDX\n"
            + "  on IDX.RDB$INDEX_NAME = RC.RDB$INDEX_NAME\n"
            + "inner join RDB$RELATION_FIELDS RF\n"
            + "  on RF.RDB$FIELD_NAME = IDX.RDB$FIELD_NAME and RF.RDB$RELATION_NAME = RC.RDB$RELATION_NAME\n"
            + "inner join RDB$FIELDS F\n"
            + "  on F.RDB$FIELD_NAME = RF.RDB$FIELD_SOURCE\n"
            + "where RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY'\n"
            + "and ";

    private static final String GET_BEST_ROW_IDENT_END =
            "\norder by IDX.RDB$FIELD_POSITION";
    //@formatter:on

    private GetBestRowIdentifier(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        if (table == null || "".equals(table)) {
            return createEmpty();
        }

        RowValueBuilder valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
        List<RowValue> rows = getPrimaryKeyIdentifier(table, valueBuilder);

        // if no primary key exists, add RDB$DB_KEY as pseudo-column
        if (rows.size() == 0) {
            FBDatabaseMetaData dbmd = mediator.getMetaData();
            // NOTE: Currently is always ROWID_VALID_TRANSACTION
            RowIdLifetime rowIdLifetime = dbmd.getRowIdLifetime();
            if (rowIdLifetime == RowIdLifetime.ROWID_VALID_TRANSACTION && scope == DatabaseMetaData.bestRowSession) {
                // consider RDB$DB_KEY scope transaction
                return createEmpty();
            }

            try (ResultSet pseudoColumns =
                         dbmd.getPseudoColumns(catalog, schema, escapeWildcards(table), "RDB$DB\\_KEY")) {
                if (!pseudoColumns.next()) {
                    return createEmpty();
                }
                rows.add(valueBuilder
                        .at(0).setShort(
                                rowIdLifetime == RowIdLifetime.ROWID_VALID_TRANSACTION
                                        ? DatabaseMetaData.bestRowTransaction
                                        : DatabaseMetaData.bestRowSession)
                        .at(1).setString("RDB$DB_KEY")
                        .at(2).setInt(Types.ROWID)
                        .at(3).setString(getDataTypeName(char_type, 0, CS_BINARY))
                        .at(4).setInt(pseudoColumns.getInt(8))
                        .at(5).set(null)
                        .at(6).set(null)
                        .at(7).setShort(DatabaseMetaData.bestRowPseudo)
                        .toRowValue(false));
            }
        }

        return new FBResultSet(ROW_DESCRIPTOR, rows);
    }

    /**
     * Get primary key of the table as best row identifier.
     *
     * @param table
     *         name of the table.
     * @param valueBuilder
     *         builder for row values
     * @return list of result set values, when empty, no primary key has been defined for a table or the table does not
     * exist. The returned list can be modified by caller if needed.
     * @throws SQLException
     *         if something went wrong.
     */
    private List<RowValue> getPrimaryKeyIdentifier(String table, RowValueBuilder valueBuilder) throws SQLException {
        Clause tableClause = Clause.equalsClause("RC.RDB$RELATION_NAME", table);
        String sql = GET_BEST_ROW_IDENT_START
                + tableClause.getCondition(false)
                + GET_BEST_ROW_IDENT_END;

        MetadataQuery metadataQuery = new MetadataQuery(sql, Clause.parameters(tableClause));
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            List<RowValue> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(createMetadataRow(rs, valueBuilder));
            }
            return rows;
        }
    }

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                .fromCurrentRow(rs)
                .build();

        return valueBuilder
                .at(0).setShort(DatabaseMetaData.bestRowSession)
                .at(1).setString(rs.getString("COLUMN_NAME"))
                .at(2).setShort(typeMetadata.getJdbcType())
                .at(3).setString(typeMetadata.getSqlTypeName())
                .at(4).setInt(typeMetadata.getColumnSize())
                .at(5).set(null)
                .at(6).setShort(typeMetadata.getScale())
                .at(7).setShort(DatabaseMetaData.bestRowNotPseudo)
                .toRowValue(false);
    }

    public static GetBestRowIdentifier create(DbMetadataMediator mediator) {
        return new GetBestRowIdentifier(mediator);
    }
}
