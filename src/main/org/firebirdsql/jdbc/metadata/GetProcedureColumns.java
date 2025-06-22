// SPDX-FileCopyrightText: Copyright 2001-2025 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DatabaseMetaData.procedureColumnIn;
import static java.sql.DatabaseMetaData.procedureColumnOut;
import static java.sql.DatabaseMetaData.procedureNoNulls;
import static java.sql.DatabaseMetaData.procedureNullable;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
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
@SuppressWarnings("java:S1192")
public abstract class GetProcedureColumns extends AbstractMetadataMethod {

    private static final String COLUMNINFO = "COLUMNINFO";
    private static final String COLUMN_SCHEMA_NAME = "PP.RDB$SCHEMA_NAME";
    private static final String COLUMN_PACKAGE_NAME = "PP.RDB$PACKAGE_NAME";
    private static final String COLUMN_PROCEDURE_NAME = "PP.RDB$PROCEDURE_NAME";
    private static final String COLUMN_PARAMETER_NAME = "PP.RDB$PARAMETER_NAME";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(20)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_CAT", COLUMNINFO).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PROCEDURE_SCHEM", COLUMNINFO).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PROCEDURE_NAME", COLUMNINFO).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", COLUMNINFO).addField()
            .at(4).simple(SQL_SHORT, 0, "COLUMN_TYPE", COLUMNINFO).addField()
            .at(5).simple(SQL_LONG, 0, "DATA_TYPE", COLUMNINFO).addField()
            .at(6).simple(SQL_VARYING, 31, "TYPE_NAME", COLUMNINFO).addField()
            .at(7).simple(SQL_LONG, 0, "PRECISION", COLUMNINFO).addField()
            .at(8).simple(SQL_LONG, 0, "LENGTH", COLUMNINFO).addField()
            .at(9).simple(SQL_SHORT, 0, "SCALE", COLUMNINFO).addField()
            .at(10).simple(SQL_SHORT, 0, "RADIX", COLUMNINFO).addField()
            .at(11).simple(SQL_SHORT, 0, "NULLABLE", COLUMNINFO).addField()
            // Field in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING, Integer.MAX_VALUE, "REMARKS", COLUMNINFO).addField()
            .at(13).simple(SQL_VARYING, 31, "COLUMN_DEF", COLUMNINFO).addField()
            .at(14).simple(SQL_LONG, 0, "SQL_DATA_TYPE", COLUMNINFO).addField()
            .at(15).simple(SQL_LONG, 0, "SQL_DATETIME_SUB", COLUMNINFO).addField()
            .at(16).simple(SQL_LONG, 0, "CHAR_OCTET_LENGTH", COLUMNINFO).addField()
            .at(17).simple(SQL_LONG, 0, "ORDINAL_POSITION", COLUMNINFO).addField()
            .at(18).simple(SQL_VARYING, 3, "IS_NULLABLE", COLUMNINFO).addField()
            .at(19).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "SPECIFIC_NAME", COLUMNINFO).addField()
            .toRowDescriptor();

    private GetProcedureColumns(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see DatabaseMetaData#getProcedureColumns(String, String, String, String) 
     */
    public final ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        if ("".equals(procedureNamePattern) || "".equals(columnNamePattern)) {
            // Matching procedure name or column name not possible
            return createEmpty();
        }

        MetadataQuery metadataQuery = createGetProcedureColumnsQuery(catalog, schemaPattern, procedureNamePattern,
                columnNamePattern);
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
        String schema = rs.getString("PROCEDURE_SCHEM");
        String procedureName = rs.getString("PROCEDURE_NAME");
        return valueBuilder
                .at(0).setString(catalog)
                .at(1).setString(schema)
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
                .at(19).setString(toSpecificName(catalog, schema, procedureName))
                .toRowValue(false);
    }

    abstract MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern);

    public static GetProcedureColumns create(DbMetadataMediator mediator) {
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
    private static class FB2_5 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_2_5 = """
                select
                  cast(null as CHAR(1)) as PROCEDURE_CAT,
                  cast(null as CHAR(1)) as PROCEDURE_SCHEM,
                  PP.RDB$PROCEDURE_NAME as PROCEDURE_NAME,
                  PP.RDB$PARAMETER_NAME as COLUMN_NAME,
                  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  F.RDB$NULL_FLAG as NULL_FLAG,
                  PP.RDB$DESCRIPTION as REMARKS,
                  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,
                  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF
                from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";
        //@formatter:on

        private static final String GET_PROCEDURE_COLUMNS_END_2_5 =
                "\norder by PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, PP.RDB$PARAMETER_NUMBER";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = List.of(
                    new Clause(COLUMN_PROCEDURE_NAME, procedureNamePattern),
                    new Clause(COLUMN_PARAMETER_NAME, columnNamePattern));
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_2_5
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_PROCEDURE_COLUMNS_END_2_5;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static class FB3 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_3 = """
                select
                  null as PROCEDURE_CAT,
                  null as PROCEDURE_SCHEM,
                  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,
                  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  F.RDB$NULL_FLAG as NULL_FLAG,
                  PP.RDB$DESCRIPTION as REMARKS,
                  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,
                  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF
                from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME
                where PP.RDB$PACKAGE_NAME is null""";
        //@formatter:on

        // NOTE: Including RDB$PACKAGE_NAME so index can be used to sort
        private static final String GET_PROCEDURE_COLUMNS_END_3 =
                "\norder by PP.RDB$PACKAGE_NAME, PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, "
                + "PP.RDB$PARAMETER_NUMBER";


        private FB3(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB3(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = List.of(
                    new Clause(COLUMN_PROCEDURE_NAME, procedureNamePattern),
                    new Clause(COLUMN_PARAMETER_NAME, columnNamePattern));
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_3
                    + (Clause.anyCondition(clauses) ? "\nand " + Clause.conjunction(clauses) : "")
                    + GET_PROCEDURE_COLUMNS_END_3;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static final class FB3CatalogAsPackage extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_3_W_PKG = """
                select
                  coalesce(trim(trailing from PP.RDB$PACKAGE_NAME), '') as PROCEDURE_CAT,
                  null as PROCEDURE_SCHEM,
                  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,
                  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  F.RDB$NULL_FLAG as NULL_FLAG,
                  PP.RDB$DESCRIPTION as REMARKS,
                  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,
                  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF
                from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";
        //@formatter:on

        private static final String GET_PROCEDURE_COLUMNS_END_3_W_PKG =
                "\norder by PP.RDB$PACKAGE_NAME nulls first, PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, "
                + "PP.RDB$PARAMETER_NUMBER";

        private FB3CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB3CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = new ArrayList<Clause>(3);
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause(COLUMN_PACKAGE_NAME));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause(COLUMN_PACKAGE_NAME, catalog));
                }
            }
            clauses.add(new Clause(COLUMN_PROCEDURE_NAME, procedureNamePattern));
            clauses.add(new Clause(COLUMN_PARAMETER_NAME, columnNamePattern));
            String sql = GET_PROCEDURE_COLUMNS_FRAGMENT_3_W_PKG
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_PROCEDURE_COLUMNS_END_3_W_PKG;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

    private static class FB6 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_6 = """
                select
                  null as PROCEDURE_CAT,
                  trim(trailing from PP.RDB$SCHEMA_NAME) as PROCEDURE_SCHEM,
                  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,
                  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  F.RDB$NULL_FLAG as NULL_FLAG,
                  PP.RDB$DESCRIPTION as REMARKS,
                  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,
                  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF
                from SYSTEM.RDB$PROCEDURE_PARAMETERS PP
                inner join SYSTEM.RDB$FIELDS F
                    on PP.RDB$FIELD_SOURCE_SCHEMA_NAME = F.RDB$SCHEMA_NAME and PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME
                where PP.RDB$PACKAGE_NAME is null""";
        //@formatter:on

        private static final String GET_PROCEDURE_COLUMNS_END_6 =
                "\norder by PP.RDB$PACKAGE_NAME, PP.RDB$SCHEMA_NAME, PP.RDB$PROCEDURE_NAME, "
                        + "PP.RDB$PARAMETER_TYPE desc, PP.RDB$PARAMETER_NUMBER";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = List.of(
                    new Clause(COLUMN_SCHEMA_NAME, schemaPattern),
                    new Clause(COLUMN_PROCEDURE_NAME, procedureNamePattern),
                    new Clause(COLUMN_PARAMETER_NAME, columnNamePattern));
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_6
                    + (Clause.anyCondition(clauses) ? "\nand " + Clause.conjunction(clauses) : "")
                    + GET_PROCEDURE_COLUMNS_END_6;
            return new MetadataQuery(query, Clause.parameters(clauses));
        }

    }

    private static final class FB6CatalogAsPackage extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_6_W_PKG = """
                select
                  coalesce(trim(trailing from PP.RDB$PACKAGE_NAME), '') as PROCEDURE_CAT,
                  trim(trailing from PP.RDB$SCHEMA_NAME) as PROCEDURE_SCHEM,
                  trim(trailing from PP.RDB$PROCEDURE_NAME) as PROCEDURE_NAME,
                  trim(trailing from PP.RDB$PARAMETER_NAME) as COLUMN_NAME,
                  PP.RDB$PARAMETER_TYPE as COLUMN_TYPE,
                """ +
                "  F.RDB$FIELD_TYPE as " + FIELD_TYPE + ",\n" +
                "  F.RDB$FIELD_SUB_TYPE as " + FIELD_SUB_TYPE + ",\n" +
                "  F.RDB$FIELD_PRECISION as " + FIELD_PRECISION + ",\n" +
                "  F.RDB$FIELD_SCALE as " + FIELD_SCALE + ",\n" +
                "  F.RDB$FIELD_LENGTH as " + FIELD_LENGTH + ",\n" +
                "  F.RDB$CHARACTER_LENGTH as " + CHAR_LEN + ",\n" +
                "  F.RDB$CHARACTER_SET_ID as " + CHARSET_ID + ",\n" + """
                  F.RDB$NULL_FLAG as NULL_FLAG,
                  PP.RDB$DESCRIPTION as REMARKS,
                  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER,
                  coalesce(PP.RDB$DEFAULT_SOURCE, F.RDB$DEFAULT_SOURCE) as COLUMN_DEF
                from SYSTEM.RDB$PROCEDURE_PARAMETERS PP
                inner join SYSTEM.RDB$FIELDS F
                  on PP.RDB$FIELD_SOURCE_SCHEMA_NAME = F.RDB$SCHEMA_NAME and PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME""";
        //@formatter:on

        private static final String GET_PROCEDURE_COLUMNS_END_6_W_PKG =
                "\norder by PP.RDB$PACKAGE_NAME nulls first, PP.RDB$SCHEMA_NAME, PP.RDB$PROCEDURE_NAME,"
                        + "PP.RDB$PARAMETER_TYPE desc, PP.RDB$PARAMETER_NUMBER";

        private FB6CatalogAsPackage(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetProcedureColumns createInstance(DbMetadataMediator mediator) {
            return new FB6CatalogAsPackage(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String catalog, String schemaPattern, String procedureNamePattern,
                String columnNamePattern) {
            var clauses = new ArrayList<Clause>(4);
            clauses.add(new Clause(COLUMN_SCHEMA_NAME, schemaPattern));
            if (catalog != null) {
                // To quote from the JDBC API: "" retrieves those without a catalog; null means that the catalog name
                // should not be used to narrow the search
                if (catalog.isEmpty()) {
                    clauses.add(Clause.isNullClause(COLUMN_PACKAGE_NAME));
                } else {
                    // Exact matches only
                    clauses.add(Clause.equalsClause(COLUMN_PACKAGE_NAME, catalog));
                }
            }
            clauses.add(new Clause(COLUMN_PROCEDURE_NAME, procedureNamePattern));
            clauses.add(new Clause(COLUMN_PARAMETER_NAME, columnNamePattern));
            String sql = GET_PROCEDURE_COLUMNS_FRAGMENT_6_W_PKG
                    + (Clause.anyCondition(clauses) ? "\nwhere " + Clause.conjunction(clauses) : "")
                    + GET_PROCEDURE_COLUMNS_END_6_W_PKG;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }
}
