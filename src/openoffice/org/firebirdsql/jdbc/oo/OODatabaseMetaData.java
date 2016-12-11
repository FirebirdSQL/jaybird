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

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OODatabaseMetaData extends FBDatabaseMetaData {

    public OODatabaseMetaData(AbstractConnection c) throws GDSException {
        super(c);
    }

    private static final String DEFAULT_SCHEMA = "DEFAULT";

    public ResultSet getSchemas() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_SCHEM";
        xsqlvars[0].relname = "TABLESCHEMAS";

        ArrayList rows = new ArrayList(1);
        rows.add(new byte[][] { getBytes(DEFAULT_SCHEMA) });

        return new FBResultSet(xsqlvars, rows);
    }

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

    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        checkCatalogAndSchema(catalog, schemaPattern);
        tableNamePattern = stripQuotes(stripEscape(tableNamePattern), true);

        XSQLVAR[] xsqlvars = buildTablePrivilegeRSMetaData();

        Clause tableClause1 = new Clause("RDB$RELATION_NAME", tableNamePattern);
        Clause tableClause2 = new Clause("RDB$RELATION_NAME", tableNamePattern);

        String sql = GET_TABLE_PRIVILEGES_START_1;
        sql += tableClause1.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_1;
        sql += GET_TABLE_PRIVILEGES_START_2;
        sql += tableClause2.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_2;

        // check the original case identifiers first
        ArrayList params = new ArrayList();
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
                return new FBResultSet(xsqlvars, new ArrayList());
        }

        return processTablePrivileges(xsqlvars, rs);
    }
}
