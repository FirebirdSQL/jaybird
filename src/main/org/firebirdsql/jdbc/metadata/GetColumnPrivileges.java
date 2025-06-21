// SPDX-FileCopyrightText: Copyright 2001-2025 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.DbMetadataMediator;
import org.firebirdsql.jdbc.DbMetadataMediator.MetadataQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.PrivilegeMapping.mapPrivilege;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getColumnPrivileges(String, String, String, String)}.
 * <p>
 * This implementation returns <b>all</b> privileges, not just from the current user. The JDBC specification is not
 * clear on this. Looking at the <i>Schemata</i> (SQL standard book 11) definition of the {@code COLUMN_PRIVILEGES}
 * view, possibly this should be restricted to the current user and user {@code PUBLIC} (and maybe active roles). This
 * may change in a future version.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class GetColumnPrivileges extends AbstractMetadataMethod {

    private static final String COLUMNPRIV = "COLUMNPRIV";
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(9)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", COLUMNPRIV).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", COLUMNPRIV).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", COLUMNPRIV).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "COLUMN_NAME", COLUMNPRIV).addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", COLUMNPRIV).addField()
            .at(5).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", COLUMNPRIV).addField()
            .at(6).simple(SQL_VARYING, 31, "PRIVILEGE", COLUMNPRIV).addField()
            .at(7).simple(SQL_VARYING, 3, "IS_GRANTABLE", COLUMNPRIV).addField()
            .at(8).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "JB_GRANTEE_TYPE", COLUMNPRIV).addField()
            .toRowDescriptor();

    GetColumnPrivileges(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    /**
     * @see java.sql.DatabaseMetaData#getColumnPrivileges(String, String, String, String) 
     */
    public ResultSet getColumnPrivileges(String schema, String table, String columnNamePattern) throws SQLException {
        if (isNullOrEmpty(table) || "".equals(columnNamePattern)) {
            return createEmpty();
        }
        MetadataQuery metadataQuery = createMetadataQuery(schema, table, columnNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createMetadataQuery(String schema, String table, String columnNamePattern);

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(0).set(null)
                .at(1).setString(rs.getString("TABLE_SCHEM"))
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getString("COLUMN_NAME"))
                .at(4).setString(rs.getString("GRANTOR"))
                .at(5).setString(rs.getString("GRANTEE"))
                .at(6).setString(mapPrivilege(rs.getString("PRIVILEGE")))
                .at(7).setString(rs.getBoolean("IS_GRANTABLE") ? "YES" : "NO")
                .at(8).setString(rs.getString("JB_GRANTEE_TYPE"))
                .toRowValue(false);
    }

    public static GetColumnPrivileges create(DbMetadataMediator mediator) {
        if (mediator.getFirebirdSupportInfo().isVersionEqualOrAbove(6)) {
            return FB6.createInstance(mediator);
        } else {
            return FB5.createInstance(mediator);
        }
    }

    /**
     * Implementation for Firebird 5.0 and older.
     */
    private static final class FB5 extends GetColumnPrivileges {

        private static final String GET_COLUMN_PRIVILEGES_START_5 = """
            select distinct
              cast(null as char(1)) as TABLE_SCHEM,
              RF.RDB$RELATION_NAME as TABLE_NAME,
              RF.RDB$FIELD_NAME as COLUMN_NAME,
              UP.RDB$GRANTOR as GRANTOR,
              UP.RDB$USER as GRANTEE,
              UP.RDB$PRIVILEGE as PRIVILEGE,
              UP.RDB$GRANT_OPTION as IS_GRANTABLE,
              T.RDB$TYPE_NAME as JB_GRANTEE_TYPE
            from RDB$RELATION_FIELDS RF
            inner join RDB$USER_PRIVILEGES UP
              on UP.RDB$RELATION_NAME = RF.RDB$RELATION_NAME
                and (UP.RDB$FIELD_NAME is null or UP.RDB$FIELD_NAME = RF.RDB$FIELD_NAME)
            left join RDB$TYPES T
              on T.RDB$FIELD_NAME = 'RDB$OBJECT_TYPE' and T.RDB$TYPE = UP.RDB$USER_TYPE
            where UP.RDB$PRIVILEGE in ('A', 'D', 'I', 'R', 'S', 'U') -- privileges relevant for columns
            and UP.RDB$OBJECT_TYPE in (0, 1) -- only tables and views
            and\s""";

        // NOTE: Sort by user is not defined in JDBC, but we do this to ensure a consistent order for tests
        private static final String GET_COLUMN_PRIVILEGES_END_5 =
                "\norder by RF.RDB$FIELD_NAME, UP.RDB$PRIVILEGE, UP.RDB$USER";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetColumnPrivileges createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createMetadataQuery(String schema, String table, String columnNamePattern) {
            var clauses = List.of(
                    Clause.equalsClause("RF.RDB$RELATION_NAME", table),
                    new Clause("RF.RDB$FIELD_NAME", columnNamePattern));
            String sql = GET_COLUMN_PRIVILEGES_START_5
                    + Clause.conjunction(clauses)
                    + GET_COLUMN_PRIVILEGES_END_5;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

    /**
     * Implementation for Firebird 6.0 and higher.
     */
    private static final class FB6 extends GetColumnPrivileges {

        private static final String GET_COLUMN_PRIVILEGES_START_6 = """
            select distinct
              trim(trailing from RF.RDB$SCHEMA_NAME) as TABLE_SCHEM,
              trim(trailing from RF.RDB$RELATION_NAME) as TABLE_NAME,
              trim(trailing from RF.RDB$FIELD_NAME) as COLUMN_NAME,
              trim(trailing from UP.RDB$GRANTOR) as GRANTOR,
              trim(trailing from UP.RDB$USER) as GRANTEE,
              UP.RDB$PRIVILEGE as PRIVILEGE,
              UP.RDB$GRANT_OPTION as IS_GRANTABLE,
              T.RDB$TYPE_NAME as JB_GRANTEE_TYPE
            from SYSTEM.RDB$RELATION_FIELDS RF
            inner join SYSTEM.RDB$USER_PRIVILEGES UP
              on UP.RDB$RELATION_SCHEMA_NAME = RF.RDB$SCHEMA_NAME and UP.RDB$RELATION_NAME = RF.RDB$RELATION_NAME
                and (UP.RDB$FIELD_NAME is null or UP.RDB$FIELD_NAME = RF.RDB$FIELD_NAME)
            left join SYSTEM.RDB$TYPES T
              on T.RDB$FIELD_NAME = 'RDB$OBJECT_TYPE' and T.RDB$TYPE = UP.RDB$USER_TYPE
            where UP.RDB$PRIVILEGE in ('A', 'D', 'I', 'R', 'S', 'U') -- privileges relevant for columns
            and UP.RDB$OBJECT_TYPE in (0, 1) -- only tables and views
            and\s""";

        // NOTE: Sort by user and schema is not defined in JDBC, but we do this to ensure a consistent order for tests
        private static final String GET_COLUMN_PRIVILEGES_END_6 =
                "\norder by RF.RDB$FIELD_NAME, UP.RDB$PRIVILEGE, UP.RDB$USER, RF.RDB$SCHEMA_NAME";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetColumnPrivileges createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createMetadataQuery(String schema, String table, String columnNamePattern) {
            var clauses = new ArrayList<Clause>(3);
            if (schema != null) {
                // NOTE: empty string will return no rows as required ("" retrieves those without a schema)
                clauses.add(Clause.equalsClause("RF.RDB$SCHEMA_NAME", schema));
            }
            clauses.add(Clause.equalsClause("RF.RDB$RELATION_NAME", table));
            clauses.add(new Clause("RF.RDB$FIELD_NAME", columnNamePattern));
            String sql = GET_COLUMN_PRIVILEGES_START_6
                    + Clause.conjunction(clauses)
                    + GET_COLUMN_PRIVILEGES_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
