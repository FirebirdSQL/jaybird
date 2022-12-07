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

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.metadata.Clause;
import org.firebirdsql.jdbc.metadata.RowValueBuilder;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.OBJECT_NAME_LENGTH;
import static org.firebirdsql.jdbc.metadata.PrivilegeMapping.mapPrivilege;

/**
 * OpenOffice/LibreOffice specific implementation of {@link java.sql.DatabaseMetaData}.
 *
 * @deprecated Switch to using "Firebird External" in LibreOffice, will be removed in Jaybird 6.
 */
@Deprecated
public class OODatabaseMetaData extends FBDatabaseMetaData {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    // TODO Review metadata methods use of toUpperCase

    public OODatabaseMetaData(FBConnection c) throws SQLException {
        super(c);
    }

    private static final String DEFAULT_SCHEMA = "DEFAULT";

    // TODO Rewrite OO metadata methods as special variant of the org.firebirdsql.jdbc.metadata implementations

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

        tableNamePattern = tableNamePattern.toUpperCase(Locale.ROOT);

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

        String upperTableNamePattern = tableNamePattern.toUpperCase(Locale.ROOT);
        String upperColumnNamePattern = columnNamePattern.toUpperCase(Locale.ROOT);

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
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        final RowDescriptor rowDescriptor = buildTablePrivilegeRSMetaData();

        Clause tableClause1 = new Clause("RDB$RELATION_NAME", tableNamePattern);
        Clause tableClause2 = new Clause("RDB$RELATION_NAME", tableNamePattern);

        String sql = GET_TABLE_PRIVILEGES_START_1;
        sql += tableClause1.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_1;
        sql += GET_TABLE_PRIVILEGES_START_2;
        sql += tableClause2.getCondition();
        sql += GET_TABLE_PRIVILEGES_END_2;

        List<String> params = new ArrayList<>(2);
        if (tableClause1.hasCondition()) {
            params.add(tableClause1.getValue());
        }
        if (tableClause2.hasCondition()) {
            params.add(tableClause2.getValue());
        }

        try (ResultSet rs = doQuery(sql, params)) {
            // if nothing found, return an empty result set
            if (!rs.next()) {
                return new FBResultSet(rowDescriptor, Collections.emptyList());
            }

            return processTablePrivileges(rowDescriptor, rs);
        }
    }

    private RowDescriptor buildTablePrivilegeRSMetaData() {
        return new RowDescriptorBuilder(7, datatypeCoder)
                .at(0).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_CAT", "TABLEPRIV").addField()
                .at(1).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_SCHEM", "TABLEPRIV").addField()
                .at(2).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "TABLE_NAME", "TABLEPRIV").addField()
                .at(3).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTOR", "TABLEPRIV").addField()
                .at(4).simple(SQL_VARYING, OBJECT_NAME_LENGTH, "GRANTEE", "TABLEPRIV").addField()
                .at(5).simple(SQL_VARYING, 31, "PRIVILEGE", "TABLEPRIV").addField()
                .at(6).simple(SQL_VARYING, 31, "IS_GRANTABLE", "TABLEPRIV").addField()
                .toRowDescriptor();
    }

    private FBResultSet processTablePrivileges(final RowDescriptor rowDescriptor, final ResultSet fbTablePrivileges) throws SQLException {
        final List<RowValue> rows = new ArrayList<>();
        final RowValueBuilder valueBuilder = new RowValueBuilder(rowDescriptor);
        do {
            rows.add(valueBuilder
                    .at(2).setString(fbTablePrivileges.getString("TABLE_NAME"))
                    .at(3).setString(fbTablePrivileges.getString("GRANTOR"))
                    .at(4).setString(fbTablePrivileges.getString("GRANTEE"))
                    .at(5).setString(mapPrivilege(fbTablePrivileges.getString("PRIVILEGE")))
                    .at(6).setString(fbTablePrivileges.getShort("IS_GRANTABLE") == 0 ? "NO" : "YES")
                    .toRowValue(true)
            );
        } while (fbTablePrivileges.next());
        return new FBResultSet(rowDescriptor, rows);
    }
}
