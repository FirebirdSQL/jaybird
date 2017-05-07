/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.encodings;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.*;

/**
 * Class responsible for character translation.
 */
public final class CharacterTranslator {

    /**
     * Default mapping table, provides an "identity" mapping of characters
     */
    private static final char[] DEFAULT_MAPPING;
    private static final int FULL_CHAR_RANGE = 256 * 256;

    /**
     * CharacterTranslator with the identity mapping established by {@link #DEFAULT_MAPPING};
     */
    public static final CharacterTranslator IDENTITY_TRANSLATOR;

    static {
        DEFAULT_MAPPING = new char[FULL_CHAR_RANGE];
        for (int i = 0; i < DEFAULT_MAPPING.length; i++) {
            DEFAULT_MAPPING[i] = (char) i;
        }
        IDENTITY_TRANSLATOR = new CharacterTranslator(DEFAULT_MAPPING);
    }

    private final char[] mapping;

    private CharacterTranslator(char[] mapping) {
        assert mapping.length == FULL_CHAR_RANGE : "Invalid length for mapping table"; // need to cover all possible char values
        this.mapping = mapping;
    }

    /**
     * Get mapping for the specified character.
     *
     * @param toMap
     *         character to map.
     * @return mapped character.
     */
    public char getMapping(char toMap) {
        return mapping[toMap];
    }

    /**
     * Initialize this class with the specified mapping.
     *
     * @param mappingPath
     *         path to the .properties file with the corresponding mapping.
     * @throws SQLException
     *         if I/O error occurred or specified mapping is incorrect or cannot be found.
     */
    public static CharacterTranslator create(String mappingPath) throws SQLException {
        Properties props = new Properties();

        try {
            ResourceBundle res = ResourceBundle.getBundle(
                    mappingPath, Locale.getDefault(), CharacterTranslator.class.getClassLoader());

            Enumeration<String> en = res.getKeys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                String value = res.getString(key);
                props.put(key, value);
            }

            final char[] mapping = DEFAULT_MAPPING.clone();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (!key.startsWith("db."))
                    throw new SQLNonTransientException("Incorrect mapping format. " +
                            "All properties should start with \"db.\", but " + key + " found.");

                if (key.length() != 4)
                    throw new SQLNonTransientException("Incorrect mapping format. " +
                            "Key should consist only of 4 characters, but " + key + " found.");

                if (value.length() != 1)
                    throw new SQLNonTransientException("Incorrect mapping format. " +
                            "Mapped value should consist only of single character, but " + value + " found.");

                char dbChar = key.charAt(3);
                char javaChar = value.charAt(0);

                mapping[dbChar] = javaChar;
                mapping[javaChar] = dbChar;
            }
            return new CharacterTranslator(mapping);
        } catch (MissingResourceException ex) {
            throw new SQLNonTransientException("Character translation " + mappingPath + " could not be found.");
        }
    }
}
