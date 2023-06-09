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
