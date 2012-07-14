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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Extension of {@link DatabaseMetaData} interface providing access to Firebird
 * specific features.
 * 
 * @author <a href="mailto:mirommail@web.de">Michael Romankiewicz</a>
 */
public interface FirebirdDatabaseMetaData extends DatabaseMetaData {
    
    /**
     * Get the source of a stored procedure.
     * 
     * @param procedureName
     *            name of the stored procedure.
     * @return source of the stored procedure.
     * @throws SQLException
     *             if specified procedure cannot be found.
     */
    public String getProcedureSourceCode(String procedureName)
            throws SQLException;

    /**
     * Get the source of a trigger.
     * 
     * @param triggerName
     *            name of the trigger.
     * @return source of the trigger.
     * @throws SQLException
     *             if specified trigger cannot be found.
     */
    public String getTriggerSourceCode(String triggerName) throws SQLException;

    /**
     * Get the source of a view.
     * 
     * @param viewName
     *            name of the view.
     * @return source of the view.
     * @throws SQLException
     *             if specified view cannot be found.
     */
    public String getViewSourceCode(String viewName) throws SQLException;
    
    
    // ---- Backported for JDK 1.3 compatibility ----
    
    /**
     * Retrieves the major version number of the underlying database.
     *
     * @return the underlying database's major version
     * @throws SQLException if a database access error occurs
     */
    int getDatabaseMajorVersion() throws SQLException;

    /**
     * Retrieves the minor version number of the underlying database.
     *
     * @return underlying database's minor version
     * @throws SQLException if a database access error occurs
     */
    int getDatabaseMinorVersion() throws SQLException;
    
    /**
     * Get the major version of the ODS (On-Disk Structure) of the database.
     * 
     * @return The major version number of the database itself
     * @exception SQLException if a database access error occurs
     */
    int getOdsMajorVersion() throws SQLException;
    
    /**
     * Get the minor version of the ODS (On-Disk Structure) of the database.
     * 
     * @return The minor version number of the database itself
     * @exception SQLException if a database access error occurs
     */
    int getOdsMinorVersion() throws SQLException;
    
}
