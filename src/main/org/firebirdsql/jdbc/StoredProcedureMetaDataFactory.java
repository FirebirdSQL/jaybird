/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
