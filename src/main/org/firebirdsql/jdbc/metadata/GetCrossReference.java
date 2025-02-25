// SPDX-FileCopyrightText: Copyright 2001-2024 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getCrossReference(String, String, String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class GetCrossReference extends AbstractKeysMethod {

    private static final String GET_CROSS_KEYS_START = """
            select
              PK.RDB$RELATION_NAME as PKTABLE_NAME,
              ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,
              FK.RDB$RELATION_NAME as FKTABLE_NAME,
              ISF.RDB$FIELD_NAME as FKCOLUMN_NAME,
              ISP.RDB$FIELD_POSITION + 1 as KEY_SEQ,
              RC.RDB$UPDATE_RULE as UPDATE_RULE,
              RC.RDB$DELETE_RULE as DELETE_RULE,
              PK.RDB$CONSTRAINT_NAME as PK_NAME,
              FK.RDB$CONSTRAINT_NAME as FK_NAME,
              PK.RDB$INDEX_NAME as JB_PK_INDEX_NAME,
              FK.RDB$INDEX_NAME as JB_FK_INDEX_NAME
            from RDB$RELATION_CONSTRAINTS PK
            inner join RDB$REF_CONSTRAINTS RC
              on PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ
            inner join RDB$RELATION_CONSTRAINTS FK
              on FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME
            inner join RDB$INDEX_SEGMENTS ISP
              on ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME
            inner join RDB$INDEX_SEGMENTS ISF
              on ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION
            where\s""";

    private static final String GET_CROSS_KEYS_END = "\norder by FK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";

    private GetCrossReference(DbMetadataMediator mediator) {
        super(mediator);
    }

    public ResultSet getCrossReference(String primaryTable, String foreignTable) throws SQLException {
        if (isNullOrEmpty(primaryTable) || isNullOrEmpty(foreignTable)) {
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
