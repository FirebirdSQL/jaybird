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
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntFunction;

import static java.lang.String.format;
import static org.firebirdsql.internal.tools.MessageConverter.toJaybirdMessageFormat;

/**
 * Uses a file for error messages and a file for SQLstates per facility.
 * <p>
 * This format is currently not used, but we might use this for Jaybird in the future.
 * </p>
 */
class PerFacilityStore extends MessageStore {

    @SuppressWarnings("unused")
    private static final int MAX_FACILITY = 25; // NOTE: This is Firebird max, Jaybird has facility 26

    private final Map<Facility, Map<Integer, String>> messagesPerFacility = new EnumMap<>(Facility.class);
    private final Map<Facility, Map<Integer, String>> sqlStatesPerFacility = new EnumMap<>(Facility.class);
    private final SymbolStore symbolStore = new SymbolStore();

    @Override
    public void reset() {
        messagesPerFacility.clear();
        sqlStatesPerFacility.clear();
    }

    @Override
    void addMessage(Facility facility, int number, String message) {
        add(messagesPerFacility, facility, number, toJaybirdMessageFormat(message));
    }

    @Override
    void addSqlState(Facility facility, int number, String sqlState) {
        add(sqlStatesPerFacility, facility, number, sqlState);
    }

    @Override
    void addSymbol(Facility facility, int number, String symbolName) {
        symbolStore.addSymbol(facility, number, symbolName);
    }

    private static void add(
            Map<Facility, Map<Integer, String>> facilityMap, Facility facility, int number, String data) {
        Map<Integer, String> facilityData = facilityMap.computeIfAbsent(facility, k -> new TreeMap<>());
        facilityData.put(facility.toErrorCode(number), data);
    }

    @Override
    public void save() throws IOException {
        save(messagesPerFacility, code -> format("error_messages_%d.properties", code));
        save(sqlStatesPerFacility, code -> format("sql_states_%d.properties", code));
        symbolStore.save();
    }

    private void save(Map<Facility, Map<Integer, String>> facilityMap, IntFunction<String> filenameGenerator)
            throws IOException {
        for (Map.Entry<Facility, Map<Integer, String>> facilityEntry : facilityMap.entrySet()) {
            Map<Integer, String> facilityData = facilityEntry.getValue();
            if (facilityData.isEmpty()) {
                continue;
            }
            String fileName = filenameGenerator.apply(facilityEntry.getKey().code());
            store(facilityData, Path.of(fileName));
        }
    }
}
