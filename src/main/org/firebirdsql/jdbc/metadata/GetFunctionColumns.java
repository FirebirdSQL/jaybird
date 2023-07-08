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
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
public abstract class GetFunctionColumns extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(17)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FUNCTION_CAT", "FUNCTION_COLUMNS").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FUNCTION_SCHEM", "FUNCTION_COLUMNS").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FUNCTION_NAME", "FUNCTION_COLUMNS").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "FUNCTION_COLUMNS").addField()
            .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", "FUNCTION_COLUMNS").addField()
            .at(5).simple(SQL_LONG, 0, "DATA_TYPE", "FUNCTION_COLUMNS").addField()
            .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TYPE_NAME", "FUNCTION_COLUMNS").addField()
            .at(7).simple(SQL_LONG | 1, 0, "PRECISION", "FUNCTION_COLUMNS").addField()
            .at(8).simple(SQL_LONG, 0, "LENGTH", "FUNCTION_COLUMNS").addField()
            .at(9).simple(SQL_SHORT | 1, 0, "SCALE", "FUNCTION_COLUMNS").addField()
            .at(10).simple(SQL_SHORT, 0, "RADIX", "FUNCTION_COLUMNS").addField()
            .at(11).simple(SQL_SHORT, 0, "NULLABLE", "FUNCTION_COLUMNS").addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "REMARKS", "FUNCTION_COLUMNS").addField()
            .at(13).simple(SQL_LONG | 1, 0, "CHAR_OCTET_LENGTH", "FUNCTION_COLUMNS").addField()
            .at(14).simple(SQL_LONG, 0, "ORDINAL_POSITION", "FUNCTION_COLUMNS").addField()
            .at(15).simple(SQL_VARYING, 3, "IS_NULLABLE", "FUNCTION_COLUMNS").addField()
            // space for quoted package name, ".", quoted function name (assuming no double quotes in name)
            .at(16).simple(SQL_VARYING, 2 * OBJECT_NAME_LENGTH + 5, "SPECIFIC_NAME", "FUNCTION_COLUMNS").addField()
            .toRowDescriptor();

    private GetFunctionColumns(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)
     */
    public final ResultSet getFunctionColumns(String catalog, String functionNamePattern, String columnNamePattern)
            throws SQLException {
        if ("".equals(functionNamePattern) || "".equals(columnNamePattern)) {
            // Matching function name or column name not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetFunctionColumnsQuery(catalog, functionNamePattern, columnNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                .fromCurrentRow(rs)
                .build();
        String catalog = rs.getString("FUNCTION_CAT");
        String functionName = rs.getString("FUNCTION_NAME");
        int ordinalPosition = rs.getInt("ORDINAL_POSITION");
        boolean nullable = rs.getBoolean("IS_NULLABLE");
        return valueBuilder
                .at(0).setString(catalog)
                .at(1).set(null)
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

    abstract MetadataQuery createGetFunctionColumnsQuery(String catalog, String functionNamePattern,
            String columnNamePattern);

    public static GetFunctionColumns create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(3)) {
            if (mediator.isUseCatalogAsPackage()) {
                return FB3CatalogAsPackage.createInstance(mediator);
            }
            return FB3.createInstance(mediator);
        } else {
            return FB2_5.createInstance(mediator);
        }
    }

    private static final class FB2_5 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_2_5 =
                "select\n"
                + "  null as FUNCTION_CAT,\n"
                + "  FUN.RDB$FUNCTION_NAME as FUNCTION_NAME,\n"
                // Firebird 2.5 and earlier have no parameter name: derive one
                + "  'PARAM_' || FUNA.RDB$ARGUMENT_POSITION as COLUMN_NAME,\n"
                + "  FUNA.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n"
                + "  FUNA.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n"
                + "  FUNA.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n"
                + "  FUNA.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n"
                + "  FUNA.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n"
                + "  FUNA.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n"
                + "  FUNA.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n"
                + "  case\n"
                + "    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0\n"
                + "    else FUNA.RDB$ARGUMENT_POSITION\n"
                + "  end as ORDINAL_POSITION,\n"
                + "  case FUNA.RDB$MECHANISM\n"
                + "    when 0 then 'F'\n"
                + "    when 1 then 'F'\n"
                + "    else 'T'\n"
                + "  end as IS_NULLABLE\n"
                + "from RDB$FUNCTIONS FUN\n"
                + "inner join RDB$FUNCTION_ARGUMENTS FUNA\n"
                + "  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME";
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
        MetadataQuery createGetFunctionColumnsQuery(String catalog, String functionNamePattern,
                String columnNamePattern) {
            Clause functionNameClause = new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern);
            Clause columnNameClause = new Clause("'PARAM_' || FUNA.RDB$ARGUMENT_POSITION", columnNamePattern);
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_2_5
                    + (anyCondition(functionNameClause, columnNameClause)
                    ? "\nwhere " + functionNameClause.getCondition(columnNameClause.hasCondition())
                    + columnNameClause.getCondition(false)
                    : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_2_5;
            return new MetadataQuery(query, Clause.parameters(functionNameClause, columnNameClause));
        }
    }

    private static final class FB3 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_3 =
                "select\n"
                + "  null as FUNCTION_CAT,\n"
                + "  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,\n"
                + "  -- legacy UDF and return value have no parameter name: derive one\n"
                + "  coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION) as COLUMN_NAME,\n"
                + "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n"
                + "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n"
                + "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n"
                + "  case\n"
                + "    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0\n"
                + "    else FUNA.RDB$ARGUMENT_POSITION\n"
                + "  end as ORDINAL_POSITION,\n"
                + "  case \n"
                + "    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false\n"
                + "    when FUNA.RDB$MECHANISM = 0 then false\n"
                + "    when FUNA.RDB$MECHANISM = 1 then false\n"
                + "    else true\n"
                + "  end as IS_NULLABLE\n"
                + "from RDB$FUNCTIONS FUN\n"
                + "inner join RDB$FUNCTION_ARGUMENTS FUNA\n"
                + "  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME \n"
                + "    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME\n"
                + "left join RDB$FIELDS F\n"
                + "  on F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE\n"
                + "where FUN.RDB$PACKAGE_NAME is null";
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
        MetadataQuery createGetFunctionColumnsQuery(String catalog, String functionNamePattern,
                String columnNamePattern) {
            Clause functionNameClause = new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern);
            Clause columnNameClause = new Clause(
                    "coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)", columnNamePattern);
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_3
                    + functionNameClause.getCondition("\nand ", "")
                    + columnNameClause.getCondition("\nand ", "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_3;
            return new MetadataQuery(query, Clause.parameters(functionNameClause, columnNameClause));
        }
    }

    private static final class FB3CatalogAsPackage extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_3_W_PKG =
                "select\n"
                + "  coalesce(trim(trailing from FUN.RDB$PACKAGE_NAME), '') as FUNCTION_CAT,\n"
                + "  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,\n"
                + "  -- legacy UDF and return value have no parameter name: derive one\n"
                + "  coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION) as COLUMN_NAME,\n"
                + "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as " + FIELD_TYPE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as " + FIELD_SUB_TYPE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) as " + FIELD_PRECISION + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as " + FIELD_SCALE + ",\n"
                + "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as " + FIELD_LENGTH + ",\n"
                + "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as " + CHAR_LEN + ",\n"
                + "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as " + CHARSET_ID + ",\n"
                + "  case\n"
                + "    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then 0\n"
                + "    else FUNA.RDB$ARGUMENT_POSITION\n"
                + "  end as ORDINAL_POSITION,\n"
                + "  case \n"
                + "    when coalesce(FUNA.RDB$NULL_FLAG, F.RDB$NULL_FLAG) = 1 then false\n"
                + "    when FUNA.RDB$MECHANISM = 0 then false\n"
                + "    when FUNA.RDB$MECHANISM = 1 then false\n"
                + "    else true\n"
                + "  end as IS_NULLABLE\n"
                + "from RDB$FUNCTIONS FUN\n"
                + "inner join RDB$FUNCTION_ARGUMENTS FUNA\n"
                + "  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME \n"
                + "    and FUNA.RDB$PACKAGE_NAME is not distinct from FUN.RDB$PACKAGE_NAME\n"
                + "left join RDB$FIELDS F\n"
                + "  on F.RDB$FIELD_NAME = FUNA.RDB$FIELD_SOURCE";
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
        MetadataQuery createGetFunctionColumnsQuery(String catalog, String functionNamePattern,
                String columnNamePattern) {
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
                    + (Clause.anyCondition(clauses)
                    ? "\nwhere " + Clause.conjunction(clauses)
                    : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_3_W_PKG;
            //@formatter:on
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }
    }
}
