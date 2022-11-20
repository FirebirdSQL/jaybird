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

import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.ForeignKeyActionMapping.mapAction;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class GetImportedKeys extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(14, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PKTABLE_CAT", "COLUMNINFO").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "PKTABLE_SCHEM", "COLUMNINFO").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKTABLE_NAME", "COLUMNINFO").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PKCOLUMN_NAME", "COLUMNINFO").addField()
            .at(4).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FKTABLE_CAT", "COLUMNINFO").addField()
            .at(5).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "FKTABLE_SCHEM", "COLUMNINFO").addField()
            .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKTABLE_NAME", "COLUMNINFO").addField()
            .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FKCOLUMN_NAME", "COLUMNINFO").addField()
            .at(8).simple(SQL_SHORT, 0, "KEY_SEQ", "COLUMNINFO").addField()
            .at(9).simple(SQL_SHORT, 0, "UPDATE_RULE", "COLUMNINFO").addField()
            .at(10).simple(SQL_SHORT, 0, "DELETE_RULE", "COLUMNINFO").addField()
            .at(11).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "FK_NAME", "COLUMNINFO").addField()
            .at(12).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", "COLUMNINFO").addField()
            .at(13).simple(SQL_SHORT, 0, "DEFERRABILITY", "COLUMNINFO").addField()
            .toRowDescriptor();

    //@formatter:off
    private static final String GET_IMPORTED_KEYS_START =
            "select\n"
            + "  PK.RDB$RELATION_NAME as PKTABLE_NAME,\n"
            + "  ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,\n"
            + "  FK.RDB$RELATION_NAME as FKTABLE_NAME,\n"
            + "  ISF.RDB$FIELD_NAME as FKCOLUMN_NAME,\n"
            + "  ISP.RDB$FIELD_POSITION + 1 as KEY_SEQ,\n"
            + "  RC.RDB$UPDATE_RULE as UPDATE_RULE,\n"
            + "  RC.RDB$DELETE_RULE as DELETE_RULE,\n"
            + "  PK.RDB$CONSTRAINT_NAME as PK_NAME,\n"
            + "  FK.RDB$CONSTRAINT_NAME as FK_NAME\n"
            + "from RDB$RELATION_CONSTRAINTS FK\n"
            + "inner join RDB$REF_CONSTRAINTS RC\n"
            + "  on FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME\n"
            + "inner join RDB$RELATION_CONSTRAINTS PK\n"
            + "  on PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ\n"
            + "inner join RDB$INDEX_SEGMENTS ISP\n"
            + " on ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME\n"
            + "inner join RDB$INDEX_SEGMENTS ISF\n"
            + " on ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION\n"
            + "where ";

    private static final String GET_IMPORTED_KEYS_END =
            "\norder by PK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";
    //@formatter:on

    private GetImportedKeys(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getImportedKeys(String table) throws SQLException {
        if (table == null || "".equals(table)) {
            return createEmpty();
        }

        Clause tableClause = Clause.equalsClause("FK.RDB$RELATION_NAME", table);
        String sql = GET_IMPORTED_KEYS_START
                + tableClause.getCondition(false)
                + GET_IMPORTED_KEYS_END;
        MetadataQuery metadataQuery = new MetadataQuery(sql, Clause.parameters(tableClause));
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(2).setString(rs.getString("PKTABLE_NAME"))
                .at(3).setString(rs.getString("PKCOLUMN_NAME"))
                .at(6).setString(rs.getString("FKTABLE_NAME"))
                .at(7).setString(rs.getString("FKCOLUMN_NAME"))
                .at(8).setShort(rs.getShort("KEY_SEQ"))
                .at(9).setShort(mapAction(rs.getString("UPDATE_RULE")))
                .at(10).setShort(mapAction(rs.getString("DELETE_RULE")))
                .at(11).setString(rs.getString("FK_NAME"))
                .at(12).setString(rs.getString("PK_NAME"))
                .at(13).setShort(DatabaseMetaData.importedKeyNotDeferrable)
                .toRowValue(true);
    }

    public static GetImportedKeys create(DbMetadataMediator mediator) {
        return new GetImportedKeys(mediator);
    }
}
