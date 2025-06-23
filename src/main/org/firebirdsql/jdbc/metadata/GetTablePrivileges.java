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
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.PrivilegeMapping.mapPrivilege;

/**
 * Provides the implementation of {@link java.sql.DatabaseMetaData#getTablePrivileges(String, String, String)}.
 * <p>
 * This implementation returns <b>all</b> privileges, not just from the current user. The JDBC specification is not
 * clear on this. Looking at the <i>Schemata</i> (SQL standard book 11) definition of the {@code TABLE_PRIVILEGES}
 * view, possibly this should be restricted to the current user and user {@code PUBLIC} (and maybe active roles). This
 * may change in a future version.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract sealed class GetTablePrivileges extends AbstractMetadataMethod {

    private static final String TABLEPRIV = "TABLEPRIV";
    
    private static final RowDescriptor ROW_DESCRIPTOR = DbMetadataMediator.newRowDescriptorBuilder(9)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", TABLEPRIV).addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", TABLEPRIV).addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", TABLEPRIV).addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", TABLEPRIV).addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", TABLEPRIV).addField()
            .at(5).simple(SQL_VARYING, 31, "PRIVILEGE", TABLEPRIV).addField()
            .at(6).simple(SQL_VARYING, 3, "IS_GRANTABLE", TABLEPRIV).addField()
            .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "JB_GRANTEE_TYPE", TABLEPRIV).addField()
            .at(8).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "JB_GRANTEE_SCHEMA", TABLEPRIV).addField()
            .toRowDescriptor();

    private GetTablePrivileges(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public final ResultSet getTablePrivileges(String schemaPattern, String tableNamePattern) throws SQLException {
        if ("".equals(tableNamePattern)) {
            return createEmpty();
        }
        MetadataQuery metadataQuery = createGetTablePrivilegesQuery(schemaPattern, tableNamePattern);
        return createMetaDataResultSet(metadataQuery);
    }

    abstract MetadataQuery createGetTablePrivilegesQuery(String schemaPattern, String tableNamePattern);

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(0).set(null)
                .at(1).setString(rs.getString("TABLE_SCHEM"))
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getString("GRANTOR"))
                .at(4).setString(rs.getString("GRANTEE"))
                .at(5).setString(mapPrivilege(rs.getString("PRIVILEGE")))
                .at(6).setString(rs.getBoolean("IS_GRANTABLE") ? "YES" : "NO")
                .at(7).setString(rs.getString("JB_GRANTEE_TYPE"))
                .at(8).setString(rs.getString("JB_GRANTEE_SCHEMA"))
                .toRowValue(false);
    }

    public static GetTablePrivileges create(DbMetadataMediator mediator) {
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
    private static final class FB5 extends GetTablePrivileges {

        // Distinct is needed as we're selecting privileges for the table and columns of the table
        private static final String GET_TABLE_PRIVILEGES_START_5 = """
            select distinct
              cast(null as char(1)) as TABLE_SCHEM,
              UP.RDB$RELATION_NAME as TABLE_NAME,
              UP.RDB$GRANTOR as GRANTOR,
              UP.RDB$USER as GRANTEE,
              UP.RDB$PRIVILEGE as PRIVILEGE,
              UP.RDB$GRANT_OPTION as IS_GRANTABLE,
              T.RDB$TYPE_NAME as JB_GRANTEE_TYPE,
              cast(null as char(1)) as JB_GRANTEE_SCHEMA
            from RDB$USER_PRIVILEGES UP
            left join RDB$TYPES T
              on T.RDB$FIELD_NAME = 'RDB$OBJECT_TYPE' and T.RDB$TYPE = UP.RDB$USER_TYPE
            where UP.RDB$PRIVILEGE in ('A', 'D', 'I', 'R', 'S', 'U') -- privileges relevant for tables
            and UP.RDB$OBJECT_TYPE in (0, 1) -- Only tables and views""";

        // NOTE: Sort by user is not defined in JDBC, but we do this to ensure a consistent order for tests
        private static final String GET_TABLE_PRIVILEGES_END_5 =
                "\norder by UP.RDB$RELATION_NAME, UP.RDB$PRIVILEGE, UP.RDB$USER";

        private FB5(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetTablePrivileges createInstance(DbMetadataMediator mediator) {
            return new FB5(mediator);
        }

        @Override
        MetadataQuery createGetTablePrivilegesQuery(String schemaPattern, String tableNamePattern) {
            Clause tableClause = new Clause("UP.RDB$RELATION_NAME", tableNamePattern);
            String sql = GET_TABLE_PRIVILEGES_START_5
                    + tableClause.getCondition("\nand ", "")
                    + GET_TABLE_PRIVILEGES_END_5;
            return new MetadataQuery(sql, Clause.parameters(tableClause));
        }

    }

    /**
     * Implementation for Firebird 6.0 and newer.
     */
    private static final class FB6 extends GetTablePrivileges {

        // Distinct is needed as we're selecting privileges for the table and columns of the table
        private static final String GET_TABLE_PRIVILEGES_START_6 = """
            select distinct
              trim(trailing from UP.RDB$RELATION_SCHEMA_NAME) as TABLE_SCHEM,
              trim(trailing from UP.RDB$RELATION_NAME) as TABLE_NAME,
              trim(trailing from UP.RDB$GRANTOR) as GRANTOR,
              trim(trailing from UP.RDB$USER) as GRANTEE,
              UP.RDB$PRIVILEGE as PRIVILEGE,
              UP.RDB$GRANT_OPTION as IS_GRANTABLE,
              trim(trailing from T.RDB$TYPE_NAME) as JB_GRANTEE_TYPE,
              trim(trailing from UP.RDB$USER_SCHEMA_NAME) as JB_GRANTEE_SCHEMA
            from SYSTEM.RDB$USER_PRIVILEGES UP
            left join SYSTEM.RDB$TYPES T
              on T.RDB$FIELD_NAME = 'RDB$OBJECT_TYPE' and T.RDB$TYPE = UP.RDB$USER_TYPE
            where UP.RDB$PRIVILEGE in ('A', 'D', 'I', 'R', 'S', 'U') -- privileges relevant for tables
            and UP.RDB$OBJECT_TYPE in (0, 1) -- Only tables and views""";

        // NOTE: Sort by user schema and user is not defined in JDBC, but we do this to ensure a consistent order for tests
        private static final String GET_TABLE_PRIVILEGES_END_6 =
                "\norder by UP.RDB$RELATION_SCHEMA_NAME, UP.RDB$RELATION_NAME, UP.RDB$PRIVILEGE, "
                        + "UP.RDB$USER_SCHEMA_NAME nulls first, UP.RDB$USER";

        private FB6(DbMetadataMediator mediator) {
            super(mediator);
        }

        private static GetTablePrivileges createInstance(DbMetadataMediator mediator) {
            return new FB6(mediator);
        }

        @Override
        MetadataQuery createGetTablePrivilegesQuery(String schemaPattern, String tableNamePattern) {
            var clauses = List.of(
                    new Clause("UP.RDB$RELATION_SCHEMA_NAME", schemaPattern),
                    new Clause("UP.RDB$RELATION_NAME", tableNamePattern));
            String sql = GET_TABLE_PRIVILEGES_START_6
                    + (Clause.anyCondition(clauses) ? "\nand " + Clause.conjunction(clauses) : "")
                    + GET_TABLE_PRIVILEGES_END_6;
            return new MetadataQuery(sql, Clause.parameters(clauses));
        }

    }

}
