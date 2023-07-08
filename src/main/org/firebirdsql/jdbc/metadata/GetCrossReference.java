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

import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getCrossReference(String, String, String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetCrossReference extends AbstractKeysMethod {

    //@formatter:off
    private static final String GET_CROSS_KEYS_START =
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

    private static final String GET_CROSS_KEYS_END =
            "\norder by FK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";
    //@formatter:on

    private GetCrossReference(DbMetadataMediator mediator) {
        super(mediator);
    }

    public ResultSet getCrossReference(String primaryTable, String foreignTable) throws SQLException {
        if (primaryTable == null || "".equals(primaryTable) || foreignTable == null || "".equals(foreignTable)) {
            return createEmpty();
        }

        Clause primaryTableClause = Clause.equalsClause("PK.RDB$RELATION_NAME", primaryTable);
        Clause foreignTableCause = Clause.equalsClause("FK.RDB$RELATION_NAME", foreignTable);
        String sql = GET_CROSS_KEYS_START
                + Clause.conjunction(primaryTableClause, foreignTableCause)
                + GET_CROSS_KEYS_END;
        MetadataQuery metadataQuery = new MetadataQuery(sql, Clause.parameters(primaryTableClause, foreignTableCause));
        return createMetaDataResultSet(metadataQuery);
    }

    public static GetCrossReference create(DbMetadataMediator mediator) {
        return new GetCrossReference(mediator);
    }
}
