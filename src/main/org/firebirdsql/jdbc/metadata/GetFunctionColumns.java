// SPDX-FileCopyrightText: Copyright 2001-2026 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2019-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DatabaseMetaData.functionColumnIn;
import static java.sql.DatabaseMetaData.functionNoNulls;
import static java.sql.DatabaseMetaData.functionNullable;
import static java.sql.DatabaseMetaData.functionReturn;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.Clause.anyCondition;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.NameHelper.toSpecificName;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHARSET_ID;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.CHAR_LEN;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_LENGTH;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_PRECISION;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SCALE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_SUB_TYPE;
import static org.firebirdsql.jdbc.metadata.TypeMetadata.FIELD_TYPE;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
@SuppressWarnings({ "java:S1192", "java:S5665" })
public abstract sealed class GetFunctionColumns extends AbstractMetadataMethod {

    private static final String FUNCTION_COLUMNS = "FUNCTION_COLUMNS";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(17)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FUNCTION_CAT", FUNCTION_COLUMNS).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FUNCTION_SCHEM", FUNCTION_COLUMNS).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_NAME", FUNCTION_COLUMNS).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", FUNCTION_COLUMNS).addField()
            .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", FUNCTION_COLUMNS).addField()
            .at(5).simple(SQL_LONG, 0, "DATA_TYPE", FUNCTION_COLUMNS).addField()
            .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_NAME", FUNCTION_COLUMNS).addField()
            .at(7).simple(SQL_LONG | 1, 0, "PRECISION", FUNCTION_COLUMNS).addField()
            .at(8).simple(SQL_LONG, 0, "LENGTH", FUNCTION_COLUMNS).addField()
            .at(9).simple(SQL_SHORT | 1, 0, "SCALE", FUNCTION_COLUMNS).addField()
            .at(10).simple(SQL_SHORT, 0, "RADIX", FUNCTION_COLUMNS).addField()
            .at(11).simple(SQL_SHORT, 0, "NULLABLE", FUNCTION_COLUMNS).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", FUNCTION_COLUMNS).addField()
            .at(13).simple(SQL_LONG | 1, 0, "CHAR_OCTET_LENGTH", FUNCTION_COLUMNS).addField()
            .at(14).simple(SQL_LONG, 0, "ORDINAL_POSITION", FUNCTION_COLUMNS).addField()
            .at(15).simple(SQL_VARYING, 3, "IS_NULLABLE", FUNCTION_COLUMNS).addField()
            // space for quoted package name, ".", quoted function name (assuming no double quotes in name)
            .at(16).simple(SQL_VARYING, 2 * OBJECT_NAME_LENGTH + 5, "SPECIFIC_NAME", FUNCTION_COLUMNS).addField()
            .toRowDescriptor();

    private GetFunctionColumns(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)
     */
    public final ResultSet getFunctionColumns(@Nullable String catalog, @Nullable String schemaPattern,
            @Nullable String functionNamePattern, @Nullable String columnNamePattern) throws SQLException {
        if ("".equals(functionNamePattern) || "".equals(columnNamePattern)) {
            // Matching function name or column name not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetFunctionColumnsQuery(catalog, schemaPattern, functionNamePattern,
                columnNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                .fromCurrentRow(rs)
                .build();
        String catalog = rs.getString("FUNCTION_CAT");
        String schema = rs.getString("FUNCTION_SCHEM");
        String functionName = rs.getString("FUNCTION_NAME");
        int ordinalPosition = rs.getInt("ORDINAL_POSITION");
        boolean nullable = rs.getBoolean("IS_NULLABLE");
        return valueBuilder
                .at(0).setString(catalog)
                .at(1).setString(schema)
                .at(2).setString(functionName)
                .at(3).setString(rs.getString("COLUMN_NAME"))
                .at(4).setShort(ordinalPosition == 0 ? functionReturn : functionColumnIn)
                .at(5).setInt(typeMetadata.getJdbcType())
                .at(6).setString(typeMetadata.getSqlTypeName())
                .at(7).setInt(typeMetadata.getColumnSize())
                .at(8).setInt(typeMetadata.getLength())
                .at(9).setShort(typeMetadata.getScale())
                .at(10).setShort(typeMetadata.getRadix())
                .at(11).setShort(nullable ? functionNullable : functionNoNulls)
                // No remarks on parameters possible
                .at(12).set(null)
                .at(13).setInt(typeMetadata.getCharOctetLength())
                .at(14).setInt(ordinalPosition)
                .at(15).setString(nullable ? "YES" : "NO")
                .at(16).setString(toSpecificName(catalog, functionName))
                .toRowValue(false);
    }

    abstract MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
            @Nullable String functionNamePattern, @Nullable String columnNamePattern);

    public static GetFunctionColumns create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(6)) {
            if (mediator.isUseCatalogAsPackage()) {
                return FB6CatalogAsPackage.createInstance(mediator);
            }
            return FB6.createInstance(mediator);
        } else if (firebirdSupportInfo.isVersionEqualOrAbove(3)) {
            if (mediator.isUseCatalogAsPackage()) {
                return FB3CatalogAsPackage.createInstance(mediator);
            }
            return FB3.createInstance(mediator);
        } else {
            return FB2_5.createInstance(mediator);
        }
    }

    @SuppressWarnings("java:S101")
    private static final class FB2_5 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_2_5 = """
                select
                  cast(null as char(1)) as FUNCTION_CAT,
                  cast(null as char(1)) as FUNCTION_SCHEM,
                  FUN.RDB$FUNCTION_NAME as FUNCTION_NAME,
                  -- Firebird 2.5 and earlier have no parameter name: derive one
                  'PARAM_' || FUNA.RDB$ARGUMENT_POSITION as COLUMN_NAME,
                """ +
                "  FUNA.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  FUNA.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  FUNA.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  FUNA.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  FUNA.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  FUNA.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  FUNA.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0
                    else FUNA.RDB$ARGUMENT_POSITION
                  end as ORDINAL_POSITION,
                  case FUNA.RDB$MECHANISM
                    when 0 then 'F'
                    when 1 then 'F'
                    else 'T'
                  end as IS_NULLABLE
                from RDB$FUNCTIONS FUN
                inner join RDB$FUNCTION_ARGUMENTS FUNA
                  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME""";
        //@formatter:on

        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_2_5 = """
                \norder by FUN.RDB$FUNCTION_NAME,
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1
                    else FUNA.RDB$ARGUMENT_POSITION
                  end""";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetFunctionColumns createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
                @Nullable String functionNamePattern, @Nullable String columnNamePattern) {
            var clauses = List.of(
                    new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern),
                    new Clause("'PARAM_' || FUNA.RDB$ARGUMENT_POSITION", columnNamePattern));
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_2_5
                    + (anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_2_5;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static final class FB3 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_3 = """
                select
                  null as FUNCTION_CAT,
                  null as FUNCTION_SCHEM,
                  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,
                  -- legacy UDF and return value have no parameter name: derive one
                  trim(trailing from coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)) as COLUMN_NAME,
                """ +
                "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n" + """
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0
                    else FUNA.RDB$ARGUMENT_POSITION
                  end as ORDINAL_POSITION,
                  case
                    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false
                    when FUNA.RDB$MECHANISM = 0 then false
                    when FUNA.RDB$MECHANISM = 1 then false
                    else true
                  end as IS_NULLABLE
                from RDB$FUNCTIONS FUN
                inner join RDB$FUNCTION_ARGUMENTS FUNA
                  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME
                    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME
                left join RDB$FIELDS F
                  on F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE
                where FUN.RDB$PACKAGE_NAME is null""";
        //@formatter:on

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_3 = """
                \norder by FUN.RDB$PACKAGE_NAME, FUN.RDB$FUNCTION_NAME,
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1
                    else FUNA.RDB$ARGUMENT_POSITION
                  end""";


        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetFunctionColumns createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
                @Nullable String functionNamePattern, @Nullable String columnNamePattern) {
            var clauses = List.of(
                    new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern),
                    new Clause("coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)",
                            columnNamePattern));
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_3
                    + (Clause.anyCondition(clauses) ? "\nand " + Clause.conjunction(clauses) : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_3;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static final class FB3CatalogAsPackage extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_3_W_PKG = """
                select
                  coalesce(trim(trailing from FUN.RDB$PACKAGE_NAME), '') as FUNCTION_CAT,
                  null as FUNCTION_SCHEM,
                  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,
                  -- legacy UDF and return value have no parameter name: derive one
                  trim(trailing from coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)) as COLUMN_NAME,
                """ +
                "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n" + """
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0
                    else FUNA.RDB$ARGUMENT_POSITION
                  end as ORDINAL_POSITION,
                  case
                    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false
                    when FUNA.RDB$MECHANISM = 0 then false
                    when FUNA.RDB$MECHANISM = 1 then false
                    else true
                  end as IS_NULLABLE
                from RDB$FUNCTIONS FUN
                inner join RDB$FUNCTION_ARGUMENTS FUNA
                  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME
                    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME
                left join RDB$FIELDS F
                  on F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE""";
        //@formatter:on

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_3_W_PKG = """
                \norder by FUN.RDB$PACKAGE_NAME nulls first, FUN.RDB$FUNCTION_NAME,
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1
                    else FUNA.RDB$ARGUMENT_POSITION
                  end""";

        private FB3CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetFunctionColumns createInstance(DbMetadataMediator mediator) {
            return new FB3CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
                @Nullable String functionNamePattern, @Nullable String columnNamePattern) {
            var clauses = new ArrayList<Clause>(3);
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause("FUN.RDB$PACKAGE_NAME"));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause("FUN.RDB$PACKAGE_NAME", catalog));
                }
            }
            clauses.add(new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern));
            clauses.add(new Clause(
                    "coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)", columnNamePattern));
            //@formatter:off
            String sql = GET_FUNCTION_COLUMNS_FRAGMENT_3_W_PKG
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_3_W_PKG;
            //@formatter:on
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

    private static final class FB6 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_6 = """
                select
                  null as FUNCTION_CAT,
                  trim(trailing from FUN.RDB$SCHEMA_NAME) as FUNCTION_SCHEM,
                  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,
                  -- legacy UDF and return value have no parameter name: derive one
                  trim(trailing from coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)) as COLUMN_NAME,
                """ +
                "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n" + """
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0
                    else FUNA.RDB$ARGUMENT_POSITION
                  end as ORDINAL_POSITION,
                  case
                    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false
                    when FUNA.RDB$MECHANISM = 0 then false
                    when FUNA.RDB$MECHANISM = 1 then false
                    else true
                  end as IS_NULLABLE
                from SYSTEM.RDB$FUNCTIONS FUN
                inner join SYSTEM.RDB$FUNCTION_ARGUMENTS FUNA
                  on FUNA.RDB$SCHEMA_NAME = FUN.RDB$SCHEMA_NAME and FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME
                    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME
                left join SYSTEM.RDB$FIELDS F
                  on F.RDB$SCHEMA_NAME = FUNA.RDB$FIELD_SOURCE_SCHEMA_NAME and F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE
                where FUN.RDB$PACKAGE_NAME is null""";
        //@formatter:on

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_6 = """
                \norder by FUN.RDB$SCHEMA_NAME, FUN.RDB$PACKAGE_NAME, FUN.RDB$FUNCTION_NAME,
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1
                    else FUNA.RDB$ARGUMENT_POSITION
                  end""";


        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetFunctionColumns createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
                @Nullable String functionNamePattern, @Nullable String columnNamePattern) {
            var clauses = List.of(
                    new Clause("FUN.RDB$SCHEMA_NAME", schemaPattern),
                    new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern),
                    new Clause("coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)",
                            columnNamePattern));
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_6
                    + (Clause.anyCondition(clauses) ? "\nand " + Clause.conjunction(clauses) : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_6;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static final class FB6CatalogAsPackage extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_6_W_PKG = """
                select
                  coalesce(trim(trailing from FUN.RDB$PACKAGE_NAME), '') as FUNCTION_CAT,
                  trim(trailing from FUN.RDB$SCHEMA_NAME) as FUNCTION_SCHEM,
                  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,
                  -- legacy UDF and return value have no parameter name: derive one
                  trim(trailing from coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)) as COLUMN_NAME,
                """ +
                "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n" +
                "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n" +
                "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n" + """
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0
                    else FUNA.RDB$ARGUMENT_POSITION
                  end as ORDINAL_POSITION,
                  case
                    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false
                    when FUNA.RDB$MECHANISM = 0 then false
                    when FUNA.RDB$MECHANISM = 1 then false
                    else true
                  end as IS_NULLABLE
                from SYSTEM.RDB$FUNCTIONS FUN
                inner join SYSTEM.RDB$FUNCTION_ARGUMENTS FUNA
                  on FUNA.RDB$SCHEMA_NAME = FUN.RDB$SCHEMA_NAME and FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME
                    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME
                left join SYSTEM.RDB$FIELDS F
                  on F.RDB$SCHEMA_NAME = FUNA.RDB$FIELD_SOURCE_SCHEMA_NAME and F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE""";
        //@formatter:on

        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_6_W_PKG = """
                \norder by FUN.RDB$PACKAGE_NAME nulls first, FUN.RDB$SCHEMA_NAME, FUN.RDB$FUNCTION_NAME,
                  case
                    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1
                    else FUNA.RDB$ARGUMENT_POSITION
                  end""";

        private FB6CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetFunctionColumns createInstance(DbMetadataMediator mediator) {
            return new FB6CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(@Nullable String catalog, @Nullable String schemaPattern,
                @Nullable String functionNamePattern, @Nullable String columnNamePattern) {
            var clauses = new ArrayList<Clause>(4);
            clauses.add(new Clause("FUN.RDB$SCHEMA_NAME", schemaPattern));
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause("FUN.RDB$PACKAGE_NAME"));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause("FUN.RDB$PACKAGE_NAME", catalog));
                }
            }
            clauses.add(new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern));
            clauses.add(new Clause(
                    "coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)", columnNamePattern));
            //@formatter:off
            String sql = GET_FUNCTION_COLUMNS_FRAGMENT_6_W_PKG
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_6_W_PKG;
            //@formatter:on
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
