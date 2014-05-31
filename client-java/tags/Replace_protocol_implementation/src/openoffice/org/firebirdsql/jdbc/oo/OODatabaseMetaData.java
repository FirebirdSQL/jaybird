package org.firebirdsql.jdbc.oo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.*;

public class OODatabaseMetaData extends FBDatabaseMetaData {

    public OODatabaseMetaData(FBConnection c) throws GDSException {
        super(c);
    }

    public OODatabaseMetaData(GDSHelper gdsHelper) {
        super(gdsHelper);
    }

    private static final String DEFAULT_SCHEMA = "DEFAULT";

    @Override
    public ResultSet getSchemas() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR(ISCConstants.SQL_VARYING, 31, "TABLE_SCHEM", "TABLESCHEMAS");

        List<byte[][]> rows = new ArrayList<byte[][]>(1);
        rows.add(new byte[][] { getBytes(DEFAULT_SCHEMA) });

        return new FBResultSet(xsqlvars, rows);
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

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema,
            String table, int scope, boolean nullable) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getBestRowIdentifier(catalog, schema, table, scope,
            nullable);
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema,
            String table, String columnNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getColumnPrivileges(catalog, schema, table,
            columnNamePattern);
    }

    @Override
    public ResultSet getCrossReference(String primaryCatalog,
            String primarySchema, String primaryTable, String foreignCatalog,
            String foreignSchema, String foreignTable) throws SQLException {
        if (DEFAULT_SCHEMA.equals(primarySchema)) primarySchema = null;

        if (DEFAULT_SCHEMA.equals(foreignSchema)) foreignSchema = null;

        return super.getCrossReference(primaryCatalog, primarySchema,
            primaryTable, foreignCatalog, foreignSchema, foreignTable);
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getExportedKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {

        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getImportedKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table,
            boolean unique, boolean approximate) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getIndexInfo(catalog, schema, table, unique, approximate);
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getPrimaryKeys(catalog, schema, table);
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern)
            throws SQLException {

        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getProcedureColumns(catalog, schemaPattern,
            procedureNamePattern, columnNamePattern);
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super
                .getProcedures(catalog, schemaPattern, procedureNamePattern);
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getSuperTables(catalog, schemaPattern, tableNamePattern);
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getSuperTypes(catalog, schemaPattern, tableNamePattern);
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
        List<String> params = new ArrayList<String>();
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
                return new FBResultSet(xsqlvars, Collections.<byte[][]>emptyList());
        }
        
        return processTablePrivileges(xsqlvars, rs);
    }
}
