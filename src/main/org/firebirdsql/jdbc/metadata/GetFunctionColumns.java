/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
import org.firebirdsql.gds.ng.fields.RowValueBuilder;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.FBDatabaseMetaData.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.Clause.anyCondition;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@InternalApi
public abstract class GetFunctionColumns {

    private static final RowDescriptor FUNCTION_COLUMNS_ROW_DESCRIPTOR =
            new RowDescriptorBuilder(17, DbMetadataMediator.datatypeCoder)
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
                    .at(16).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "FUNCTION_COLUMNS").addField()
                    .toRowDescriptor();

    private final DbMetadataMediator mediator;

    private GetFunctionColumns(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @see java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)
     */
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
            String columnNamePattern) throws SQLException {
        if ("".equals(functionNamePattern) || "".equals(columnNamePattern)) {
            // Matching function name or column name not possible
            return new FBResultSet(FUNCTION_COLUMNS_ROW_DESCRIPTOR, Collections.<RowValue>emptyList());
        }

        MetadataQuery metadataQuery = createGetFunctionColumnsQuery(functionNamePattern, columnNamePattern);
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            if (!rs.next()) {
                return new FBResultSet(FUNCTION_COLUMNS_ROW_DESCRIPTOR, Collections.<RowValue>emptyList());
            }

            final byte[] functionColumnIn = mediator.createShort(DatabaseMetaData.functionColumnIn);
            final byte[] functionColumnReturn = mediator.createShort(DatabaseMetaData.functionReturn);
            final byte[] functionNoNulls = mediator.createShort(DatabaseMetaData.functionNoNulls);
            final byte[] functionNullable = mediator.createShort(DatabaseMetaData.functionNullable);
            final byte[] nullableYes = mediator.createString("YES");
            final byte[] nullableNo = mediator.createString("NO");

            final List<RowValue> rows = new ArrayList<>();
            final RowValueBuilder valueBuilder = new RowValueBuilder(FUNCTION_COLUMNS_ROW_DESCRIPTOR);
            do {
                byte[] functionNameBytes = mediator.createString(rs.getString("FUNCTION_NAME"));
                TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                        .withType(rs.getObject("FIELD_TYPE", Integer.class))
                        .withSubType(rs.getObject("FIELD_SUB_TYPE", Integer.class))
                        .withPrecision(rs.getObject("FIELD_PRECISION", Integer.class))
                        .withScale(rs.getObject("FIELD_SCALE", Integer.class))
                        .withFieldLength(rs.getObject("FIELD_LENGTH", Integer.class))
                        .withCharacterLength(rs.getObject("CHARACTER_LENGTH", Integer.class))
                        .withCharacterSetId(rs.getObject("CHARACTER_SET_ID", Integer.class))
                        .build();
                int ordinalPosition = rs.getInt("ORDINAL_POSITION");
                boolean nullable = rs.getBoolean("IS_NULLABLE");
                valueBuilder
                        .at(0).set(null)
                        .at(1).set(null)
                        .at(2).set(functionNameBytes)
                        .at(3).set(mediator.createString(rs.getString("COLUMN_NAME")))
                        .at(4).set(ordinalPosition == 0 ? functionColumnReturn : functionColumnIn)
                        .at(5).set(mediator.createInt(typeMetadata.getJdbcType()))
                        .at(6).set(mediator.createString(typeMetadata.getSqlTypeName()))
                        .at(7).set(mediator.createInt(typeMetadata.getColumnSize()))
                        .at(8).set(mediator.createInt(typeMetadata.getLength()))
                        .at(9).set(mediator.createShort(typeMetadata.getScale()))
                        .at(10).set(mediator.createShort(typeMetadata.getRadix()))
                        .at(11).set(nullable ? functionNullable : functionNoNulls)
                        // No remarks on parameters possible
                        .at(12).set(null)
                        .at(13).set(mediator.createInt(typeMetadata.getCharOctetLength()))
                        .at(14).set(mediator.createInt(ordinalPosition))
                        .at(15).set(nullable ? nullableYes : nullableNo)
                        .at(16).set(functionNameBytes);
                rows.add(valueBuilder.toRowValue(false));
            } while (rs.next());
            return new FBResultSet(FUNCTION_COLUMNS_ROW_DESCRIPTOR, rows);
        }
    }

    abstract MetadataQuery createGetFunctionColumnsQuery(String functionNamePattern, String columnNamePattern);

    public static GetFunctionColumns create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        if (firebirdSupportInfo.isVersionEqualOrAbove(3, 0)) {
            return new GetFunctionColumnsFirebird3_0(mediator);
        } else {
            return new GetFunctionColumnsFirebird2_5(mediator);
        }
    }

    private static class GetFunctionColumnsFirebird2_5 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_2_5 =
                "select\n"
                + "  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,\n"
                + "  -- Firebird 2.5 and earlier have no parameter name: derive one\n"
                + "  'PARAM_' || FUNA.RDB$ARGUMENT_POSITION as COLUMN_NAME,\n"
                + "  FUNA.RDB$FIELD_TYPE as FIELD_TYPE,\n"
                + "  FUNA.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE,\n"
                + "  FUNA.RDB$FIELD_PRECISION AS FIELD_PRECISION,\n"
                + "  FUNA.RDB$FIELD_SCALE as FIELD_SCALE,\n"
                + "  FUNA.RDB$FIELD_LENGTH as FIELD_LENGTH,\n"
                + "  FUNA.RDB$CHARACTER_LENGTH as \"CHARACTER_LENGTH\",\n"
                + "  FUNA.RDB$CHARACTER_SET_ID as CHARACTER_SET_ID,\n"
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
                + "  on FUNA.RDB$FUNCTION_NAME = FUN.RDB$FUNCTION_NAME\n";

        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_2_5 =
                "order by FUN.RDB$FUNCTION_NAME,\n"
                + "  case\n"
                + "    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1\n"
                + "    else FUNA.RDB$ARGUMENT_POSITION\n"
                + "  end";
        //@formatter:on

        private GetFunctionColumnsFirebird2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(String functionNamePattern, String columnNamePattern) {
            Clause functionNameClause = new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern);
            Clause columnNameClause = new Clause("'PARAM_' || FUNA.RDB$ARGUMENT_POSITION", columnNamePattern);
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_2_5
                    + (anyCondition(functionNameClause, columnNameClause)
                    ? "where " + functionNameClause.getCondition("", columnNameClause.hasCondition() ? "\nand " : "\n")
                    + columnNameClause.getCondition("", "\n")
                    : "")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_2_5;
            return new MetadataQuery(query, Clause.parameters(functionNameClause, columnNameClause));
        }
    }

    private static class GetFunctionColumnsFirebird3_0 extends GetFunctionColumns {

        //@formatter:off
        private static final String GET_FUNCTION_COLUMNS_FRAGMENT_3_0 =
                "select\n"
                + "  trim(trailing from FUN.RDB$FUNCTION_NAME) as FUNCTION_NAME,\n"
                + "  -- legacy UDF and return value have no parameter name: derive one\n"
                + "  coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION) as COLUMN_NAME,\n"
                + "  coalesce(FUNA.RDB$FIELD_TYPE, F.RDB$FIELD_TYPE) as FIELD_TYPE,\n"
                + "  coalesce(FUNA.RDB$FIELD_SUB_TYPE, F.RDB$FIELD_SUB_TYPE) as FIELD_SUB_TYPE,\n"
                + "  coalesce(FUNA.RDB$FIELD_PRECISION, F.RDB$FIELD_PRECISION) AS FIELD_PRECISION,\n"
                + "  coalesce(FUNA.RDB$FIELD_SCALE, F.RDB$FIELD_SCALE) as FIELD_SCALE,\n"
                + "  coalesce(FUNA.RDB$FIELD_LENGTH, F.RDB$FIELD_LENGTH) as FIELD_LENGTH,\n"
                + "  coalesce(FUNA.RDB$CHARACTER_LENGTH, F.RDB$CHARACTER_LENGTH) as \"CHARACTER_LENGTH\",\n"
                + "  coalesce(FUNA.RDB$CHARACTER_SET_ID, F.RDB$CHARACTER_SET_ID) as CHARACTER_SET_ID,\n"
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
                + "where FUN.RDB$PACKAGE_NAME is null\n";

        private static final String GET_FUNCTION_COLUMNS_ORDER_BY_3_0 =
                "order by FUN.RDB$PACKAGE_NAME, FUN.RDB$FUNCTION_NAME,\n"
                + "  case\n"
                + "    when FUN.RDB$RETURN_ARGUMENT = FUNA.RDB$ARGUMENT_POSITION then -1\n"
                + "    else FUNA.RDB$ARGUMENT_POSITION\n"
                + "  end";
        //@formatter:on

        private GetFunctionColumnsFirebird3_0(DbMetadataMediator mediator) {
            super(mediator);
        }

        @Override
        MetadataQuery createGetFunctionColumnsQuery(String functionNamePattern, String columnNamePattern) {
            Clause functionNameClause = new Clause("FUN.RDB$FUNCTION_NAME", functionNamePattern);
            Clause columnNameClause = new Clause(
                    "coalesce(FUNA.RDB$ARGUMENT_NAME, 'PARAM_' || FUNA.RDB$ARGUMENT_POSITION)", columnNamePattern);
            String query = GET_FUNCTION_COLUMNS_FRAGMENT_3_0
                    + functionNameClause.getCondition("and ", "\n")
                    + columnNameClause.getCondition("and ", "\n")
                    + GET_FUNCTION_COLUMNS_ORDER_BY_3_0;
            return new MetadataQuery(query, Clause.parameters(functionNameClause, columnNameClause));
        }
    }
}
