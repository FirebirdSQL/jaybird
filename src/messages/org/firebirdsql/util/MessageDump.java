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
package org.firebirdsql.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Utility class for generating the property files containing the error codes and error messages.
 */
public class MessageDump {

    private static final int ISC_CODE = 0x14000000;
    private static final String FORMAT_OPTION_PREFIX = "--format=";
    private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

    private static Connection getConnection(String database) throws Exception {
        String url = "jdbc:firebirdsql:" + database;
        return DriverManager.getConnection(url, "SYSDBA", "masterkey");
    }

    private static int getErrorCode(int code, int number) {
        return ISC_CODE | ((code & 0x1F) << 16) | (number & 0x3FFF);
    }

    private static String extractMessage(String fbMessage) {
        char[] chars = fbMessage.toCharArray();

        StringBuilder sb = new StringBuilder(fbMessage.length() + 10);
        int counter = 0;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                i++;

                if (chars[i] == 's') {
                    sb.append('{').append(counter++).append('}');
                } else if (chars[i] == 'd') {
                    sb.append('{').append(counter++).append('}');
                } else if (chars[i] == 'l') {
                    i++;
                    if (chars[i] == 'd') {
                        sb.append('{').append(counter++).append('}');
                    } else {
                        sb.append("%l").append(chars[i]);
                    }
                } else {
                    sb.append('%').append(chars[i]);
                }
            } else if (chars[i] == '@') {
                i++;

                try {
                    // assumes parameter-number not to exceed 9.
                    int msgNum = Integer.parseInt("" + chars[i]);
                    sb.append('{').append(msgNum - 1).append('}');
                } catch (NumberFormatException ex) {
                    sb.append(chars[i]);
                }
            } else {
                sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    private static void extractErrorMessages(Statement stmt, MessageStore messageStore) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT fac_code, number, trim(trailing from text) FROM messages ORDER BY fac_code, number")) {
            extractData(rs, messageStore::addMessage);
        }
    }

    private static void extractSQLStates(Statement stmt, MessageStore messageStore) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT fac_code, number, trim(sql_state) FROM system_errors ORDER BY fac_code, number")) {
            extractData(rs, messageStore::addSqlState);
        }
    }

    private static void extractData(ResultSet rs, MessageConsumer messageConsumer) throws SQLException {
        while (rs.next()) {
            int code = rs.getInt(1);
            int number = rs.getInt(2);
            String data = rs.getString(3);

            messageConsumer.accept(code, number, data);
        }
    }

    public static void main(String[] args) throws Exception {
        String dbUrl;
        OutputFormat outputFormat = OutputFormat.SINGLE;
        if (args.length == 0) {
            dbUrl = "localhost:d:/data/db/fb4/msg.fdb";
        } else {
            dbUrl = args[0];
            for (int idx = 1; idx < args.length; idx++) {
                if (args[idx].startsWith(FORMAT_OPTION_PREFIX)) {
                    String optionValue = args[idx].substring(FORMAT_OPTION_PREFIX.length());
                    try {
                        outputFormat = OutputFormat.valueOf(optionValue);
                    } catch (IllegalArgumentException e) {
                        System.err.printf("Unsupported value for option %s: %s%n", FORMAT_OPTION_PREFIX, optionValue);
                        System.exit(-1);
                    }
                }
            }
        }
        System.out.println("Using output format " + outputFormat);
        MessageStore messageStore = outputFormat.createMessageStore();

        try (Connection connection = getConnection(dbUrl);
             Statement statement = connection.createStatement()) {
            System.out.println("Retrieving error messages");
            extractErrorMessages(statement, messageStore);

            System.out.println("Retrieving SQL State values");
            extractSQLStates(statement, messageStore);
        }
        System.out.println("Saving");
        messageStore.save();
    }

    private static void store(Map<Integer, String> data, Path filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, ISO_8859_1)) {
            MessageDump.store(data, writer);
        }
    }

    private static void store(Map<Integer, String> map, BufferedWriter writer) throws IOException {
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
                outBuffer.append('\\');
                outBuffer.append('\\');
                break;
            case '\t':
                outBuffer.append('\\');
                outBuffer.append('t');
                break;
            case '\n':
                outBuffer.append('\\');
                outBuffer.append('n');
                break;
            case '\r':
                outBuffer.append('\\');
                outBuffer.append('r');
                break;
            case '\f':
                outBuffer.append('\\');
                outBuffer.append('f');
                break;
            default:
                if ((aChar < 0x0020) || (aChar > 0x007e)) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex(aChar >> 12));
                    outBuffer.append(toHex(aChar >> 8));
                    outBuffer.append(toHex(aChar >> 4));
                    outBuffer.append(toHex(aChar));
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

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xF];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private interface MessageStore {

        void addMessage(int code, int number, String message);

        void addSqlState(int code, int number, String sqlState);

        void save() throws IOException;

    }

    private enum OutputFormat {
        SINGLE(SingleFileStore::new),
        PER_FACILITY(PerFacilityStore::new),
        ;

        private final Supplier<MessageStore> messageStoreSupplier;

        OutputFormat(Supplier<MessageStore> messageStoreSupplier) {
            this.messageStoreSupplier = messageStoreSupplier;
        }

        MessageStore createMessageStore() {
            return messageStoreSupplier.get();
        }
    }

    /**
     * Uses a single file for error messages, and a single file for SQLstates.
     * <p>
     * This is the format used by Jaybird.
     * </p>
     */
    private static class SingleFileStore implements MessageStore {

        private final Map<Integer, String> messages = new TreeMap<>();
        private final Map<Integer, String> sqlStates = new TreeMap<>();

        @Override
        public void addMessage(int code, int number, String message) {
            messages.put(getErrorCode(code, number), extractMessage(message));
        }

        @Override
        public void addSqlState(int code, int number, String sqlState) {
            sqlStates.put(getErrorCode(code, number), sqlState);
        }

        @Override
        public void save() throws IOException {
            store(messages, Path.of("isc_error_msg.properties"));
            store(sqlStates, Path.of("isc_error_sqlstates.properties"));
        }
    }

    /**
     * Uses a file for error messages and a file for SQLstates per facility.
     * <p>
     * This format is currently not used, but we might use this for Jaybird in the future.
     * </p>
     */
    private static class PerFacilityStore implements MessageStore {

        private static final int MAX_FACILITY = 25; // NOTE: This is Firebird max, Jaybird has facility 26
        static final int FACILITY_SIZE = MAX_FACILITY + 1;

        private final Map<Integer, Map<Integer, String>> messagesPerFacility = new HashMap<>(FACILITY_SIZE, 1);
        private final Map<Integer, Map<Integer, String>> sqlStatesPerFacility = new HashMap<>(FACILITY_SIZE, 1);

        @Override
        public void addMessage(int code, int number, String message) {
            add(messagesPerFacility, code, number, extractMessage(message));
        }

        @Override
        public void addSqlState(int code, int number, String sqlState) {
            add(sqlStatesPerFacility, code, number, sqlState);
        }

        private static void add(Map<Integer, Map<Integer, String>> facilityMap, int code, int number, String data) {
            Map<Integer, String> facilityData = facilityMap.computeIfAbsent(code, k -> new TreeMap<>());
            facilityData.put(getErrorCode(code, number), data);
        }

        @Override
        public void save() throws IOException {
            save(messagesPerFacility, code -> format("error_messages_%d.properties", code));
            save(sqlStatesPerFacility, code -> format("sql_states_%d.properties", code));
        }

        private static void save(Map<Integer, Map<Integer, String>> facilityMap,
                Function<Integer, String> filenameGenerator) throws IOException {
            for (Map.Entry<Integer, Map<Integer, String>> facilityEntry : facilityMap.entrySet()) {
                Map<Integer, String> facilityData = facilityEntry.getValue();
                if (facilityData.isEmpty()) {
                    continue;
                }
                String fileName = filenameGenerator.apply(facilityEntry.getKey());
                store(facilityData, Path.of(fileName));
            }
        }
    }

    @FunctionalInterface
    private interface MessageConsumer {
        void accept(int code, int number, String data);
    }
}
