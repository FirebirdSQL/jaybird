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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_TEXT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation of {@link DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)}.
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings("java:S1192")
public abstract sealed class GetIndexInfo extends AbstractMetadataMethod {

    private static final String INDEXINFO = "INDEXINFO";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(13)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", INDEXINFO).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", INDEXINFO).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", INDEXINFO).addField()
            .at(3).simple(SQL_TEXT, 1, "NON_UNIQUE", INDEXINFO).addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_QUALIFIER", INDEXINFO).addField()
            .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_NAME", INDEXINFO).addField()
            .at(6).simple(SQL_SHORT, 0, "TYPE", INDEXINFO).addField()
            .at(7).simple(SQL_SHORT, 0, "ORDINAL_POSITION", INDEXINFO).addField()
            // Field with EXPRESSION_SOURCE (used for expression indexes) in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(8).simple(SQL_VARYING, Integer.MAX_VALUE, "COLUMN_NAME", INDEXINFO).addField()
            .at(9).simple(SQL_TEXT | 1, 1, "ASC_OR_DESC", INDEXINFO).addField()
            .at(10).simple(SQL_LONG, 0, "CARDINALITY", INDEXINFO).addField()
            .at(11).simple(SQL_LONG, 0, "PAGES", INDEXINFO).addField()
            // Field with CONDITION_SOURCE (used for partial indexes) in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(12).simple(SQL_VARYING | 1, Integer.MAX_VALUE, "FILTER_CONDITION", INDEXINFO).addField()
            .toRowDescriptor();

    private GetIndexInfo(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    @SuppressWarnings("unused")
    public ResultSet getIndexInfo(String table, boolean unique, boolean approximate) throws SQLException {
        if (isNullOrEmpty(table)) {
            return createEmpty();
        }

        MetadataQuery metadataQuery = createIndexInfoQuery(table, unique);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createIndexInfoQuery(String table, boolean unique);

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        valueBuilder
                .at(0).set(null)
                .at(1).set(null)
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getInt("UNIQUE_FLAG") == 0 ? "T" : "F")
                .at(4).set(null)
                .at(5).setString(rs.getString("INDEX_NAME"))
                .at(6).setShort(DatabaseMetaData.tableIndexOther);
        String columnName = rs.getString("COLUMN_NAME");
        if (columnName == null) {
            valueBuilder
                    .at(7).setShort(1)
                    .at(8).setString(rs.getString("EXPRESSION_SOURCE"));
        } else {
            valueBuilder
                    .at(7).setShort(rs.getShort("ORDINAL_POSITION"))
                    .at(8).setString(columnName);
        }
        switch (rs.getInt("ASC_OR_DESC")) {
        case 0 -> valueBuilder.at(9).setString("A");
        case 1 -> valueBuilder.at(9).setString("D");
        default -> valueBuilder.at(9).set(null);
        }
        // NOTE: We are setting CARDINALITY and PAGES to NULL as we don't have this info; might contravene JDBC spec
        valueBuilder
                // TODO index 10: use 1 / RDB$STATISTICS for approximation of CARDINALITY?
                .at(10).set(null)
                // TODO index 11: query RDB$PAGES for PAGES information?
                .at(11).set(null)
                .at(12).setString(rs.getString("CONDITION_SOURCE"));

        return valueBuilder.toRowValue(false);
    }

    public static GetIndexInfo create(DbMetadataMediator mediator) {
        FirebirdSupportInfo firebirdSupportInfo = mediator.getFirebirdSupportInfo();
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (firebirdSupportInfo.isVersionEqualOrAbove(5)) {
            return FB5.createInstance(mediator);
        } else {
            return FB2_5.createInstance(mediator);
        }
    }

    private static final class FB2_5 extends GetIndexInfo {

        private static final String GET_INDEX_INFO_START_2_5 = """
            select
              IND.RDB$RELATION_NAME as TABLE_NAME,
              IND.RDB$UNIQUE_FLAG as UNIQUE_FLAG,
              IND.RDB$INDEX_NAME as INDEX_NAME,
              ISE.RDB$FIELD_POSITION + 1 as ORDINAL_POSITION,
              ISE.RDB$FIELD_NAME as COLUMN_NAME,
              IND.RDB$EXPRESSION_SOURCE as EXPRESSION_SOURCE,
              IND.RDB$INDEX_TYPE as ASC_OR_DESC,
              null as CONDITION_SOURCE
            from RDB$INDICES IND
            left join RDB$INDEX_SEGMENTS ISE on IND.RDB$INDEX_NAME = ISE.RDB$INDEX_NAME where\s""";

        private static final String GET_INDEX_INFO_END_2_5 =
                "\norder by IND.RDB$UNIQUE_FLAG, IND.RDB$INDEX_NAME, ISE.RDB$FIELD_POSITION";

        private FB2_5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetIndexInfo createInstance(DbMetadataMediator mediator) {
            return new FB2_5(mediator);
        }

        @Override
        MetadataQuery createIndexInfoQuery(String table, boolean unique) {
            Clause tableClause = Clause.equalsClause("IND.RDB$RELATION_NAME", table);
            String sql = GET_INDEX_INFO_START_2_5
                    + tableClause.getCondition(unique)
                    + (unique ? "IND.RDB$UNIQUE_FLAG = 1" : "")
                    + GET_INDEX_INFO_END_2_5;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }

    }

    private static final class FB5 extends GetIndexInfo {

        private static final String GET_INDEX_INFO_START_5 = """
            select
              trim(trailing from IND.RDB$RELATION_NAME) as TABLE_NAME,
              IND.RDB$UNIQUE_FLAG as UNIQUE_FLAG,
              trim(trailing from IND.RDB$INDEX_NAME) as INDEX_NAME,
              ISE.RDB$FIELD_POSITION + 1 as ORDINAL_POSITION,
              trim(trailing from ISE.RDB$FIELD_NAME) as COLUMN_NAME,
              IND.RDB$EXPRESSION_SOURCE as EXPRESSION_SOURCE,
              IND.RDB$INDEX_TYPE as ASC_OR_DESC,
              IND.RDB$CONDITION_SOURCE as CONDITION_SOURCE
            from RDB$INDICES IND
            left join RDB$INDEX_SEGMENTS ISE on IND.RDB$INDEX_NAME = ISE.RDB$INDEX_NAME where\s""";

        private static final String GET_INDEX_INFO_END_5 =
                "\norder by IND.RDB$UNIQUE_FLAG, IND.RDB$INDEX_NAME, ISE.RDB$FIELD_POSITION";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetIndexInfo createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createIndexInfoQuery(String table, boolean unique) {
            Clause tableClause = Clause.equalsClause("IND.RDB$RELATION_NAME", table);
            String sql = GET_INDEX_INFO_START_5
                    + tableClause.getCondition(unique)
                    + (unique ? "IND.RDB$UNIQUE_FLAG = 1" : "")
                    + GET_INDEX_INFO_END_5;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }

    }

}
