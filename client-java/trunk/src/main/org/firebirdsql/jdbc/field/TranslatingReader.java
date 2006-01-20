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

package org.firebirdsql.jdbc.field;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.jdbc.FBSQLException;


/**
 * Reader that is capable translating characters using the specified mapping.
 * Should be used together with the <code>isc_dpb_mapping_path</code> parameter. 
 * If no mapping path is specified, it behaves as normal {@link java.io.InputStreamReader}.
 */
class TranslatingReader extends InputStreamReader {

    /**
     * Create instance of this class.
     * 
     * @param in input stream from which characters are read.
     * @param charsetName Java character set.
     * @param mappingPath path to the character mapping.
     * 
     * @return instance of {@link TranslatingReader}
     * 
     * @throws SQLException if the specified mapping path is not found.
     */
    static TranslatingReader getInstance(InputStream in, String charsetName,
            String mappingPath) throws SQLException {
        
        try {
            if (charsetName != null)
                return new TranslatingReader(in, charsetName, mappingPath);
            else
                return new TranslatingReader(in, mappingPath);
            
        } catch(UnsupportedEncodingException ex) {
            throw new FBSQLException("Cannot set character stream because " +
                "the unsupported encoding is detected in the JVM: " +
                charsetName + ". Please report this to the driver developers."
            );
        }
    }
    
    private char[] charMap;

    /**
     * Create instance of this class.
     * 
     * @param in input stream from which characters are read.
     * @param charsetName Java character set.
     * @param mappingPath path to the character mapping.
     * 
     * @throws java.io.UnsupportedEncodingException if the specified charset
     * is not known.
     * 
     * @throws SQLException if the specified mapping path is not found.
     */
    private TranslatingReader(InputStream in, String charsetName, String mappingPath)
            throws UnsupportedEncodingException, SQLException {
        super(in, charsetName);
        
        if (mappingPath != null)
            charMap = EncodingFactory.getTranslator(mappingPath).getMapping();
        else
            charMap = null;
    }
    
    /**
     * Create instance of this class.
     * 
     * @param in input stream from which characters are read.
     * @param mappingPath path to the character mapping.
     * 
     * @throws java.io.UnsupportedEncodingException if the specified charset
     * is not known.
     * 
     * @throws SQLException if the specified mapping path is not found.
     */
    private TranslatingReader(InputStream in, String mappingPath)
            throws UnsupportedEncodingException, SQLException {
        super(in);
        
        if (mappingPath != null)
            charMap = EncodingFactory.getTranslator(mappingPath).getMapping();
        else
            charMap = null;
    }

    public int read() throws IOException {
        if (charMap == null)
            return super.read();
        else
            return charMap[super.read()];
    }

    public int read(char[] cbuf, int offset, int length) throws IOException {
        int result = super.read(cbuf, offset, length);
        
        if (charMap != null) {
            for (int i = 0; i < cbuf.length; i++) {
                cbuf[i] = charMap[cbuf[i]];
            }
        }
        
        return result;
    }
}
