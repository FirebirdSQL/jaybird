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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for generating the property files containing the error codes and error messages from MSG.FDB.
 * <p>
 * This tool only works for Firebird 4.0 and earlier. For Firebird 5.0 and higher, use {@link MessageExtractor}.
 * </p>
 * 
 * @deprecated Use {@link MessageExtractor}
 */
@Deprecated
public class MessageDump {

    private static final String FORMAT_OPTION_PREFIX = "--format=";

    private static Connection getConnection(String database) throws Exception {
        String url = "jdbc:firebirdsql:" + database;
        return DriverManager.getConnection(url, "SYSDBA", "masterkey");
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

    @FunctionalInterface
    private interface MessageConsumer {
        void accept(int code, int number, String data);
    }
}
