// SPDX-FileCopyrightText: Copyright 2001-2026 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetImportedKeys extends AbstractKeysMethod {

    private GetImportedKeys(DbMetadataMediator mediator) {
        super(mediator);
    }

    public final ResultSet getImportedKeys(@Nullable String schema, @Nullable String table) throws SQLException {
        if (isNullOrEmpty(table)) {
            return createEmpty();
        }
        MetadataQuery metadataQuery = createGetImportedKeysQuery(schema, table);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createGetImportedKeysQuery(@Nullable String schema, String table);

    public static GetImportedKeys create(DbMetadataMediator mediator) {
        // NOTE: Indirection through static method prevents unnecessary classloading
        if (mediator.getFirebirdSupportInfo().isVersionEqualOrAbove(6)) {
            return FB6.createInstance(mediator);
        } else {
            return FB5.createInstance(mediator);
        }
    }

    /**
     * Implementation for Firebird 5.0 and older.
     */
    private static final class FB5 extends GetImportedKeys {

        private static final String GET_IMPORTED_KEYS_START_5 = """
            select
              cast(null as char(1)) as PKTABLE_SCHEM,
              PK.RDB$RELATION_NAME as PKTABLE_NAME,
              ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,
              cast(null as char(1)) as FKTABLE_SCHEM,
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

        private static final String GET_IMPORTED_KEYS_END_5 = "\norder by PK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetImportedKeys createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createGetImportedKeysQuery(@Nullable String schema, String table) {
            Clause tableClause = Clause.equalsClause("FK.RDB$RELATION_NAME", table);
            String sql = GET_IMPORTED_KEYS_START_5
                    + tableClause.getCondition(false)
                    + GET_IMPORTED_KEYS_END_5;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }

    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetImportedKeys {

        private static final String GET_IMPORTED_KEYS_START_6 = """
            select
              trim(trailing from PK.RDB$SCHEMA_NAME) as PKTABLE_SCHEM,
              trim(trailing from PK.RDB$RELATION_NAME) as PKTABLE_NAME,
              trim(trailing from ISP.RDB$FIELD_NAME) as PKCOLUMN_NAME,
              trim(trailing from FK.RDB$SCHEMA_NAME) as FKTABLE_SCHEM,
              trim(trailing from FK.RDB$RELATION_NAME) as FKTABLE_NAME,
              trim(trailing from ISF.RDB$FIELD_NAME) as FKCOLUMN_NAME,
              ISP.RDB$FIELD_POSITION + 1 as KEY_SEQ,
              RC.RDB$UPDATE_RULE as UPDATE_RULE,
              RC.RDB$DELETE_RULE as DELETE_RULE,
              trim(trailing from PK.RDB$CONSTRAINT_NAME) as PK_NAME,
              trim(trailing from FK.RDB$CONSTRAINT_NAME) as FK_NAME,
              trim(trailing from PK.RDB$INDEX_NAME) as JB_PK_INDEX_NAME,
              trim(trailing from FK.RDB$INDEX_NAME) as JB_FK_INDEX_NAME
            from SYSTEM.RDB$RELATION_CONSTRAINTS PK
            inner join SYSTEM.RDB$REF_CONSTRAINTS RC
              on PK.RDB$SCHEMA_NAME = RC.RDB$CONST_SCHEMA_NAME_UQ and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ
            inner join SYSTEM.RDB$RELATION_CONSTRAINTS FK
              on FK.RDB$SCHEMA_NAME = RC.RDB$SCHEMA_NAME and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME
            inner join SYSTEM.RDB$INDEX_SEGMENTS ISP
              on ISP.RDB$SCHEMA_NAME = PK.RDB$SCHEMA_NAME and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME
            inner join SYSTEM.RDB$INDEX_SEGMENTS ISF
              on ISF.RDB$SCHEMA_NAME = FK.RDB$SCHEMA_NAME and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME
                and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION
            where\s""";

        private static final String GET_IMPORTED_KEYS_END_6 =
                "\norder by PK.RDB$SCHEMA_NAME, PK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetImportedKeys createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetImportedKeysQuery(@Nullable String schema, String table) {
            var clauses = new ArrayList<Clause>(2);
            if (schema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("FK.RDB$SCHEMA_NAME", schema));
            }
            clauses.add(Clause.equalsClause("FK.RDB$RELATION_NAME", table));
            String sql = GET_IMPORTED_KEYS_START_6
                    + Clause.conjunction(clauses)
                    + GET_IMPORTED_KEYS_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
