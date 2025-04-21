// SPDX-FileCopyrightText: Copyright 2004-2008 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

/**
 * Helper class for extracting and transforming messages as defined in Firebird.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class MessageConverter {

    private MessageConverter() {
        // no instances
    }

    @SuppressWarnings("java:S127")
    static String toJaybirdMessageFormat(String fbMessage) {
        char[] chars = fbMessage.toCharArray();

        StringBuilder sb = new StringBuilder(fbMessage.length() + 10);
        int counter = 0;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                i++;

                switch (chars[i]) {
                case 's', 'd' -> sb.append('{').append(counter++).append('}');
                case 'l' -> {
                    i++;
                    if (chars[i] == 'd') {
                        sb.append('{').append(counter++).append('}');
                    } else {
                        sb.append("%l").append(chars[i]);
                    }
                }
                default -> sb.append('%').append(chars[i]);
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

    static String toNetProviderMessageFormat(String fbMessage) {
        // Format is the same as Jaybird's
        return toJaybirdMessageFormat(fbMessage);
    }

    static String toSqlState(String sqlStateClass, String sqlStateSubClass) {
        if (sqlStateClass.length() != 2) {
            throw new IllegalArgumentException("SQLSTATE class must be 2 characters, was: '" + sqlStateClass + "'");
        }
        if (sqlStateSubClass.length() != 3) {
            throw new IllegalArgumentException(
                    "SQLSTATE subclass must be 3 characters, was: '" + sqlStateSubClass + "'");
        }
        return (sqlStateClass + sqlStateSubClass).intern();
    }

    /**
     * Unescapes the Firebird 5.0+ {@code .h} message strings.
     *
     * @param message
     *         message to unescape
     * @return unescaped message
     */
    @SuppressWarnings("java:S127")
    static String unescapeSource(String message) {
        int length = message.length();
        var sb = new StringBuilder(length);
        for (int x = 0; x < length; x++) {
            char ch = message.charAt(x);
            if (ch == '\\' && x + 1 < length) {
                // Unconditionally increment position, we restore this in the default of the switch
                char nextCh = message.charAt(++x);
                switch (nextCh) {
                case '\\', '"' -> sb.append(nextCh);
                case 't' -> sb.append('\t');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 'f' -> sb.append('\f');
                default -> {
                    // Restore position
                    x--;
                    sb.append(ch);
                }
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
