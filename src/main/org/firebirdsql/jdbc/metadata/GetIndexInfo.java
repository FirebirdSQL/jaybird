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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_TEXT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class GetIndexInfo extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(13, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", "INDEXINFO").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "INDEXINFO").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "INDEXINFO").addField()
            .at(3).simple(SQL_TEXT, 1, "NON_UNIQUE", "INDEXINFO").addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_QUALIFIER", "INDEXINFO").addField()
            .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "INDEX_NAME", "INDEXINFO").addField()
            .at(6).simple(SQL_SHORT, 0, "TYPE", "INDEXINFO").addField()
            .at(7).simple(SQL_SHORT, 0, "ORDINAL_POSITION", "INDEXINFO").addField()
            // Field with EXPRESSION_SOURCE (used for expression indexes) in Firebird is actually a blob, using Integer.MAX_VALUE for length
            .at(8).simple(SQL_VARYING, Integer.MAX_VALUE, "COLUMN_NAME", "INDEXINFO").addField()
            .at(9).simple(SQL_TEXT | 1, 1, "ASC_OR_DESC", "INDEXINFO").addField()
            .at(10).simple(SQL_LONG, 0, "CARDINALITY", "INDEXINFO").addField()
            .at(11).simple(SQL_LONG, 0, "PAGES", "INDEXINFO").addField()
            .at(12).simple(SQL_VARYING | 1, 31, "FILTER_CONDITION", "INDEXINFO").addField()
            .toRowDescriptor();

    //@formatter:off
    private static final String GET_INDEX_INFO_START =
            "select\n"
            + "  IND.RDB$RELATION_NAME as TABLE_NAME,\n"
            + "  IND.RDB$UNIQUE_FLAG as UNIQUE_FLAG,\n"
            + "  IND.RDB$INDEX_NAME as INDEX_NAME,\n"
            + "  ISE.RDB$FIELD_POSITION + 1 as ORDINAL_POSITION,\n"
            + "  ISE.RDB$FIELD_NAME as COLUMN_NAME,\n"
            + "  IND.RDB$EXPRESSION_SOURCE as EXPRESSION_SOURCE,\n"
            + "  IND.RDB$INDEX_TYPE as ASC_OR_DESC\n"
            + "from RDB$INDICES IND\n"
            + "left join RDB$INDEX_SEGMENTS ISE on IND.RDB$INDEX_NAME = ISE.RDB$INDEX_NAME "
            + "where ";

    private static final String GET_INDEX_INFO_END =
            "\norder by IND.RDB$UNIQUE_FLAG, IND.RDB$INDEX_NAME, ISE.RDB$FIELD_POSITION";
    //@formatter:on

    private GetIndexInfo(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getIndexInfo(String table, boolean unique, boolean approximate) throws SQLException {
        if (table == null || "".equals(table)) {
            return createEmpty();
        }

        Clause tableClause = Clause.equalsClause("IND.RDB$RELATION_NAME", table);
        String sql = GET_INDEX_INFO_START
                + tableClause.getCondition(unique)
                + (unique ? "IND.RDB$UNIQUE_FLAG = 1" : "")
                + GET_INDEX_INFO_END;
        MetadataQuery metadataQuery = new MetadataQuery(sql, Clause.parameters(tableClause));
        return createMetaDataResultSet(metadataQuery);
    }

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
        case 0:
            valueBuilder.at(9).setString("A");
            break;
        case 1:
            valueBuilder.at(9).setString("D");
            break;
        default:
            valueBuilder.at(9).set(null);
            break;
        }
        // NOTE: We are setting CARDINALITY and PAGES to NULL as we don't have this info; might contravene JDBC spec
        valueBuilder
                // TODO index 10: use 1 / RDB$STATISTICS for approximation of CARDINALITY?
                .at(10).set(null)
                // TODO index 11: query RDB$PAGES for PAGES information?
                .at(11).set(null)
                // Firebird has no filtered indexes
                .at(12).set(null);

        return valueBuilder.toRowValue(false);
    }

    public static GetIndexInfo create(DbMetadataMediator mediator) {
        return new GetIndexInfo(mediator);
    }
}
