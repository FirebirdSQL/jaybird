// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common implementation for associating symbol names and their error code and writing it out suitable for copy/past
 * into ISCConstants.java.
 *
 * @author Mark Rotteveel
 */
final class SymbolStore {

    private final Map<Integer, String> symbolMap = new TreeMap<>();

    void reset() {
        symbolMap.clear();
    }

    void addSymbol(Facility facility, int number, String symbolName) {
        symbolMap.put(facility.toErrorCode(number), symbolName);
    }

    void save() throws IOException {
        final String lineSeparator = System.lineSeparator();
        try (var writer = Files.newBufferedWriter(Path.of("fragment-ISCConstants.txt"))) {
            for (Map.Entry<Integer, String> entry : symbolMap.entrySet()) {
                writer.append("    ").append("int isc_").append(entry.getValue()).append(" = ")
                        .append(String.valueOf(entry.getKey())).append(';').append(lineSeparator);
            }
        }
    }

}
