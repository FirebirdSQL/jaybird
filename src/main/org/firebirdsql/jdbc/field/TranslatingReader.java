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
package org.firebirdsql.jdbc.field;

import java.io.*;
import java.sql.SQLException;

import org.firebirdsql.encodings.CharacterTranslator;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Reader that is capable translating characters using the specified mapping.
 * Should be used together with the <code>isc_dpb_mapping_path</code> parameter. 
 * If no mapping path is specified, it behaves as normal {@link java.io.InputStreamReader}.
 */
final class TranslatingReader extends InputStreamReader {

    /**
     * Return a reader instance for the input stream, character set and mapping path.
     * <p>
     * This method does not necessarily return a {@link TranslatingReader}.
     * </p>
     * 
     * @param in input stream from which characters are read.
     * @param charsetName Java character set.
     * @param mappingPath path to the character mapping.
     * 
     * @return instance of {@link TranslatingReader}
     * 
     * @throws SQLException if the specified mapping path is not found.
     */
    static Reader getInstance(InputStream in, String charsetName, String mappingPath) throws SQLException {
        final CharacterTranslator mapping = EncodingFactory.getTranslator(mappingPath);
        try {
            if (charsetName != null) {
                if (mapping != null) {
                    return new TranslatingReader(in, charsetName, mapping);
                }
                return new InputStreamReader(in, charsetName);
            } else {
                if (mapping != null) {
                    return new TranslatingReader(in, mapping);
                }
                return new InputStreamReader(in);
            }
        } catch(UnsupportedEncodingException ex) {
            throw new FBSQLException("Cannot set character stream because " +
                "the unsupported encoding is detected in the JVM: " +
                charsetName + ". Please report this to the driver developers."
            );
        }
    }
    
    private final CharacterTranslator mapping;

    private TranslatingReader(InputStream in, String charsetName, CharacterTranslator mapping)
            throws UnsupportedEncodingException, SQLException {
        super(in, charsetName);
        assert mapping != null : "mapping is required";
        this.mapping = mapping;
    }
    
    private TranslatingReader(InputStream in, CharacterTranslator mapping)
            throws UnsupportedEncodingException, SQLException {
        super(in);
        assert mapping != null : "mapping is required";
        this.mapping = mapping;
    }

    @Override
    public int read() throws IOException {
        final int valueRead = super.read();
        if (valueRead == -1) {
            return -1;
        }
        return mapping.getMapping((char) super.read());
    }

    @Override
    public int read(final char[] cbuf, final int offset, final int length) throws IOException {
        int result = super.read(cbuf, offset, length);
        if (result == -1) {
            return -1;
        }

        for (int i = offset; i < offset + result; i++) {
            cbuf[i] = mapping.getMapping(cbuf[i]);
        }

        return result;
    }
}
