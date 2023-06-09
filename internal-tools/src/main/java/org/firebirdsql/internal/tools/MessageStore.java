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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * API for storing messages.
 */
abstract class MessageStore {

    private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

    /**
     * Resets this message store, clearing currently stored messages.
     */
    abstract void reset();

    /**
     * Add a message.
     *
     * @param facility
     *         facility
     * @param number
     *         error number within the facility
     * @param message
     *         message
     */
    abstract void addMessage(Facility facility, int number, String message);

    /**
     * Add a message.
     *
     * @param code
     *         code of the facility
     * @param number
     *         error number within the facility
     * @param message
     *         message
     */
    final void addMessage(int code, int number, String message) {
        Facility facility = Facility.of(code);
        addMessage(facility, number, message);
    }

    /**
     * Add a SQLSTATE.
     *
     * @param facility
     *         facility
     * @param number
     *         error number within the facility
     * @param sqlState
     *         SQLSTATE
     */
    abstract void addSqlState(Facility facility, int number, String sqlState);

    /**
     * Add a SQLSTATE.
     *
     * @param code
     *         facility
     * @param number
     *         error number within the facility
     * @param sqlState
     *         SQLSTATE
     */
    final void addSqlState(int code, int number, String sqlState) {
        Facility facility = Facility.of(code);
        addSqlState(facility, number, sqlState);
    }

    /**
     * Adds a symbol name (constant) for an error code.
     *
     * @param facility
     *         facility
     * @param number
     *         error number within the facility
     * @param symbolName
     *         symbol name without the {@code isc_} prefix, i.e. as listed in the msg header files
     */
    abstract void addSymbol(Facility facility, int number, String symbolName);

    /**
     * Saves the messages to disk.
     *
     * @throws IOException
     *         For failures to write the files.
     */
    abstract void save() throws IOException;

    final void store(Map<Integer, String> data, Path filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, ISO_8859_1)) {
            store(data, writer);
        }
    }

    final void store(Map<Integer, String> map, BufferedWriter writer) throws IOException {
        // This basically replicates how Properties.store works
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            String key = saveConvert(Integer.toString(entry.getKey()), true);

            // No need to escape embedded and trailing spaces for value, hence pass false to flag.
            String val = saveConvert(entry.getValue(), false);
            writeln(writer, key + "=" + val);
        }
    }

    private static String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
            case ' ':
                if (x == 0 || escapeSpace) outBuffer.append('\\');

                outBuffer.append(' ');
                break;
            case '\\':
                outBuffer.append('\\').append('\\');
                break;
            case '\t':
                outBuffer.append('\\').append('t');
                break;
            case '\n':
                outBuffer.append('\\').append('n');
                break;
            case '\r':
                outBuffer.append('\\').append('r');
                break;
            case '\f':
                outBuffer.append('\\').append('f');
                break;
            default:
                if ((aChar < 0x0020) || (aChar > 0x007e)) {
                    outBuffer.append('\\').append('u')
                            .append(toHex(aChar >> 12))
                            .append(toHex(aChar >> 8))
                            .append(toHex(aChar >> 4))
                            .append(toHex(aChar));
                } else {
                    if (SPECIAL_SAVE_CHARS.indexOf(aChar) != -1) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.newLine();
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xF];
    }
}
