/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
final class SimpleFetcherListener implements FBObjectListener.FetcherListener {

    final List<@Nullable RowValue> receivedRows = new ArrayList<>();

    @Override
    public void rowChanged(FBFetcher fetcher, @Nullable RowValue newRow) {
        receivedRows.add(newRow);
    }

    void assertRow(int rowIndex, Consumer<@Nullable RowValue> assertion) {
        assertTrue(0 <= rowIndex && rowIndex < receivedRows.size(), () -> "row index out of range: " + rowIndex);
        assertion.accept(receivedRows.get(rowIndex));
    }

    void clearRows() {
        receivedRows.clear();
    }
    
}
