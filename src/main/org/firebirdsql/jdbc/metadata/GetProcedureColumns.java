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
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.DatabaseMetaData.procedureColumnIn;
import static java.sql.DatabaseMetaData.procedureColumnOut;
import static java.sql.DatabaseMetaData.procedureNoNulls;
import static java.sql.DatabaseMetaData.procedureNullable;
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
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getProcedureColumns(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class GetProcedureColumns extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(20, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", "COLUMNINFO").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", "COLUMNINFO").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", "COLUMNINFO").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", "COLUMNINFO").addField()
            .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", "COLUMNINFO").addField()
            .at(5).simple(SQL_LONG, 0, "DATA_TYPE", "COLUMNINFO").addField()
            .at(6).simple(SQL_VARYING, 31, "TYPE_NAME", "COLUMNINFO").addField()
            .at(7).simple(SQL_LONG, 0, "PRECISION", "COLUMNINFO").addField()
            .at(8).simple(SQL_LONG, 0, "LENGTH", "COLUMNINFO").addField()
            .at(9).simple(SQL_SHORT, 0, "SCALE", "COLUMNINFO").addField()
            .at(10).simple(SQL_SHORT, 0, "RADIX", "COLUMNINFO").addField()
            .at(11).simple(SQL_SHORT, 0, "NULLABLE", "COLUMNINFO").addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", "COLUMNINFO").addField()
            .at(13).simple(SQL_VARYING, 31, "COLUMN_DEF", "COLUMNINFO").addField()
            .at(14).simple(SQL_LONG, 0, "SQL_DATA_TYPE", "COLUMNINFO").addField()
            .at(15).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", "COLUMNINFO").addField()
            .at(16).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", "COLUMNINFO").addField()
            .at(17).simple(SQL_LONG, 0, "ORDINAL_POSITION", "COLUMNINFO").addField()
            .at(18).simple(SQL_VARYING, 3, "IS_NULLABLE", "COLUMNINFO").addField()
            .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", "COLUMNINFO").addField()
            .toRowDescriptor();

    private GetProcedureColumns(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see DatabaseMetaData#getProcedureColumns(String, String, String, String) 
     */
    public final ResultSet getProcedureColumns(String catalog, String procedureNamePattern, String columnNamePattern)
            throws SQLException {
        if ("".equals(procedureNamePattern) || "".equals(columnNamePattern)) {
            // Matching procedure name or column name not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetProcedureColumnsQuery(catalog, procedureNamePattern, columnNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    final RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        final short columnType = rs.getShort("COLUMN_TYPE");
        // TODO: Find out what the difference is with NULL_FLAG in RDB$PROCEDURE_PARAMETERS (might be ODS dependent)
        final short nullFlag = rs.getShort("NULL_FLAG");
        TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                .fromCurrentRow(rs)
                .build();
        String catalog = rs.getString("PROCEDURE_CAT");
        String procedureName = rs.getString("PROCEDURE_NAME");
        return valueBuilder
                .at(0).setString(catalog)
                .at(1).set(null)
                .at(2).setString(procedureName)
                .at(3).setString(rs.getString("COLUMN_NAME"))
                // TODO: Unsure if procedureColumnOut is correct, maybe procedureColumnResult, or need ODS dependent use of RDB$PROCEDURE_TYPE to decide on selectable or executable?
                // TODO: ResultSet columns should not be first according to JDBC 4.3 description
                .at(4).setShort(columnType == 0 ? procedureColumnIn : procedureColumnOut)
                .at(5).setInt(typeMetadata.getJdbcType())
                .at(6).setString(typeMetadata.getSqlTypeName())
                .at(7).setInt(typeMetadata.getColumnSize())
                .at(8).setInt(typeMetadata.getLength())
                .at(9).setShort(typeMetadata.getScale())
                .at(10).setShort(typeMetadata.getRadix())
                .at(11).setShort(nullFlag == 1 ? procedureNoNulls : procedureNullable)
                .at(12).setString(rs.getString("REMARKS"))
                // TODO: Need to write ODS version dependent method to retrieve some of the info for indexes 13 (From 2.0 defaults for procedure parameters)
                .at(13).setString(extractDefault(rs.getString("COLUMN_DEF")))
                // JDBC reserves 14 and 15 for future use and are always NULL
                .at(14).set(null)
                .at(15).set(null)
                .at(16).setInt(typeMetadata.getCharOctetLength())
                // TODO: Find correct value for ORDINAL_POSITION (+ order of columns and intent, see JDBC-229)
                .at(17).setInt(rs.getInt("PARAMETER_NUMBER"))
                // TODO: Find out if there is a conceptual difference with NULLABLE (idx 11)
                .at(18).setString(nullFlag == 1 ? "NO" : "YES")
                .at(19).setString(toSpecificName(catalog, procedureName))
                .toRowValue(false);
    }

    abstract MetadataQuery createGetProcedureColumnsQuery(String catalog, String procedureNamePattern,
            String columnNamePattern);

    public static GetProcedureColumns create(DbMetadataMediator mediator) {
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

    private static class FB2_5 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_2_5 =
                "select\n"
                + "  null as PROCEDURE_CAT,\n"
                + "  PP.RDB$PROCEDURE_NAME as PROCEDURE_NAME,\n"
                + "  PP.RDB$PARAMETER_NAME as COLUMN_NAME,\n"
                + "  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,\n"
                + "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n"
                + "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n"
                + "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n"
                + "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n"
                + "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n"
                + "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n"
                + "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n"
                + "  F.RDB$NULL_FLAG as NULL_FLAG,\n"
                + "  PP.RDB$DESCRIPTION as REMARKS,\n"
                + "  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,\n"
                + "  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF\n"
                + "from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME";
        private static final String GET_PROCEDURE_COLUMNS_END_2_5 =
                "\norder by PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, PP.RDB$PARAMETER_NUMBER";
        //@formatter:on

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String procedureNamePattern,
                String columnNamePattern) {
            Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
            Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_2_5
                    + (anyCondition(procedureClause, columnClause)
                    ? "\nwhere " + procedureClause.getCondition(columnClause.hasCondition())
                    + columnClause.getCondition(false)
                    : "")
                    + GET_PROCEDURE_COLUMNS_END_2_5;
            return new MetadataQuery(query, Clause.parameters(procedureClause, columnClause));
        }
    }

    private static class FB3 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_3 =
                "select\n"
                + "  null as PROCEDURE_CAT,\n"
                + "  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,\n"
                + "  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,\n"
                + "  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,\n"
                + "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n"
                + "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n"
                + "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n"
                + "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n"
                + "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n"
                + "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n"
                + "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n"
                + "  F.RDB$NULL_FLAG as NULL_FLAG,\n"
                + "  PP.RDB$DESCRIPTION as REMARKS,\n"
                + "  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,\n"
                + "  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF\n"
                + "from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME\n"
                + "where PP.RDB$PACKAGE_NAME is null";
        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_PROCEDURE_COLUMNS_END_3 =
                "\norder by PP.RDB$PACKAGE_NAME, PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, "
                + "PP.RDB$PARAMETER_NUMBER";
        //@formatter:on

        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String procedureNamePattern,
                String columnNamePattern) {
            Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
            Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_3
                    + procedureClause.getCondition("\nand ", "")
                    + columnClause.getCondition("\nand ", "")
                    + GET_PROCEDURE_COLUMNS_END_3;
            return new MetadataQuery(query, Clause.parameters(procedureClause, columnClause));
        }
    }

    private static final class FB3CatalogAsPackage extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_3_W_PKG =
                "select\n"
                + "  coalesce(trim(trailing from PP.RDB$PACKAGE_NAME), '') as PROCEDURE_CAT,\n"
                + "  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,\n"
                + "  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,\n"
                + "  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,\n"
                + "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n"
                + "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n"
                + "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n"
                + "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n"
                + "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n"
                + "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n"
                + "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n"
                + "  F.RDB$NULL_FLAG as NULL_FLAG,\n"
                + "  PP.RDB$DESCRIPTION as REMARKS,\n"
                + "  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,\n"
                + "  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF\n"
                + "from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME";

        private static final String GET_PROCEDURE_COLUMNS_END_3_W_PKG =
                "\norder by PP.RDB$PACKAGE_NAME nulls first, PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, "
                + "PP.RDB$PARAMETER_NUMBER";
        //@formatter:on

        private FB3CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB3CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = new ArrayList<Clause>(3);
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause("PP.RDB$PACKAGE_NAME"));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause("PP.RDB$PACKAGE_NAME", catalog));
                }
            }
            clauses.add(new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern));
            clauses.add(new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern));
            //@formatter:off
            String sql = GET_PROCEDURE_COLUMNS_FRAGMENT_3_W_PKG
                    + (Clause.anyCondition(clauses)
                    ? "\nwhere " + Clause.conjunction(clauses)
                    : "")
                    + GET_PROCEDURE_COLUMNS_END_3_W_PKG;
            //@formatter:on
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }
    }
}
