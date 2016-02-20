/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc.oo;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;

public class OODatabaseMetaData extends FBDatabaseMetaData {

    public OODatabaseMetaData(FBConnection c) throws SQLException {
        super(c);
    }

    private static final String DEFAULT_SCHEMA = "DEFAULT";

    @Override
    public ResultSet getSchemas() throws SQLException {
        final RowDescriptor rowDescriptor = new RowDescriptorBuilder(2, datatypeCoder)
                .at(0).simple(SQL_VARYING, 31, "TABLE_SCHEM", "TABLESCHEMAS").addField()
                .at(1).simple(SQL_VARYING, 31, "TABLE_CATALOG", "TABLESCHEMAS").addField()
                .toRowDescriptor();

        return new FBResultSet(rowDescriptor,
                Collections.singletonList(RowValue.of(rowDescriptor, getBytes(DEFAULT_SCHEMA), null)));
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException {

        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        FBResultSet rs = (FBResultSet) super.getTables(catalog, schemaPattern,
                tableNamePattern, types);

        if (rs.next()) {
            rs.beforeFirst();
            return rs;
        }

        tableNamePattern = tableNamePattern.toUpperCase();

        return super.getTables(catalog, schemaPattern, tableNamePattern, types);
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern)
            throws SQLException {

        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        FBResultSet rs = (FBResultSet) super.getColumns(catalog, schemaPattern,
                tableNamePattern, columnNamePattern);

        if (rs.next()) {
            rs.beforeFirst();
            return rs;
        }

        String upperTableNamePattern = tableNamePattern.toUpperCase();
        String upperColumnNamePattern = columnNamePattern.toUpperCase();

        rs = (FBResultSet) super.getColumns(catalog, schemaPattern,
                upperTableNamePattern, columnNamePattern);

        if (rs.next()) {
            rs.beforeFirst();
            return rs;
        }

        rs = (FBResultSet) super.getColumns(catalog, schemaPattern,
                tableNamePattern, upperColumnNamePattern);

        if (rs.next()) {
            rs.beforeFirst();
            return rs;
        }

        return super.getColumns(catalog, schemaPattern, upperTableNamePattern,
                upperColumnNamePattern);
    }

    private static final String GET_TABLE_PRIVILEGES_START_1 =
            "SELECT " +
            "null as TABLE_CAT, " +
            "null as TABLE_SCHEM, " +
            "RDB$RELATION_NAME as TABLE_NAME, " +
            "RDB$GRANTOR as GRANTOR, " +
            "RDB$USER as GRANTEE, " +
            "RDB$PRIVILEGE as PRIVILEGE, " +
            "RDB$GRANT_OPTION as IS_GRANTABLE " +
            "FROM RDB$USER_PRIVILEGES " +
            "WHERE ";
    private static final String GET_TABLE_PRIVILEGES_END_1 =
            " CURRENT_USER IN (RDB$USER, RDB$GRANTOR) AND RDB$FIELD_NAME IS NULL AND RDB$OBJECT_TYPE = 0";
    private static final String GET_TABLE_PRIVILEGES_START_2 =
            "UNION " +
            "SELECT " +
            "null as TABLE_CAT, " +
            "null as TABLE_SCHEM, " +
            "RDB$RELATION_NAME as TABLE_NAME, " +
            "RDB$GRANTOR as GRANTOR, " +
            "CURRENT_USER as GRANTEE, " +
            "RDB$PRIVILEGE as PRIVILEGE, " +
            "RDB$GRANT_OPTION as IS_GRANTABLE " +
            "FROM RDB$USER_PRIVILEGES " +
            "WHERE ";
    private static final String GET_TABLE_PRIVILEGES_END_2 =
            " RDB$USER IN (CURRENT_ROLE, 'PUBLIC') AND RDB$FIELD_NAME IS NULL AND RDB$OBJECT_TYPE = 0 " +
            "ORDER BY 3, 6";

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        checkCatalogAndSchema(catalog, schemaPattern);
        tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);

        final RowDescriptor rowDescriptor = buildTablePrivilegeRSMetaData();

        Clause tableClause1 = new Clause("RDB$RELATION_NAME", tableNamePattern);
        Clause tableClause2 = new Clause("RDB$RELATION_NAME", tableNamePattern);

        String sql = GET_TABLE_PRIVILEGES_START_1;
        sql += tableClause1.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_1;
        sql += GET_TABLE_PRIVILEGES_START_2;
        sql += tableClause2.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_2;

        // check the original case identifiers first
        List<String> params = new ArrayList<>();
        if (!tableClause1.getCondition().equals("")) {
            params.add(tableClause1.getOriginalCaseValue());
        }
        if (!tableClause2.getCondition().equals("")) {
            params.add(tableClause2.getOriginalCaseValue());
        }

        ResultSet rs = doQuery(sql, params);

        // if nothing found, check the uppercased identifiers
        if (!rs.next()) {
            params.clear();
            if (!tableClause1.getCondition().equals("")) {
                params.add(tableClause1.getValue());
            }
            if (!tableClause2.getCondition().equals("")) {
                params.add(tableClause2.getValue());
            }

            rs = doQuery(sql, params);

            // if nothing found, return an empty result set
            if (!rs.next())
                return new FBResultSet(rowDescriptor, Collections.<RowValue>emptyList());
        }

        return processTablePrivileges(rowDescriptor, rs);
    }
}
