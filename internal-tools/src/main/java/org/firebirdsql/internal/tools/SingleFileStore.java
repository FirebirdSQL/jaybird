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
