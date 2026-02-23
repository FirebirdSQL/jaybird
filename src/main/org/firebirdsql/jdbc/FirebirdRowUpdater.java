// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.SQLException;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.util.InternalApi;

/**
 * Interface for the clean separation of the classes between result set and corresponding row updater.
 */
@InternalApi
public sealed interface FirebirdRowUpdater permits FBRowUpdater {

    void cancelRowUpdates() throws SQLException;

    void updateRow() throws SQLException;

    void deleteRow() throws SQLException;

    void insertRow() throws SQLException;

    void refreshRow() throws SQLException;

    void moveToInsertRow() throws SQLException;

    void moveToCurrentRow() throws SQLException;

    RowValue getNewRow() throws SQLException;

    RowValue getInsertRow() throws SQLException;

    RowValue getOldRow() throws SQLException;

    void setRow(RowValue row) throws SQLException;
    
    FBField getField(int fieldPosition) throws SQLException;
    
    void close() throws SQLException;
}