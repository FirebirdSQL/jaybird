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
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getCrossReference(String, String, String, String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetCrossReference extends AbstractKeysMethod {

    private GetCrossReference(DbMetadataMediator mediator) {
        super(mediator);
    }

    public final ResultSet getCrossReference(@Nullable String parentSchema, @Nullable String parentTable,
            @Nullable String foreignSchema, @Nullable String foreignTable) throws SQLException {
        if (isNullOrEmpty(parentTable) || isNullOrEmpty(foreignTable)) {
            return createEmpty();
        }
        MetadataQuery metadataQuery = createGetCrossReferenceQuery(parentSchema, parentTable, foreignSchema, foreignTable);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createGetCrossReferenceQuery(@Nullable String parentSchema, String parentTable,
            @Nullable String foreignSchema, String foreignTable);

    public static GetCrossReference create(DbMetadataMediator mediator) {
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
    private static final class FB5 extends GetCrossReference {

        private static final String GET_CROSS_KEYS_START_5 = """
            select
              cast(null as char(1)) AS PKTABLE_SCHEM,
              PK.RDB$RELATION_NAME as PKTABLE_NAME,
              ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,
              cast(null as char(1)) AS FKTABLE_SCHEM,
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

        private static final String GET_CROSS_KEYS_END_5 = "\norder by FK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetCrossReference createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createGetCrossReferenceQuery(@Nullable String parentSchema, String parentTable,
                @Nullable String foreignSchema, String foreignTable) {
            Clause parentTableClause = Clause.equalsClause("PK.RDB$RELATION_NAME", parentTable);
            Clause foreignTableCause = Clause.equalsClause("FK.RDB$RELATION_NAME", foreignTable);
            String sql = GET_CROSS_KEYS_START_5
                    + Clause.conjunction(parentTableClause, foreignTableCause)
                    + GET_CROSS_KEYS_END_5;
            return new MetadataQuery(sql, Clause.parameters(parentTableClause, foreignTableCause));
        }

    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetCrossReference {

        private static final String GET_CROSS_KEYS_START_6 = """
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

        private static final String GET_CROSS_KEYS_END_6 =
                "\norder by FK.RDB$SCHEMA_NAME, FK.RDB$RELATION_NAME, ISP.RDB$FIELD_POSITION";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetCrossReference createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetCrossReferenceQuery(@Nullable String parentSchema, String parentTable,
                @Nullable String foreignSchema, String foreignTable) {
            var clauses = new ArrayList<Clause>(4);
            if (parentSchema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("PK.RDB$SCHEMA_NAME", parentSchema));
            }
            clauses.add(Clause.equalsClause("PK.RDB$RELATION_NAME", parentTable));
            if (foreignSchema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("FK.RDB$SCHEMA_NAME", foreignSchema));
            }
            clauses.add(Clause.equalsClause("FK.RDB$RELATION_NAME", foreignTable));
            String sql = GET_CROSS_KEYS_START_6
                    + Clause.conjunction(clauses)
                    + GET_CROSS_KEYS_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
