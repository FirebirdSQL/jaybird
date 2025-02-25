// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
