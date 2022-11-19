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

import java.sql.ResultSet;
import java.sql.SQLException;

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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class GetTablePrivileges extends AbstractMetadataMethod {

    private static final RowDescriptor ROW_DESCRIPTOR = new RowDescriptorBuilder(8, DbMetadataMediator.datatypeCoder)
            .at(0).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLEPRIV").addField()
            .at(1).simple(SQL_VARYING | 1, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLEPRIV").addField()
            .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "TABLEPRIV").addField()
            .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", "TABLEPRIV").addField()
            .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", "TABLEPRIV").addField()
            .at(5).simple(SQL_VARYING, 31, "PRIVILEGE", "TABLEPRIV").addField()
            .at(6).simple(SQL_VARYING, 3, "IS_GRANTABLE", "TABLEPRIV").addField()
            .at(7).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "JB_GRANTEE_TYPE", "TABLEPRIV").addField()
            .toRowDescriptor();

    //@formatter:off
    private static final String GET_TABLE_PRIVILEGES_START =
            // Distinct is needed as we're selecting privileges for the table and columns of the table
            "select distinct\n"
            + "  UP.RDB$RELATION_NAME as TABLE_NAME,\n"
            + "  UP.RDB$GRANTOR as GRANTOR,\n"
            + "  UP.RDB$USER as GRANTEE,\n"
            + "  UP.RDB$PRIVILEGE as PRIVILEGE,\n"
            + "  UP.RDB$GRANT_OPTION as IS_GRANTABLE,\n"
            + "  T.RDB$TYPE_NAME as JB_GRANTEE_TYPE\n"
            + "from RDB$USER_PRIVILEGES UP\n"
            + "left join RDB$TYPES T\n"
            + "  on T.RDB$FIELD_NAME = 'RDB$OBJECT_TYPE' and T.RDB$TYPE = UP.RDB$USER_TYPE \n"
            // Other privileges don't make sense for table privileges
            // TODO Consider including ALTER/DROP privileges
            + "where UP.RDB$PRIVILEGE in ('A', 'D', 'I', 'R', 'S', 'U')\n"
            // Only tables and views
            + "and UP.RDB$OBJECT_TYPE in (0, 1)\n";
    
    // NOTE: Sort by user is not defined in JDBC, but we do this to ensure a consistent order for tests
    private static final String GET_TABLE_PRIVILEGES_END = "order by RDB$RELATION_NAME, RDB$PRIVILEGE, RDB$USER";
    //@formatter:on

    private GetTablePrivileges(DbMetadataMediator mediator) {
        super(ROW_DESCRIPTOR, mediator);
    }

    public ResultSet getTablePrivileges(String tableNamePattern) throws SQLException {
        if ("".equals(tableNamePattern)) {
            return createEmpty();
        }
        Clause tableClause = new Clause("RDB$RELATION_NAME", tableNamePattern);

        String sql = GET_TABLE_PRIVILEGES_START
                + tableClause.getCondition("and ", "\n")
                + GET_TABLE_PRIVILEGES_END;
        MetadataQuery metadataQuery = new MetadataQuery(sql, Clause.parameters(tableClause));
        return createMetaDataResultSet(metadataQuery);
    }

    @Override
    RowValue createMetadataRow(ResultSet rs, RowValueBuilder valueBuilder) throws SQLException {
        return valueBuilder
                .at(0).set(null)
                .at(1).set(null)
                .at(2).setString(rs.getString("TABLE_NAME"))
                .at(3).setString(rs.getString("GRANTOR"))
                .at(4).setString(rs.getString("GRANTEE"))
                .at(5).setString(mapPrivilege(rs.getString("PRIVILEGE")))
                .at(6).setString(rs.getBoolean("IS_GRANTABLE") ? "YES" : "NO")
                .at(7).setString(rs.getString("JB_GRANTEE_TYPE"))
                .toRowValue(false);
    }

    public static GetTablePrivileges create(DbMetadataMediator mediator) {
        return new GetTablePrivileges(mediator);
    }
}
