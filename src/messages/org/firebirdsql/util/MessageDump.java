package org.firebirdsql.util;

import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;

/**
 * 
 */
public class MessageDump {

    private static final int ISC_CODE = 0x14000000;

    private static FirebirdConnection getConnection(String database)
            throws Exception {
        Class.forName(FBDriver.class.getName());
        String url = "jdbc:firebirdsql:" + database;
        return (FirebirdConnection) DriverManager.getConnection(url, "SYSDBA",
            "masterkey");
    }

    private static int getErrorCode(int code, int number) {
        return ISC_CODE | ((code & 0x1F) << 16) | (number & 0x3FFF);
    }

    private static String extractMessage(String fbMessage) {
        char[] chars = fbMessage.toCharArray();

        StringBuffer sb = new StringBuffer();
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
            } else
                sb.append(chars[i]);
        }

        return sb.toString();
    }

    private static Properties extractProperties(FirebirdConnection connection)
            throws Exception {
        Properties result = new Properties();

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt
                    .executeQuery("SELECT fac_code, number, text FROM messages");

            while (rs.next()) {
                int code = rs.getInt(1);
                int number = rs.getInt(2);
                String message = rs.getString(3);

                result.setProperty(
                    Integer.toString(getErrorCode(code, number)),
                    extractMessage(message));
            }

        } finally {
            stmt.close();
        }

        return result;
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0)
            args = new String[] { "localhost:c:/database/messages.fdb"};

        FirebirdConnection connection = getConnection(args[0]);
        try {
            Properties props = extractProperties(connection);
            TreeMap sortedMap = new TreeMap(props);

            store(sortedMap, new FileOutputStream("./error.properties"), "");
        } finally {
            connection.close();
        }
    }

    public static void store(Map map, OutputStream out, String header)
            throws IOException {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header != null) writeln(awriter, "#" + header);
        writeln(awriter, "#" + new Date().toString());
        synchronized (map) {
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = saveConvert((String)entry.getKey(), true);

                /*
                 * No need to escape embedded and trailing spaces for value,
                 * hence pass false to flag.
                 */
                String val = saveConvert((String)entry.getValue(), false);
                writeln(awriter, key + "=" + val);
            }
        }
        awriter.flush();
    }

    private static String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len * 2);

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

    /** A table of hex digits */
    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String specialSaveChars = "=: \t\r\n\f#!";

}
