/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.util;

import java.io.*;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for generating the property files containing the errorcodes and error messages.
 */
public class MessageDump {

    private static final int ISC_CODE = 0x14000000;

    private static Connection getConnection(String database) throws Exception {
        Class.forName("org.firebirdsql.jdbc.FBDriver");
        String url = "jdbc:firebirdsql:" + database;
        return DriverManager.getConnection(url, "SYSDBA", "masterkey");
    }

    private static int getErrorCode(int code, int number) {
        return ISC_CODE | ((code & 0x1F) << 16) | (number & 0x3FFF);
    }

    private static String extractMessage(String fbMessage) {
        char[] chars = fbMessage.toCharArray();

        StringBuilder sb = new StringBuilder();
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
                    } else
                        sb.append("%ld");
                } else
                    sb.append('%').append(chars[i]);
            } else if (chars[i] == '@') {
                i++;

                try {
                    // Currently assumes parameter-number not to exceed 9.
                    int msgNum = Integer.parseInt("" + chars[i]);
                    sb.append('{').append(Integer.toString(msgNum - 1)).append('}');
                } catch (NumberFormatException ex) {
                    sb.append(chars[i]);
                }
            } else
                sb.append(chars[i]);
        }

        return sb.toString();
    }

    private static Map<Integer, String> extractErrorMessages(Connection connection) throws Exception {
        Map<Integer, String> result = new TreeMap<>();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt
                    .executeQuery("SELECT fac_code, number, text FROM messages");

            while (rs.next()) {
                int code = rs.getInt(1);
                int number = rs.getInt(2);
                String message = rs.getString(3);

                result.put(
                        getErrorCode(code, number),
                        extractMessage(message));
            }
        }
        return result;
    }

    private static Map<Integer, String> extractSQLStates(Connection connection) throws SQLException {
        Map<Integer, String> result = new TreeMap<>();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt
                    .executeQuery("SELECT fac_code, number, sql_state FROM system_errors");

            while (rs.next()) {
                int code = rs.getInt(1);
                int number = rs.getInt(2);
                String sqlState = rs.getString(3);

                result.put(
                        getErrorCode(code, number),
                        extractMessage(sqlState));
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[] { "localhost:d:/data/db/fb4/msg.fdb" };
        }

        try (Connection connection = getConnection(args[0])) {
            System.out.println("Retrieving error messages");
            final Map<Integer, String> errorMessages = extractErrorMessages(connection);
            try (FileOutputStream errorStream = new FileOutputStream("./error.properties")) {
                store(errorMessages, errorStream, null);
            }

            System.out.println("Retrieving SQL State values");
            final Map<Integer, String> sqlStates = extractSQLStates(connection);
            try (FileOutputStream sqlstateStream = new FileOutputStream("./sqlstates.properties")) {
                store(sqlStates, sqlstateStream, null);
            }
        }
    }

    public static void store(Map<Integer, String> map, OutputStream out, String header)
            throws IOException {
        BufferedWriter awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header != null) writeln(awriter, "#" + header);
        writeln(awriter, "#" + new java.util.Date().toString());
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            String key = saveConvert(Integer.toString(entry.getKey()), true);

            /*
             * No need to escape embedded and trailing spaces for value,
             * hence pass false to flag.
             */
            String val = saveConvert(entry.getValue(), false);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
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
                    outBuffer.append(toHex((aChar >> 12) & 0xF));
                    outBuffer.append(toHex((aChar >> 8) & 0xF));
                    outBuffer.append(toHex((aChar >> 4) & 0xF));
                    outBuffer.append(toHex(aChar & 0xF));
                } else {
                    if (specialSaveChars.indexOf(aChar) != -1)
                        outBuffer.append('\\');
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
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final String specialSaveChars = "=: \t\r\n\f#!";

}
