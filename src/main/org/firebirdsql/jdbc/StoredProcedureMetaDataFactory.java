/*
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
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
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory to retrieve meta-data on stored procedures in a Firebird database.
 */
public abstract class StoredProcedureMetaDataFactory {

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

        return versionInformationEqualOrAbove(metaData.getDatabaseMajorVersion(), metaData.getDatabaseMinorVersion(), 2, 1)
                && versionInformationEqualOrAbove(metaData.getOdsMajorVersion(), metaData.getOdsMinorVersion(), 2, 1);
    }

    private static boolean versionInformationEqualOrAbove(int majorVersion, int minorVersion,
            int requiredMajorVersion, int requiredMinorVersion) {

        return majorVersion > requiredMajorVersion ||
                (majorVersion == requiredMajorVersion && minorVersion >= requiredMinorVersion);
    }
}

/**
 * A fully-functional implementation of {@link StoredProcedureMetaData}.
 */
class DefaultCallableStatementMetaData implements StoredProcedureMetaData {

    Set<String> selectableProcedureNames = new HashSet<String>();

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
                    selectableProcedureNames.add(resultSet.getString(1).trim().toUpperCase());
                }
            }
        }
    }

    public boolean canGetSelectableInformation() {
        return true;
    }

    public boolean isSelectable(String procedureName) throws SQLException {
        return selectableProcedureNames.contains(procedureName.toUpperCase());
    }
}

/**
 * A non-functional implementation of {@link StoredProcedureMetaData} for
 * databases that don't have this capability.
 */
class DummyCallableStatementMetaData implements StoredProcedureMetaData {

    public boolean canGetSelectableInformation() {
        return false;
    }

    public boolean isSelectable(String procedureName) throws SQLException {
        throw new FBSQLException("A DummyCallableStatementMetaData can't retrieve selectable settings");
    }

}
