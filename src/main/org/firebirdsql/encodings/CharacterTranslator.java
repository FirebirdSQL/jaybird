/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.encodings;

import java.util.*;

import org.firebirdsql.jdbc.FBSQLException;

/**
 * Class responsible for character translation.
 */
public class CharacterTranslator {

    private char[] mapping = new char[256 * 256]; // cover all unicode chars
    
    public CharacterTranslator() {
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = (char)i;
        }
    }

    /**
     * Get mapping table.
     * 
     * @return mapping table.
     */
    public char[] getMapping() {
        return mapping;
    }
    
    /**
     * Get mapping for the specified character.
     * 
     * @param toMap character to map.
     * 
     * @return mapped character.
     */
    public char getMapping(char toMap) {
        return mapping[toMap];
    }
    
    /**
     * Initialize this class with the specified mapping.
     * 
     * @param mappingPath path to the .properties file with the corresponding 
     * mapping.
     * 
     * @throws FBSQLException if I/O error occured or specified mapping is 
     * incorrect or cannot be found.
     */
    public void init(String mappingPath) throws FBSQLException {
        
        Properties props = new Properties();

        try {
            ResourceBundle res = ResourceBundle.getBundle(
                mappingPath, Locale.getDefault(), getClass().getClassLoader());
                
            Enumeration en = res.getKeys();
            while(en.hasMoreElements()) {
                String key = (String)en.nextElement();
                String value = res.getString(key);
                props.put(key, value);
            }
    
            
            for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                
                if (!key.startsWith("db."))
                    throw new FBSQLException("Incorrect mapping format. " +
                            "All properties should start with \"db.\", but " + 
                            key + " found.");
                
                if (key.length() != 4)
                    throw new FBSQLException("Incorrect mapping format. " +
                            "Key should consist only of 4 characters, but " + 
                            key + " found.");
                
                if (value.length() != 1)
                    throw new FBSQLException("Incorrect mapping format. " + 
                        "Mapped value should consist only of single character, but " + 
                        value + " found.");
                
                char dbChar = key.charAt(3);
                char javaChar = value.charAt(0);
                
                mapping[dbChar] = javaChar;
                mapping[javaChar] = dbChar;
            }
        } catch(MissingResourceException ex) {
            throw new FBSQLException("Character translation " + mappingPath + 
                " could not be found.");
        }
    }
}
