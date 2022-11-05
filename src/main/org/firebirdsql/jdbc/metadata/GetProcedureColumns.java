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
import org.firebirdsql.gds.ng.fields.RowValueBuilder;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.Clause.anyCondition;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public abstract class GetProcedureColumns {

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

    private final DbMetadataMediator mediator;

    private GetProcedureColumns(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @see DatabaseMetaData#getProcedureColumns(String, String, String, String) 
     */
    @SuppressWarnings("unused")
    public final ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        if ("".equals(procedureNamePattern) || "".equals(columnNamePattern)) {
            // Matching procedure name or column name not possible
            return new FBResultSet(ROW_DESCRIPTOR, Collections.emptyList());
        }

        MetadataQuery metadataQuery = createGetProcedureColumnsQuery(procedureNamePattern, columnNamePattern);
        try (ResultSet rs = mediator.performMetaDataQuery(metadataQuery)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(ROW_DESCRIPTOR, Collections.emptyList());
            }

            byte[] procedureNoNulls = mediator.createShort(DatabaseMetaData.procedureNoNulls);
            byte[] procedureNullable = mediator.createShort(DatabaseMetaData.procedureNullable);
            byte[] procedureColumnIn = mediator.createShort(DatabaseMetaData.procedureColumnIn);
            byte[] procedureColumnOut = mediator.createShort(DatabaseMetaData.procedureColumnOut);
            byte[] yesBytes = mediator.createString("YES");
            byte[] noBytes = mediator.createString("NO");

            List<RowValue> rows = new ArrayList<>();
            RowValueBuilder valueBuilder = new RowValueBuilder(ROW_DESCRIPTOR);
            do {
                final short columnType = rs.getShort("COLUMN_TYPE");
                // TODO: Find out what the difference is with NULL_FLAG in RDB$PROCEDURE_PARAMETERS (might be ODS dependent)
                final short nullFlag = rs.getShort("NULL_FLAG");
                TypeMetadata typeMetadata = TypeMetadata.builder(mediator.getFirebirdSupportInfo())
                        .fromCurrentRow(rs)
                        .build();

                valueBuilder
                        .at(0).set(null)
                        .at(1).set(null)
                        .at(2).setString(rs.getString("PROCEDURE_NAME"))
                        .at(3).setString(rs.getString("COLUMN_NAME"))
                        // TODO: Unsure if procedureColumnOut is correct, maybe procedureColumnResult, or need ODS dependent use of RDB$PROCEDURE_TYPE to decide on selectable or executable?
                        // TODO: ResultSet columns should not be first according to JDBC 4.3 description
                        .at(4).set(columnType == 0 ? procedureColumnIn : procedureColumnOut)
                        .at(5).setInt(typeMetadata.getJdbcType())
                        .at(6).setString(typeMetadata.getSqlTypeName())
                        .at(7).setInt(typeMetadata.getColumnSize())
                        .at(8).setInt(typeMetadata.getLength())
                        .at(9).setShort(typeMetadata.getScale())
                        .at(10).setShort(typeMetadata.getRadix())
                        .at(11).set(nullFlag == 1 ? procedureNoNulls : procedureNullable)
                        .at(12).setString(rs.getString("REMARKS"))
                        // TODO: Need to write ODS version dependent method to retrieve some of the info for indexes 13 (From 2.0 defaults for procedure parameters)
                        .at(13).set(null)
                        // JDBC reserves 14 and 15 for future use and are always NULL
                        .at(14).set(null)
                        .at(15).set(null)
                        .at(16).setInt(typeMetadata.getCharOctetLength())
                        // TODO: Find correct value for ORDINAL_POSITION (+ order of columns and intent, see JDBC-229)
                        .at(17).setInt(rs.getInt("PARAMETER_NUMBER"))
                        // TODO: Find out if there is a conceptual difference with NULLABLE (idx 11)
                        .at(18).set(nullFlag == 1 ? noBytes : yesBytes)
                        .at(19).set(valueBuilder.get(2));

                rows.add(valueBuilder.toRowValue(false));
            } while (rs.next());
            return new FBResultSet(ROW_DESCRIPTOR, rows);
        }
    }

    abstract MetadataQuery createGetProcedureColumnsQuery(String procedureNamePattern, String columnNamePattern);

    public static GetProcedureColumns create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        if (firebirdSupportInfo.isVersionEqualOrAbove(3, 0)) {
            return new FB3(mediator);
        } else {
            return new FB2_5(mediator);
        }
    }

    private static class FB2_5 extends GetProcedureColumns {

        //@formatter:off
        private static final String GET_PROCEDURE_COLUMNS_FRAGMENT_2_5 =
                "select\n"
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
                + "  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER\n"
                + "from RDB$PROCEDURE_PARAMETERS PP inner join RDB$FIELDS F on PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME";
        private static final String GET_PROCEDURE_COLUMNS_END_2_5 =
                "\norder by PP.RDB$PROCEDURE_NAME, PP.RDB$PARAMETER_TYPE desc, PP.RDB$PARAMETER_NUMBER";
        //@formatter:on

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String procedureNamePattern, String columnNamePattern) {
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
                + "  PP.RDB$PARAMETER_NUMBER + 1 as PARAMETER_NUMBER\n"
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

        @Override
        MetadataQuery createGetProcedureColumnsQuery(String procedureNamePattern, String columnNamePattern) {
            Clause procedureClause = new Clause("PP.RDB$PROCEDURE_NAME", procedureNamePattern);
            Clause columnClause = new Clause("PP.RDB$PARAMETER_NAME", columnNamePattern);
            String query = GET_PROCEDURE_COLUMNS_FRAGMENT_3
                    + procedureClause.getCondition("\nand ", "")
                    + columnClause.getCondition("\nand ", "")
                    + GET_PROCEDURE_COLUMNS_END_3;
            return new MetadataQuery(query, Clause.parameters(procedureClause, columnClause));
        }
    }
}
