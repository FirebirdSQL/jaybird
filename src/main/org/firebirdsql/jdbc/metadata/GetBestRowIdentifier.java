// SPDX-FileCopyrightText: Copyright 2001-2025 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
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
public abstract class GetBestRowIdentifier extends AbstractMetadataMethod {

    private static final String ROWIDENTIFIER = "ROWIDENTIFIER";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(8)
            .at(0).simple(SQL_SHORT, 0, "SCOPE", ROWIDENTIFIER).addField()
            .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", ROWIDENTIFIER).addField()
            .at(2).simple(SQL_LONG, 0, "DATA_TYPE", ROWIDENTIFIER).addField()
            .at(3).simple(SQL_VARYING, 31, "TYPE_NAME", ROWIDENTIFIER).addField()
            .at(4).simple(SQL_LONG, 0, "COLUMN_SIZE", ROWIDENTIFIER).addField()
            .at(5).simple(SQL_LONG, 0, "BUFFER_LENGTH", ROWIDENTIFIER).addField()
            .at(6).simple(SQL_SHORT, 0, "DECIMAL_DIGITS", ROWIDENTIFIER).addField()
            .at(7).simple(SQL_SHORT, 0, "PSEUDO_COLUMN", ROWIDENTIFIER).addField()
            .toRowDescriptor();

    private GetBestRowIdentifier(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    @SuppressWarnings("unused")
    public final ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        if (isNullOrEmpty(table)) {
            return createEmpty();
        }

        RowValueBuilder valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
        List<RowValue> rows = getPrimaryKeyIdentifier(schema, table, valueBuilder);

        // if no primary key exists, add RDB$DB_KEY as pseudo-column
        if (rows.isEmpty()) {
            FBDatabaseMetaData dbmd = mediator.getMetaData();
            // NOTE: Currently is always ROWID_VALID_TRANSACTION
            RowIdLifetime rowIdLifetime = dbmd.getRowIdLifetime();
            if (rowIdLifetime == RowIdLifetime.ROWID_VALID_TRANSACTION && scope == DatabaseMetaData.bestRowSession) {
                // consider RDB$DB_KEY scope transaction
                return createEmpty();
            }

            try (ResultSet pseudoColumns = dbmd.getPseudoColumns(
                    catalog, escapeWildcards(schema), escapeWildcards(table), "RDB$DB\\_KEY")) {
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
                        .at(4).setInt(pseudoColumns.getInt(6))
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
     * @param schema
     *         name of the schema
     * @param table
     *         name of the table
     * @param valueBuilder
     *         builder for row values
     * @return list of result set values, when empty, no primary key has been defined for a table or the table does not
     * exist. The returned list can be modified by caller if needed.
     * @throws SQLException
     *         if something went wrong.
     */
    private List<RowValue> getPrimaryKeyIdentifier(String schema, String table, RowValueBuilder valueBuilder)
            throws SQLException {
        MetadataQuery metadataQuery = createGetPrimaryKeyIdentifierQuery(schema, table);
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            List<RowValue> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(createMetadataRow(rs, valueBuilder));
            }
            return rows;
        }
    }

    abstract MetadataQuery createGetPrimaryKeyIdentifierQuery(String schema, String table);

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
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (mediator.getFirebirdSupportInfo().isVersionEqualOrAbove(6)) {
            return FB6.createInstance(mediator);
        } else {
            return FB5.createInstance(mediator);
        }
    }

    /**
     * Implementation for Firebird 5.0 and older.
     */
    private static final class FB5 extends GetBestRowIdentifier {

        //@formatter:off
        private static final String GET_BEST_ROW_IDENT_START = """
                select
                  RF.RDB$FIELD_NAME as COLUMN_NAME,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + "\n" + """
                from RDB$RELATION_CONSTRAINTS RC
                inner join RDB$INDEX_SEGMENTS IDX
                  on IDX.RDB$INDEX_NAME = RC.RDB$INDEX_NAME
                inner join RDB$RELATION_FIELDS RF
                  on RF.RDB$FIELD_NAME = IDX.RDB$FIELD_NAME and RF.RDB$RELATION_NAME = RC.RDB$RELATION_NAME
                inner join RDB$FIELDS F
                  on F.RDB$FIELD_NAME = RF.RDB$FIELD_SOURCE
                where RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY'
                and\s""";
        //@formatter:on

        private static final String GET_BEST_ROW_IDENT_END = "\norder by IDX.RDB$FIELD_POSITION";


        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetBestRowIdentifier createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createGetPrimaryKeyIdentifierQuery(String schema, String table) {
            Clause tableClause = Clause.equalsClause("RC.RDB$RELATION_NAME", table);
            String sql = GET_BEST_ROW_IDENT_START
                    + tableClause.getCondition(false)
                    + GET_BEST_ROW_IDENT_END;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }
    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetBestRowIdentifier {

        //@formatter:off
        private static final String GET_BEST_ROW_IDENT_START_6 = """
                select
                  trim(trailing from RF.RDB$FIELD_NAME) as COLUMN_NAME,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + "\n" + """
                from SYSTEM.RDB$RELATION_CONSTRAINTS RC
                inner join SYSTEM.RDB$INDEX_SEGMENTS IDX
                  on IDX.RDB$SCHEMA_NAME = RC.RDB$SCHEMA_NAME and IDX.RDB$INDEX_NAME = RC.RDB$INDEX_NAME
                inner join SYSTEM.RDB$RELATION_FIELDS RF
                  on RF.RDB$FIELD_NAME = IDX.RDB$FIELD_NAME and RF.RDB$SCHEMA_NAME = RC.RDB$SCHEMA_NAME and RF.RDB$RELATION_NAME = RC.RDB$RELATION_NAME
                inner join SYSTEM.RDB$FIELDS F
                  on F.RDB$SCHEMA_NAME = RF.RDB$FIELD_SOURCE_SCHEMA_NAME and F.RDB$FIELD_NAME = RF.RDB$FIELD_SOURCE
                where RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY'
                and\s""";
        //@formatter:on

        // The order by schema name is to ensure a consistent order when this is called with schema = null, as that will
        // not narrow the search by schema, so can return columns of multiple same named tables in different schemas.
        private static final String GET_BEST_ROW_IDENT_END_6 = "\norder by RC.RDB$SCHEMA_NAME, IDX.RDB$FIELD_POSITION";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetBestRowIdentifier createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetPrimaryKeyIdentifierQuery(String schema, String table) {
            List<Clause> clauses = new ArrayList<>(2);
            if (schema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("RC.RDB$SCHEMA_NAME", schema));
            }
            clauses.add(Clause.equalsClause("RC.RDB$RELATION_NAME", table));
            String sql = GET_BEST_ROW_IDENT_START_6
                    + Clause.conjunction(clauses)
                    + GET_BEST_ROW_IDENT_END_6;

            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
