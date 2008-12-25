package org.firebirdsql.jdbc.oo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.*;

public class OODatabaseMetaData extends FBDatabaseMetaData {

    public OODatabaseMetaData(AbstractConnection c) throws GDSException {
        super(c);
    }

    public OODatabaseMetaData(GDSHelper gdsHelper) {
        super(gdsHelper);
    }

    public static final String TABLE = "TABLE";

    public static final String SYSTEM_TABLE = "SYSTEM TABLE";

    public static final String VIEW = "VIEW";

    public static final String[] ALL_TYPES = { TABLE, SYSTEM_TABLE, VIEW};

    private static final String DEFAULT_SCHEMA = "DEFAULT";

    public ResultSet getSchemas() throws SQLException {
        XSQLVAR[] xsqlvars = new XSQLVAR[1];

        xsqlvars[0] = new XSQLVAR();
        xsqlvars[0].sqltype = ISCConstants.SQL_VARYING;
        xsqlvars[0].sqllen = 31;
        xsqlvars[0].sqlname = "TABLE_SCHEM";
        xsqlvars[0].relname = "TABLESCHEMAS";

        ArrayList rows = new ArrayList(1);
        rows.add(new byte[][] { getBytes(DEFAULT_SCHEMA)});

        return new OOResultSet(xsqlvars, rows);
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

    public ResultSet getBestRowIdentifier(String catalog, String schema,
            String table, int scope, boolean nullable) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getBestRowIdentifier(catalog, schema, table, scope,
            nullable);
    }

    public ResultSet getColumnPrivileges(String catalog, String schema,
            String table, String columnNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getColumnPrivileges(catalog, schema, table,
            columnNamePattern);
    }

    public ResultSet getCrossReference(String primaryCatalog,
            String primarySchema, String primaryTable, String foreignCatalog,
            String foreignSchema, String foreignTable) throws SQLException {
        if (DEFAULT_SCHEMA.equals(primarySchema)) primarySchema = null;

        if (DEFAULT_SCHEMA.equals(foreignSchema)) foreignSchema = null;

        return super.getCrossReference(primaryCatalog, primarySchema,
            primaryTable, foreignCatalog, foreignSchema, foreignTable);
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getExportedKeys(catalog, schema, table);
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {

        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getImportedKeys(catalog, schema, table);
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table,
            boolean unique, boolean approximate) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getIndexInfo(catalog, schema, table, unique, approximate);
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
        if (DEFAULT_SCHEMA.equals(schema)) schema = null;

        return super.getPrimaryKeys(catalog, schema, table);
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern)
            throws SQLException {

        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getProcedureColumns(catalog, schemaPattern,
            procedureNamePattern, columnNamePattern);
    }

    public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super
                .getProcedures(catalog, schemaPattern, procedureNamePattern);
    }

    public ResultSet getSuperTables(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getSuperTables(catalog, schemaPattern, tableNamePattern);
    }

    public ResultSet getSuperTypes(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getSuperTypes(catalog, schemaPattern, tableNamePattern);
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException {
        if (DEFAULT_SCHEMA.equals(schemaPattern)) schemaPattern = null;

        return super.getTablePrivileges(catalog, schemaPattern,
            tableNamePattern);
    }

    public String stripEscape(String pattern) {
        return super.stripEscape(pattern);
    }

    public String stripQuotes(String pattern) {
        if ((pattern.length() >= 2) && (pattern.charAt(0) == '\"')
                && (pattern.charAt(pattern.length() - 1) == '\"')) {
            return pattern.substring(1, pattern.length() - 1);
        } else {
            return pattern;
        }
    }

    private class Clause {

        private String condition = "";

        private String value;

        public Clause(String columnName, String pattern) {
            if (pattern == null) {
                return;
            } else if (isAllCondition(pattern)) {
                // do nothing to tableCondition
                return;
            } else if (hasNoWildcards(pattern)) {
                value = stripQuotes(stripEscape(pattern));
                condition = columnName + " = ? and ";
            } else {
                value = stripQuotes(pattern) + SPACES + "%";
                condition = columnName + " || '" + SPACES
                        + "' like ? escape '\\' and ";
            }
        }

        public String getCondition() {
            return condition;
        }

        public String getValue() {
            return value;
        }
    }

}
