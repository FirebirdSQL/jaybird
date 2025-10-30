// SPDX-FileCopyrightText: Copyright 2001-2025 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;

import static java.sql.DatabaseMetaData.columnNoNulls;
import static java.sql.DatabaseMetaData.columnNullable;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.int128_type;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHARSET_ID;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHAR_LEN;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_LENGTH;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_PRECISION;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SCALE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SUB_TYPE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_TYPE;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetColumns extends AbstractMetadataMethod {

    private static final String COLUMNINFO = "COLUMNINFO";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(26)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", COLUMNINFO).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", COLUMNINFO).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", COLUMNINFO).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", COLUMNINFO).addField()
            .at(4).simple(SQL_LONG, 0, "DATA_TYPE", COLUMNINFO).addField()
            .at(5).simple(SQL_VARYING | 1, 31, "TYPE_NAME", COLUMNINFO).addField()
            .at(6).simple(SQL_LONG, 0, "COLUMN_SIZE", COLUMNINFO).addField()
            .at(7).simple(SQL_LONG, 0, "BUFFER_LENGTH", COLUMNINFO).addField()
            .at(8).simple(SQL_LONG, 0, "DECIMAL_DIGITS", COLUMNINFO).addField()
            .at(9).simple(SQL_LONG, 0, "NUM_PREC_RADIX", COLUMNINFO).addField()
            .at(10).simple(SQL_LONG, 0, "NULLABLE", COLUMNINFO).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(11).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", COLUMNINFO).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "COLUMN_DEF", COLUMNINFO).addField()
            .at(13).simple(SQL_LONG, 0, "SQL_DATA_TYPE", COLUMNINFO).addField()
            .at(14).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", COLUMNINFO).addField()
            .at(15).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", COLUMNINFO).addField()
            .at(16).simple(SQL_LONG, 0, "ORDINAL_POSITION", COLUMNINFO).addField()
            .at(17).simple(SQL_VARYING, 3, "IS_NULLABLE", COLUMNINFO).addField()
            .at(18).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_CATALOG", COLUMNINFO).addField()
            .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_SCHEMA", COLUMNINFO).addField()
            .at(20).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SCOPE_TABLE", COLUMNINFO).addField()
            .at(21).simple(SQL_SHORT, 0, "SOURCE_DATA_TYPE", COLUMNINFO).addField()
            .at(22).simple(SQL_VARYING, 3, "IS_AUTOINCREMENT", COLUMNINFO).addField()
            .at(23).simple(SQL_VARYING, 3, "IS_GENERATEDCOLUMN", COLUMNINFO).addField()
            .at(24).simple(SQL_VARYING, 3, "JB_IS_IDENTITY", COLUMNINFO).addField()
            .at(25).simple(SQL_VARYING, 10, "JB_IDENTITY_TYPE", COLUMNINFO).addField()
            .toRowDescriptor();

    private GetColumns(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see java.sql.DatabaseMetaData#getColumns(String, String, String, String)
     * @see org.firebirdsql.jdbc.FBDatabaseMetaData#getColumns(String, String, String, String) 
     */
    public final ResultSet getColumns(String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        if ("".equals(tableNamePattern) || "".equals(columnNamePattern)) {
            // Matching table name or column not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetColumnsQuery(schemaPattern, tableNamePattern, columnNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                .fromCurrentRow(rs)
                .build();

        boolean isNullable = rs.getBoolean("IS_NULLABLE");
        boolean isComputed = rs.getBoolean("IS_COMPUTED");
        boolean isIdentity = rs.getBoolean("IS_IDENTITY");
        return valueBuilder
                .at(1).setString(rs.getString("SCHEMA_NAME"))
                .at(2).setString(rs.getString("RELATION_NAME"))
                .at(3).setString(rs.getString("FIELD_NAME"))
                .at(4).setInt(typeMetadata.getJdbcType())
                .at(5).setString(typeMetadata.getSqlTypeName())
                .at(6).setInt(typeMetadata.getColumnSize())
                .at(8).setInt(typeMetadata.getScale())
                .at(9).setInt(typeMetadata.getRadix())
                .at(10).setInt(isNullable ? columnNullable : columnNoNulls)
                .at(11).setString(rs.getString("REMARKS"))
                .at(12).setString(extractDefault(rs.getString("DEFAULT_SOURCE")))
                .at(15).setInt(typeMetadata.getCharOctetLength())
                .at(16).setInt(rs.getInt("FIELD_POSITION"))
                .at(17).setString(isNullable ? "YES" : "NO")
                .at(22).setString(getIsAutoIncrementValue(isIdentity, typeMetadata))
                .at(23).setString(isComputed || isIdentity ? "YES" : "NO")
                .at(24).setString(isIdentity ? "YES" : "NO")
                .at(25).setString(rs.getString("JB_IDENTITY_TYPE"))
                .toRowValue(true);
    }

    private String getIsAutoIncrementValue(boolean isIdentity, TypeMetadata typeMetadata) {
        if (isIdentity) {
            return "YES";
        }
        return switch (typeMetadata.getJdbcType()) {
            // Could be autoincrement by trigger, but we simply don't know
            case Types.INTEGER, Types.TINYINT, Types.BIGINT, Types.SMALLINT -> "";
            case Types.NUMERIC, Types.DECIMAL -> {
                if (Objects.equals(typeMetadata.getScale(), 0) && typeMetadata.getType() != int128_type) {
                    // Could be autoincrement by trigger, but we simply don't know
                    yield "";
                }
                // Scaled NUMERIC/DECIMAL or INT128-based: definitely not autoincrement
                yield "NO";
            }
            // All other types are never autoincrement
            default -> "NO";
        };
    }

    abstract MetadataQuery createGetColumnsQuery(String schemaPattern, String tableNamePattern,
            String columnNamePattern);

    public static GetColumns create(DbMetadataMediator mediator) {
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
    private static final class FB2_5 extends GetColumns {

        private static final String GET_COLUMNS_FRAGMENT_2_5 = """
                select
                  cast(null as char(1)) AS SCHEMA_NAME,
                  RF.RDB$RELATION_NAME as RELATION_NAME,
                  RF.RDB$FIELD_NAME as FIELD_NAME,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  RF.RDB$DESCRIPTION as REMARKS,
                  coalesce(RF.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as DEFAULT_SOURCE,
                  RF.RDB$FIELD_POSITION + 1 as FIELD_POSITION,
                  iif(coalesce(RF.RDB$NULL_FLAG, 0) + coalesce(F.RDB$NULL_FLAG, 0) = 0, 'T', 'F') as IS_NULLABLE,
                  iif(F.RDB$COMPUTED_BLR is not NULL, 'T', 'F') as IS_COMPUTED,
                  'F' as IS_IDENTITY,
                  cast(NULL as VARCHAR(10)) as JB_IDENTITY_TYPE
                from RDB$RELATION_FIELDS RF inner join RDB$FIELDS F on RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";

        private static final String GET_COLUMNS_ORDER_BY_2_5 = "\norder by RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetColumns createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetColumnsQuery(String schemaPattern, String tableNamePattern, String columnNamePattern) {
            var clauses = List.of(
                    new Clause("RF.RDB$RELATION_NAME", tableNamePattern),
                    new Clause("RF.RDB$FIELD_NAME", columnNamePattern));
            String sql = GET_COLUMNS_FRAGMENT_2_5
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_COLUMNS_ORDER_BY_2_5;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

    private static final class FB3 extends GetColumns {

        private static final String GET_COLUMNS_FRAGMENT_3 = """
                select
                  null AS SCHEMA_NAME,
                  trim(trailing from RF.RDB$RELATION_NAME) as RELATION_NAME,
                  trim(trailing from RF.RDB$FIELD_NAME) as FIELD_NAME,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  RF.RDB$DESCRIPTION as REMARKS,
                  coalesce(RF.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as DEFAULT_SOURCE,
                  RF.RDB$FIELD_POSITION + 1 as FIELD_POSITION,
                  (coalesce(RF.RDB$NULL_FLAG, 0) + coalesce(F.RDB$NULL_FLAG, 0) = 0) as IS_NULLABLE,
                  (F.RDB$COMPUTED_BLR is not NULL) as IS_COMPUTED,
                  (RF.RDB$IDENTITY_TYPE IS NOT NULL) as IS_IDENTITY,
                  trim(trailing from decode(RF.RDB$IDENTITY_TYPE, 0, 'ALWAYS', 1, 'BY DEFAULT')) as JB_IDENTITY_TYPE
                from RDB$RELATION_FIELDS RF inner join RDB$FIELDS F on RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";

        private static final String GET_COLUMNS_ORDER_BY_3 = "\norder by RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION";

        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetColumns createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        MetadataQuery createGetColumnsQuery(String schemaPattern, String tableNamePattern, String columnNamePattern) {
            var clauses = List.of(
                    new Clause("RF.RDB$RELATION_NAME", tableNamePattern),
                    new Clause("RF.RDB$FIELD_NAME", columnNamePattern));
            String sql = GET_COLUMNS_FRAGMENT_3
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_COLUMNS_ORDER_BY_3;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

    private static final class FB6 extends GetColumns {

        private static final String GET_COLUMNS_FRAGMENT_6 = """
                select
                  trim(trailing from RF.RDB$SCHEMA_NAME) AS SCHEMA_NAME,
                  trim(trailing from RF.RDB$RELATION_NAME) as RELATION_NAME,
                  trim(trailing from RF.RDB$FIELD_NAME) as FIELD_NAME,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  RF.RDB$DESCRIPTION as REMARKS,
                  coalesce(RF.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as DEFAULT_SOURCE,
                  RF.RDB$FIELD_POSITION + 1 as FIELD_POSITION,
                  (coalesce(RF.RDB$NULL_FLAG, 0) + coalesce(F.RDB$NULL_FLAG, 0) = 0) as IS_NULLABLE,
                  (F.RDB$COMPUTED_BLR is not NULL) as IS_COMPUTED,
                  (RF.RDB$IDENTITY_TYPE IS NOT NULL) as IS_IDENTITY,
                  trim(trailing from decode(RF.RDB$IDENTITY_TYPE, 0, 'ALWAYS', 1, 'BY DEFAULT')) as JB_IDENTITY_TYPE
                from SYSTEM.RDB$RELATION_FIELDS RF
                inner join SYSTEM.RDB$FIELDS F
                  on RF.RDB$FIELD_SOURCE_SCHEMA_NAME = F.RDB$SCHEMA_NAME and RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";

        private static final String GET_COLUMNS_ORDER_BY_6 =
                "\norder by RF.RDB$SCHEMA_NAME, RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetColumns createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetColumnsQuery(String schemaPattern, String tableNamePattern, String columnNamePattern) {
            var clauses = List.of(
                    new Clause("RF.RDB$SCHEMA_NAME", schemaPattern),
                    new Clause("RF.RDB$RELATION_NAME", tableNamePattern),
                    new Clause("RF.RDB$FIELD_NAME", columnNamePattern));
            String sql = GET_COLUMNS_FRAGMENT_6
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_COLUMNS_ORDER_BY_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
