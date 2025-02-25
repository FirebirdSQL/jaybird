// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.firebirdsql.internal.tools.MessageConverter.toJaybirdMessageFormat;

/**
 * Uses a single file for error messages, and a single file for SQLstates.
 * <p>
 * This is the format used by Jaybird.
 * </p>
 */
class SingleFileStore extends MessageStore {

    private final Map<Integer, String> messages = new TreeMap<>();
    private final Map<Integer, String> sqlStates = new TreeMap<>();
    private final SymbolStore symbolStore = new SymbolStore();

    @Override
    public void reset() {
        messages.clear();
        sqlStates.clear();
        symbolStore.reset();
    }

    @Override
    void addMessage(Facility facility, int number, String message) {
        messages.put(facility.toErrorCode(number), toJaybirdMessageFormat(message));
    }

    @Override
    void addSqlState(Facility facility, int number, String sqlState) {
        sqlStates.put(facility.toErrorCode(number), sqlState);
    }

    @Override
    void addSymbol(Facility facility, int number, String symbolName) {
        symbolStore.addSymbol(facility, number, symbolName);
    }

    @Override
    public void save() throws IOException {
        store(messages, Path.of("isc_error_msg.properties"));
        store(sqlStates, Path.of("isc_error_sqlstates.properties"));
        symbolStore.save();
    }
}
