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

import java.sql.SQLException;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.field.FBField;

/**
 * INTERNAL INTERFACE: might be changed in the future!
 * 
 * Interface for the clean separation of the classes between result set and 
 * corresponding row updater. 
 */
interface FirebirdRowUpdater {

    void cancelRowUpdates() throws SQLException;

    void updateRow() throws SQLException;

    void deleteRow() throws SQLException;

    void insertRow() throws SQLException;

    void refreshRow() throws SQLException;

    boolean rowInserted() throws SQLException;

    boolean rowDeleted() throws SQLException;

    boolean rowUpdated() throws SQLException;

    void moveToInsertRow() throws SQLException;

    void moveToCurrentRow() throws SQLException;

    RowValue getNewRow() throws SQLException;

    RowValue getInsertRow() throws SQLException;

    RowValue getOldRow() throws SQLException;

    void setRow(RowValue row) throws SQLException;
    
    FBField getField(int fieldPosition) throws SQLException;
    
    void close() throws SQLException;
}