package org.firebirdsql.encodings;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * Class responsible for character translation.
 */
public class CharacterTranslator {

    private String mappingPath;
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
     * @throws IOException if I/O error occured or specified mapping is 
     * incorrect or cannot be found.
     */
    public void init(String mappingPath) throws SQLException {
        
        this.mappingPath = mappingPath;
        Properties props = new Properties();

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
                throw new SQLException("Incorrect mapping format. " +
                        "All properties should start with \"db.\", but " + 
                        key + " found.");
            
            if (key.length() != 4)
                throw new SQLException("Incorrect mapping format. " +
                        "Key should consist only of 4 characters, but " + 
                        key + " found.");
            
            if (value.length() != 1)
                throw new SQLException("Incorrect mapping format. " + 
                    "Mapped value should consist only of single character, but " + 
                    value + " found.");
            
            char dbChar = key.charAt(3);
            char javaChar = value.charAt(0);
            
            mapping[dbChar] = javaChar;
        }
    }
    
    private InputStream getMappingStream(String mappingPath) throws IOException {
        
        ClassLoader cl = getClass().getClassLoader();
        if (cl == null)
            cl = ClassLoader.getSystemClassLoader();
        
        InputStream in = cl.getResourceAsStream(mappingPath);
        
        if (in == null) {
            cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream(mappingPath);
        }
        
        if (in == null && !mappingPath.startsWith("/"))
            in = getMappingStream("/" + mappingPath);

        return in;
    }
}
