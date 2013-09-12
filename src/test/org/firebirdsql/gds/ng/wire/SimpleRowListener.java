/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.listeners.RowListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link RowListener} for testing purposes
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class SimpleRowListener implements RowListener {

    private final List<List<FieldValue>> rows = new ArrayList<List<FieldValue>>();
    private Boolean allRowsFetched;
    private Boolean hasResultSet;
    private Boolean hasSingletonResult;

    @Override
    public void newRow(FbStatement sender, List<FieldValue> rowData) {
        rows.add(rowData);
    }

    @Override
    public void allRowsFetched(FbStatement sender) {
        allRowsFetched = true;
    }

    @Override
    public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
        this.hasResultSet = hasResultSet;
        this.hasSingletonResult = hasSingletonResult;
    }

    public Boolean isAllRowsFetched() {
        return allRowsFetched;
    }

    public Boolean hasResultSet() {
        return hasResultSet;
    }

    public Boolean hasSingletonResult() {
        return hasSingletonResult;
    }

    public List<List<FieldValue>> getRows() {
        return rows;
    }
}
