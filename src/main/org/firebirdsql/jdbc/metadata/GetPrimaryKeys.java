// SPDX-FileCopyrightText: Copyright 2001-2026 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;

/**
 * Provides the implementation for {@link java.sql.DatabaseMetaData#getPrimaryKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetPrimaryKeys extends AbstractMetadataMethod {

    private static final String COLUMNINFO = "COLUMNINFO";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(7)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", COLUMNINFO).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", COLUMNINFO).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", COLUMNINFO).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", COLUMNINFO).addField()
            .at(4).simple(SQL_SHORT, 0, "KEY_SEQ", COLUMNINFO).addField()
            .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "PK_NAME", COLUMNINFO).addField()
            .at(6).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "JB_PK_INDEX_NAME", COLUMNINFO).addField()
            .toRowDescriptor();

    private GetPrimaryKeys(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public final ResultSet getPrimaryKeys(@Nullable String schema, @Nullable String table) throws SQLException {
        if (isNullOrEmpty(table)) {
            return createEmpty();
        }
        MetadataQuery metadataQuery = createGetPrimaryKeysQuery(schema, table);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createGetPrimaryKeysQuery(@Nullable String schema, String table);

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(0).set(null)
                .at(1).setString(rs.getString("TABLE_SCHEM"))
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getString("COLUMN_NAME"))
                .at(4).setShort(rs.getShort("KEY_SEQ"))
                .at(5).setString(rs.getString("PK_NAME"))
                .at(6).setString(rs.getString("JB_PK_INDEX_NAME"))
                .toRowValue(false);
    }

    public static GetPrimaryKeys create(DbMetadataMediator mediator) {
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
    private static final class FB5 extends GetPrimaryKeys {

        private static final String GET_PRIMARY_KEYS_START_5 = """
            select
              cast(null as char(1)) as TABLE_SCHEM,
              RC.RDB$RELATION_NAME as TABLE_NAME,
              ISGMT.RDB$FIELD_NAME as COLUMN_NAME,
              ISGMT.RDB$FIELD_POSITION + 1 as KEY_SEQ,
              RC.RDB$CONSTRAINT_NAME as PK_NAME,
              RC.RDB$INDEX_NAME as JB_PK_INDEX_NAME
            from RDB$RELATION_CONSTRAINTS RC
            inner join RDB$INDEX_SEGMENTS ISGMT
              on RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME
            where RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY'
            and\s""";

        private static final String GET_PRIMARY_KEYS_END_5 = "\norder by ISGMT.RDB$FIELD_NAME";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetPrimaryKeys createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createGetPrimaryKeysQuery(@Nullable String schema, String table) {
            Clause tableClause = Clause.equalsClause("RC.RDB$RELATION_NAME", table);
            String sql = GET_PRIMARY_KEYS_START_5
                    + tableClause.getCondition(false)
                    + GET_PRIMARY_KEYS_END_5;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }

    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetPrimaryKeys {

        private static final String GET_PRIMARY_KEYS_START_6 = """
            select
              trim(trailing from RC.RDB$SCHEMA_NAME) as TABLE_SCHEM,
              trim(trailing from RC.RDB$RELATION_NAME) as TABLE_NAME,
              trim(trailing from ISGMT.RDB$FIELD_NAME) as COLUMN_NAME,
              ISGMT.RDB$FIELD_POSITION + 1 as KEY_SEQ,
              trim(trailing from RC.RDB$CONSTRAINT_NAME) as PK_NAME,
              trim(trailing from RC.RDB$INDEX_NAME) as JB_PK_INDEX_NAME
            from SYSTEM.RDB$RELATION_CONSTRAINTS RC
            inner join SYSTEM.RDB$INDEX_SEGMENTS ISGMT
              on RC.RDB$SCHEMA_NAME = ISGMT.RDB$SCHEMA_NAME and RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME
            where RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY'
            and\s""";

        // For consistent order (e.g. for tests), we're also sorting on schema name.
        // JDBC specifies that the result set is sorted on COLUMN_NAME, so we can't sort on schema first
        private static final String GET_PRIMARY_KEYS_END_6 = "\norder by ISGMT.RDB$FIELD_NAME, ISGMT.RDB$SCHEMA_NAME";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetPrimaryKeys createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetPrimaryKeysQuery(@Nullable String schema, String table) {
            var clauses = new ArrayList<Clause>(2);
            if (schema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("RC.RDB$SCHEMA_NAME", schema));
            }
            clauses.add(Clause.equalsClause("RC.RDB$RELATION_NAME", table));
            String sql = GET_PRIMARY_KEYS_START_6
                    + Clause.conjunction(clauses)
                    + GET_PRIMARY_KEYS_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
