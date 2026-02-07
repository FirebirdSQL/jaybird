// SPDX-FileCopyrightText: Copyright 2001-2026 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.jspecify.annotations.Nullable;

import java.sql.PseudoColumnUsage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.BIGINT_PRECISION;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getPseudoColumns(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetPseudoColumns {

    private static final String PSEUDOCOLUMNS = "PSEUDOCOLUMNS";
    public static final String COLUMN_SCHEMA_NAME = "RDB$SCHEMA_NAME";
    public static final String COLUMN_RELATION_NAME = "RDB$RELATION_NAME";

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(12)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", PSEUDOCOLUMNS).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", PSEUDOCOLUMNS).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", PSEUDOCOLUMNS).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", PSEUDOCOLUMNS).addField()
            .at(4).simple(SQL_LONG, 0, "DATA_TYPE", PSEUDOCOLUMNS).addField()
            .at(5).simple(SQL_LONG, 0, "COLUMN_SIZE", PSEUDOCOLUMNS).addField()
            .at(6).simple(SQL_LONG | 1, 0, "DECIMAL_DIGITS", PSEUDOCOLUMNS).addField()
            .at(7).simple(SQL_LONG, 0, "NUM_PREC_RADIX", PSEUDOCOLUMNS).addField()
            .at(8).simple(SQL_VARYING, 50, "COLUMN_USAGE", PSEUDOCOLUMNS).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(9).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", PSEUDOCOLUMNS).addField()
            .at(10).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", PSEUDOCOLUMNS).addField()
            .at(11).simple(SQL_VARYING, 3, "IS_NULLABLE", PSEUDOCOLUMNS).addField()
            .toRowDescriptor();

    private static final String DB_KEY_REMARK = "The RDB$DB_KEY column in a select list will be renamed by Firebird to "
            + "DB_KEY in the result set (both as column name and label). Result set getters in Jaybird will map this, "
            + "but in introspection of ResultSetMetaData, DB_KEY will be reported. Identification as a Types.ROWID "
            + "will only work in a select list (ResultSetMetaData), not for parameters (ParameterMetaData), but "
            + "Jaybird will allow setting a RowId value.";

    private final DbMetadataMediator mediator;

    private GetPseudoColumns(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    public ResultSet getPseudoColumns(@Nullable String schemaPattern, @Nullable String tableNamePattern,
            @Nullable String columnNamePattern) throws SQLException {
        if ("".equals(tableNamePattern) || "".equals(columnNamePattern)) {
            // Matching table and/or column not possible
            return createEmpty();
        }

        MetadataPatternMatcher matcher = MetadataPattern.compile(columnNamePattern).toMetadataPatternMatcher();
        boolean retrieveDbKey = matcher.matches("RDB$DB_KEY");
        boolean retrieveRecordVersion = supportsRecordVersion() && matcher.matches("RDB$RECORD_VERSION");

        if (!(retrieveDbKey || retrieveRecordVersion)) {
            // No matching columns
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetPseudoColumnsQuery(schemaPattern, tableNamePattern);
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            if (!rs.next()) {
                return createEmpty();
            }

            var rows = new ArrayList<RowValue>();
            var valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
            do {
                String schema = rs.getString(COLUMN_SCHEMA_NAME);
                String tableName = rs.getString(COLUMN_RELATION_NAME);

                if (retrieveDbKey) {
                    int dbKeyLength = rs.getInt("RDB$DBKEY_LENGTH");
                    valueBuilder
                            .at(1).setString(schema)
                            .at(2).setString(tableName)
                            .at(3).setString("RDB$DB_KEY")
                            .at(4).setInt(Types.ROWID)
                            .at(5).setInt(dbKeyLength)
                            .at(7).setInt(10)
                            .at(8).setString(PseudoColumnUsage.NO_USAGE_RESTRICTIONS.name())
                            .at(9).setString(DB_KEY_REMARK)
                            .at(10).setInt(dbKeyLength)
                            .at(11).setString("NO");
                    rows.add(valueBuilder.toRowValue(true));
                }

                if (retrieveRecordVersion && rs.getBoolean("HAS_RECORD_VERSION")) {
                    valueBuilder
                            .at(1).setString(schema)
                            .at(2).setString(tableName)
                            .at(3).setString("RDB$RECORD_VERSION")
                            .at(4).setInt(Types.BIGINT)
                            .at(5).setInt(BIGINT_PRECISION)
                            .at(6).setInt(0)
                            .at(7).setInt(10)
                            .at(8).setString(PseudoColumnUsage.NO_USAGE_RESTRICTIONS.name())
                            .at(11).setString(rs.getString("RECORD_VERSION_NULLABLE"));
                    rows.add(valueBuilder.toRowValue(true));
                }
            } while (rs.next());

            return new FBResultSet(ROW_DESCRIPTOR, rows);
        }
    }

    abstract boolean supportsRecordVersion();

    abstract MetadataQuery createGetPseudoColumnsQuery(@Nullable String schemaPattern,
            @Nullable String tableNamePattern);

    private ResultSet createEmpty() throws SQLException {
        return new FBResultSet(ROW_DESCRIPTOR, emptyList());
    }

    public static GetPseudoColumns create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(6)) {
            return FB6.createInstance(mediator);
        } else if (firebirdSupportInfo.isVersionEqualOrAbove(3)) {
            return FB3.createInstance(mediator);
        } else {
            return FB2_5.createInstance(mediator);
        }
    }

    @SuppressWarnings("java:S101")
    private static final class FB2_5 extends GetPseudoColumns {

        private static final String GET_PSEUDO_COLUMNS_FRAGMENT_2_5 = """
                select
                 cast(null as char(1)) as RDB$SCHEMA_NAME,
                 RDB$RELATION_NAME,
                 RDB$DBKEY_LENGTH,
                 'F' AS HAS_RECORD_VERSION,
                 -- unknown nullability (and doesn't matter, no RDB$RECORD_VERSION)
                 '' AS RECORD_VERSION_NULLABLE
                from RDB$RELATIONS""";

        private static final String GET_PSEUDO_COLUMNS_END_2_5 = "\norder by RDB$RELATION_NAME";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetPseudoColumns createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        boolean supportsRecordVersion() {
            return false;
        }

        @Override
        MetadataQuery createGetPseudoColumnsQuery(@Nullable String schemaPattern, @Nullable String tableNamePattern) {
            var tableNameClause = new Clause(COLUMN_RELATION_NAME, tableNamePattern);
            String sql = GET_PSEUDO_COLUMNS_FRAGMENT_2_5
                    + tableNameClause.getCondition("\nwhere ", "")
                    + GET_PSEUDO_COLUMNS_END_2_5;
            return new MetadataQuery(sql, Clause.parameters(tableNameClause));
        }

    }

    private static final class FB3 extends GetPseudoColumns {

        private static final String GET_PSEUDO_COLUMNS_FRAGMENT_3 = """
                select
                  null as RDB$SCHEMA_NAME,
                  trim(trailing from RDB$RELATION_NAME) as RDB$RELATION_NAME,
                  RDB$DBKEY_LENGTH,
                  RDB$DBKEY_LENGTH = 8 as HAS_RECORD_VERSION,
                  case
                    -- table, view, GTT preserve + delete: never null
                    when RDB$RELATION_TYPE in (0, 1, 4, 5) then 'NO'
                    -- external + virtual: always null
                    when RDB$RELATION_TYPE in (2, 3) then 'YES'
                    -- unknown or unsupported (by Jaybird) type: unknown nullability
                    else ''
                  end as RECORD_VERSION_NULLABLE
                from RDB$RELATIONS""";

        private static final String GET_PSEUDO_COLUMNS_END_3 = "\norder by RDB$RELATION_NAME";

        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetPseudoColumns createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        boolean supportsRecordVersion() {
            return true;
        }

        @Override
        MetadataQuery createGetPseudoColumnsQuery(@Nullable String schemaPattern, @Nullable String tableNamePattern) {
            var tableNameClause = new Clause(COLUMN_RELATION_NAME, tableNamePattern);
            String sql = GET_PSEUDO_COLUMNS_FRAGMENT_3
                    + tableNameClause.getCondition("\nwhere ", "")
                    + GET_PSEUDO_COLUMNS_END_3;
            return new MetadataQuery(sql, Clause.parameters(tableNameClause));
        }

    }

    private static final class FB6 extends GetPseudoColumns {

        private static final String GET_PSEUDO_COLUMNS_FRAGMENT_6 = """
                select
                  trim(trailing from RDB$SCHEMA_NAME) as RDB$SCHEMA_NAME,
                  trim(trailing from RDB$RELATION_NAME) as RDB$RELATION_NAME,
                  RDB$DBKEY_LENGTH,
                  RDB$DBKEY_LENGTH = 8 as HAS_RECORD_VERSION,
                  case
                    -- table, view, GTT preserve + delete: never null
                    when RDB$RELATION_TYPE in (0, 1, 4, 5) then 'NO'
                    -- external + virtual: always null
                    when RDB$RELATION_TYPE in (2, 3) then 'YES'
                    -- unknown or unsupported (by Jaybird) type: unknown nullability
                    else ''
                  end as RECORD_VERSION_NULLABLE
                from SYSTEM.RDB$RELATIONS""";

        private static final String GET_PSEUDO_COLUMNS_END_6 = "\norder by RDB$SCHEMA_NAME, RDB$RELATION_NAME";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetPseudoColumns createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        boolean supportsRecordVersion() {
            return true;
        }

        @Override
        MetadataQuery createGetPseudoColumnsQuery(@Nullable String schemaPattern, @Nullable String tableNamePattern) {
            var clauses = List.of(
                    new Clause(COLUMN_SCHEMA_NAME, schemaPattern),
                    new Clause(COLUMN_RELATION_NAME, tableNamePattern));
            String sql = GET_PSEUDO_COLUMNS_FRAGMENT_6
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_PSEUDO_COLUMNS_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
