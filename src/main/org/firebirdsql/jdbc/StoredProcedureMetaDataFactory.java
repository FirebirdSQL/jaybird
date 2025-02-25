// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;

/**
 * Factory to retrieve meta-data on stored procedures in a Firebird database.
 */
final class StoredProcedureMetaDataFactory {

    private StoredProcedureMetaDataFactory() {
        // no instances
    }

    /**
     * Retrieve a {@link StoredProcedureMetaData} object for a Connection.
     *
     * @param connection
     *         The connection for which data is to be retrieved
     * @return {@link StoredProcedureMetaData} for the current connection
     * @throws SQLException
     *         if an exception occurs while retrieving meta-data
     */
    public static StoredProcedureMetaData getInstance(FBConnection connection) throws SQLException {
        if (connectionHasProcedureMetadata(connection)) {
            return new DefaultCallableStatementMetaData(connection);
        } else {
            return new DummyCallableStatementMetaData();
        }
    }

    private static boolean connectionHasProcedureMetadata(FBConnection connection) throws SQLException {
        if (connection.isIgnoreProcedureType()) {
            return false;
        }
        FirebirdDatabaseMetaData metaData = (FirebirdDatabaseMetaData) connection.getMetaData();

        return versionEqualOrAboveFB21(metaData.getDatabaseMajorVersion(), metaData.getDatabaseMinorVersion())
                && versionEqualOrAboveFB21(metaData.getOdsMajorVersion(), metaData.getOdsMinorVersion());
    }

    private static boolean versionEqualOrAboveFB21(int majorVersion, int minorVersion) {
        return majorVersion > 2 ||
                (majorVersion == 2 && minorVersion >= 1);
    }
}

/**
 * A fully-functional implementation of {@link StoredProcedureMetaData}.
 */
final class DefaultCallableStatementMetaData implements StoredProcedureMetaData {

    final Set<String> selectableProcedureNames = new HashSet<>();

    public DefaultCallableStatementMetaData(Connection connection)
            throws SQLException {
        loadSelectableProcedureNames(connection);
    }

    private void loadSelectableProcedureNames(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // TODO Replace with looking for specific procedure
            String sql = "SELECT RDB$PROCEDURE_NAME FROM RDB$PROCEDURES WHERE RDB$PROCEDURE_TYPE = 1";
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    selectableProcedureNames.add(resultSet.getString(1).trim().toUpperCase(Locale.ROOT));
                }
            }
        }
    }

    public boolean canGetSelectableInformation() {
        return true;
    }

    public boolean isSelectable(String procedureName) {
        return selectableProcedureNames.contains(procedureName.toUpperCase(Locale.ROOT));
    }
}

/**
 * A non-functional implementation of {@link StoredProcedureMetaData} for databases that don't have this capability.
 */
final class DummyCallableStatementMetaData implements StoredProcedureMetaData {

    public boolean canGetSelectableInformation() {
        return false;
    }

    public boolean isSelectable(String procedureName) throws SQLException {
        throw new SQLNonTransientException("A DummyCallableStatementMetaData can't retrieve selectable settings",
                SQL_STATE_GENERAL_ERROR);
    }

}
