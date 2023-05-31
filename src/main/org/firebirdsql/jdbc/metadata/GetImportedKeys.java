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

import org.firebirdsql.jdbc.metadata.DbMetadataMediator.MetadataQuery;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetImportedKeys extends AbstractKeysMethod {

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
            + "from RDB$RELATION_CONSTRAINTS PK\n"
            + "inner join RDB$REF_CONSTRAINTS RC\n"
            + "  on PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ\n"
            + "inner join RDB$RELATION_CONSTRAINTS FK\n"
            + "  on FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME\n"
            + "inner join RDB$INDEX_SEGMENTS ISP\n"
            + "  on ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME\n"
            + "inner join RDB$INDEX_SEGMENTS ISF\n"
            + "  on ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION\n"
            + "where ";

    private static final String GET_IMPORTED_KEYS_END =
            "\norder by PK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";
    //@formatter:on

    private GetImportedKeys(DbMetadataMediator mediator) {
        super(mediator);
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

    public static GetImportedKeys create(DbMetadataMediator mediator) {
        return new GetImportedKeys(mediator);
    }
}
