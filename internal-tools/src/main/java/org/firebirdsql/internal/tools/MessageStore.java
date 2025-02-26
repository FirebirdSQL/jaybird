// SPDX-FileCopyrightText: Copyright 2004-2008 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * API for storing messages.
 */
abstract class MessageStore implements FirebirdErrorStore {

    private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

    @Override
    public void addFirebirdError(FirebirdError firebirdError) {
        addMessage(firebirdError.facility(), firebirdError.numberInFacility(), firebirdError.message());
        if (firebirdError.hasSqlState()) {
            addSqlState(firebirdError.facility(), firebirdError.numberInFacility(), firebirdError.sqlState());
        }
        if (firebirdError.hasSymbolName()) {
            addSymbol(firebirdError.facility(), firebirdError.numberInFacility(), firebirdError.symbolName());
        }
    }

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

    final void store(Map<Integer, String> data, Path filePath) throws IOException {
        // REUSE-IgnoreStart
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, ISO_8859_1)) {
            writer.write("# SPDX-FileCopyrightText: 2000-");
            writer.write(Year.now().toString());
            writer.write(" Firebird development team and individual contributors");
            writer.newLine();
            writeln(writer, "# SPDX-License-Identifier: LGPL-2.1-or-later");
            writeln(writer, "# SPDX-FileComment: The keys and values listed here were obtained from the Firebird "
                    + "sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial "
                    + "Developer Public License), both are variants of the Mozilla Public License version 1.1");
            store(data, writer);
        }
        // REUSE-IgnoreEnd
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
        var outBuffer = new StringBuilder(len * 2);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
            case ' ' -> {
                if (x == 0 || escapeSpace) outBuffer.append('\\');

                outBuffer.append(' ');
            }
            case '\\' -> outBuffer.append('\\').append('\\');
            case '\t' -> outBuffer.append('\\').append('t');
            case '\n' -> outBuffer.append('\\').append('n');
            case '\r' -> outBuffer.append('\\').append('r');
            case '\f' -> outBuffer.append('\\').append('f');
            default -> {
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
