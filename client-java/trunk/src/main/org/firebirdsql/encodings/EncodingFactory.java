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

import java.io.*;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EncodingFactory {

    // TODO Refactor information on characters into a single resource file (eg XML) to ensure coherence of information

    private static final byte[] CHARSET_MAXIMUM_SIZE;
    /**
     * Highest id of the character sets in Firebird (default)
     */
    private static final int MAX_CHARSET_ID = 69;
    static {
        final byte[] maximumSize = new byte[MAX_CHARSET_ID + 1];
        // Fill with 1 as default
        Arrays.fill(maximumSize, (byte) 1);
        maximumSize[0] = 1; // NONE
        maximumSize[1] = 1; // OCTETS
        maximumSize[2] = 1; // ASCII
        maximumSize[3] = 3; // UNICODE_FSS
        maximumSize[4] = 4; // UTF8
        maximumSize[5] = 2; // SJIS_0208
        maximumSize[6] = 2; // EUJC_0208
        maximumSize[9] = 1; // DOS737
        maximumSize[10] = 1; // DOS437
        maximumSize[11] = 1; // DOS850
        maximumSize[12] = 1; // DOS865
        maximumSize[13] = 1; // DOS775
        maximumSize[14] = 1; // DOS863
        maximumSize[15] = 1; // DOS775
        maximumSize[16] = 1; // DOS858
        maximumSize[17] = 1; // DOS862
        maximumSize[18] = 1; // DOS864
        maximumSize[19] = 1; // NEXT
        maximumSize[21] = 1; // ISO8859_1
        maximumSize[22] = 1; // ISO8859_2
        maximumSize[23] = 1; // ISO8859_3
        maximumSize[34] = 1; // ISO8859_4
        maximumSize[35] = 1; // ISO8859_5
        maximumSize[36] = 1; // ISO8859_6
        maximumSize[37] = 1; // ISO8859_7
        maximumSize[38] = 1; // ISO8859_8
        maximumSize[39] = 1; // ISO8859_9
        maximumSize[40] = 1; // ISO8859_13
        maximumSize[44] = 2; // KSC_5601
        maximumSize[45] = 1; // DOS852
        maximumSize[46] = 1; // DOS857
        maximumSize[47] = 1; // DOS861
        maximumSize[48] = 1; // DOS866
        maximumSize[49] = 1; // DOS869
        maximumSize[50] = 1; // CYRL
        maximumSize[51] = 1; // WIN1250
        maximumSize[52] = 1; // WIN1251
        maximumSize[53] = 1; // WIN1252
        maximumSize[54] = 1; // WIN1253
        maximumSize[55] = 1; // WIN1254
        maximumSize[56] = 2; // BIG_5
        maximumSize[57] = 2; // GB2312
        maximumSize[58] = 1; // WIN1255
        maximumSize[59] = 1; // WIN1256
        maximumSize[60] = 1; // WIN1257
        maximumSize[63] = 1; // KOI8R
        maximumSize[64] = 1; // KOI8U
        maximumSize[65] = 1; // WIN1258
        maximumSize[66] = 1; // TIS620
        maximumSize[67] = 2; // GBK
        maximumSize[68] = 2; // CP943C
        maximumSize[69] = 4; // GB18030

        CHARSET_MAXIMUM_SIZE = maximumSize;
    };

    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();

    public static final String ISC_ENCODING_SIZE_RESOURCE = "isc_encoding_size.properties";
    public static final String ISC_ENCODINGS_RESOURCE = "isc_encodings.properties";

    private static final Map<String, Charset> ISC_ENCODINGS;
    private static final Map<String, Byte> ISC_ENCODING_SIZES;

    private static final Map<Charset, String> JAVA_ENCODINGS;

    private static final ConcurrentMap<String, CharacterTranslator> TRANSLATIONS = new ConcurrentHashMap<String, CharacterTranslator>();
    private static final ConcurrentMap<Charset, Encoding> STANDARD_ENCODINGS = new ConcurrentHashMap<Charset, Encoding>();

    /**
     * Default mapping table, provides an "identity" mapping.
     */
    public static final char[] DEFAULT_MAPPING;
    static {
        DEFAULT_MAPPING = new char[256 * 256];
        for (int i = 0; i < DEFAULT_MAPPING.length; i++) {
            DEFAULT_MAPPING[i] = (char) i;
        }

        ISC_ENCODING_SIZES = Collections.unmodifiableMap(loadEncodingSizes());
        // Sizes are just to prevent resizes during loading
        final Map<String, Charset> iscEncodingsTarget = new HashMap<String, Charset>(64);
        final Map<Charset, String> javaEncodingsTarget = new HashMap<Charset, String>(64);
        loadEncodings(iscEncodingsTarget, javaEncodingsTarget);
        ISC_ENCODINGS = Collections.unmodifiableMap(iscEncodingsTarget);
        JAVA_ENCODINGS = Collections.unmodifiableMap(javaEncodingsTarget);
    }

    /**
     * Maps the given java encoding name to its Charset.
     * 
     * @param encoding
     *            Name of the java encoding
     * @return Java Charset object
     */
    public static Charset charsetForEncoding(String encoding) {
        // TODO: How would we ever get NONE here?
        if (encoding == null || encoding.equals("NONE")) {
            return DEFAULT_ENCODING;
        } else {
            return Charset.forName(encoding);
        }
    }

    /**
     * Creates an {@link Encoding} instance for the supplied java Charset.
     * 
     * @param encoding
     *            Java Charset
     * @return Instance of {@link Encoding}
     */
    private static Encoding createEncoding(Charset charset) {
        if (charset == null)
            charset = DEFAULT_ENCODING;
        return findOrCreateStandardEncoding(charset);
    }

    /**
     * Retrieves the {@link Encoding} instance for the charset from cache, or
     * creates a new one.
     * 
     * @param charset
     *            Java Charset instance
     * @return Encoding instance
     */
    private static Encoding findOrCreateStandardEncoding(final Charset charset) {
        assert (charset != null) : "Parameter charset is required";
        Encoding standardEncoding = STANDARD_ENCODINGS.get(charset);
        if (standardEncoding != null) {
            return standardEncoding;
        }

        if (getJavaEncodingSize(charset) == 1) {
            standardEncoding = new Encoding_OneByte(charset);
        } else {
            standardEncoding = new Encoding_NotOneByte(charset);
        }

        STANDARD_ENCODINGS.putIfAbsent(charset, standardEncoding);
        return STANDARD_ENCODINGS.get(charset);
    }

    /**
     * Creates an {@link encoding} instance for the charset with the provided mapping.
     * 
     * @param charset
     *            Java Charset instance
     * @param charMapping
     *            Mapping of characters
     * @return Encoding instance
     */
    private static Encoding createMappedEncoding(Charset charset, char[] charMapping) {
        assert (charset != null) : "Parameter charset is required";
        if (getJavaEncodingSize(charset) == 1) {
            return new Encoding_OneByte(charset, charMapping);
        } else {
            return new Encoding_NotOneByte(charset, charMapping);
        }
    }

    /**
     * Get size of a character for the specified character set.
     * 
     * @param characterSetId
     *            of the character set.
     * @return maximum size of the character in bytes or 1 if charset was not
     *         found.
     */
    public static int getCharacterSetSize(int characterSetId) {
        if (characterSetId >= 0 && characterSetId < CHARSET_MAXIMUM_SIZE.length) {
            return CHARSET_MAXIMUM_SIZE[characterSetId];
        }
        // let's assume that default length is 1
        return 1;
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset name.
     * 
     * @param encoding
     *            Java Charset name
     * @return Instance of {@link Encoding}
     */
    public static Encoding getEncoding(String encoding) {
        Charset charset = charsetForEncoding(encoding);
        return getEncoding(charset);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset.
     * 
     * @param encoding
     *            Java Charset
     * @return Instance of {@link Encoding}
     */
    public static Encoding getEncoding(Charset charset) {
        return createEncoding(charset);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset and
     * alternative character mapping.
     * 
     * @param charset
     *            Java Charset
     * @param charMapping
     *            Alternative character mapping
     * @return Instance of {@link Encoding}
     */
    public static Encoding getEncoding(Charset charset, char[] charMapping) {
        if (charset == null) {
            charset = DEFAULT_ENCODING;
        }
        return createMappedEncoding(charset, charMapping);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset name and
     * alternative character mapping.
     * 
     * @param encoding
     *            Java Charset name
     * @param mappingPath
     *            Resource file with alternative character mapping
     * @return Instance of {@link Encoding}
     */
    public static Encoding getEncoding(String encoding, String mappingPath) throws SQLException {
        Charset charset = charsetForEncoding(encoding);
        return getEncoding(charset, mappingPath);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset and
     * alternative character mapping.
     * 
     * @param charset
     *            Java Charset
     * @param mappingPath
     *            Resource file with alternative character mapping
     * @return Instance of {@link Encoding}
     */
    public static Encoding getEncoding(Charset charset, String mappingPath) throws SQLException {
        if (mappingPath == null) {
            return getEncoding(charset);
        }
        CharacterTranslator translator = getTranslator(mappingPath);
        return getEncoding(charset, translator.getMapping());
    }

    /**
     * Get Firebird encoding for given Java language encoding.
     * 
     * @param javaEncoding
     *            Java language encoding.
     * @return corresponding Firebird encoding or <code>null</code> if none
     *         found.
     */
    public static String getIscEncoding(String javaEncoding) {
        return getIscEncoding(Charset.forName(javaEncoding));
    }

    /**
     * Get Firebird encoding for given Java Charset.
     * 
     * @param javaEncoding
     *            Java Charset
     * @return corresponding Firebird encoding or <code>null</code> if none
     *         found.
     */
    public static String getIscEncoding(Charset javaCharset) {
        return JAVA_ENCODINGS.get(javaCharset);
    }

    /**
     * Get size of a character for the specified Firebird encoding.
     * 
     * @param iscEncoding
     *            Firebird encoding.
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     */
    public static int getIscEncodingSize(String iscEncoding) {
        if (iscEncoding == null) {
            return 1;
        }

        Byte result = ISC_ENCODING_SIZES.get(iscEncoding.toLowerCase());
        if (result == null)
            return 1;
        else
            return result.byteValue();
    }

    /**
     * Get Java language encoding for given Firebird encoding.
     * 
     * @param iscEncoding
     *            Firebird encoding
     * @return corresponding Java encoding or <code>null</code> if none found.
     */
    public static String getJavaEncoding(String iscEncoding) {
        if (iscEncoding == null) {
            return null;
        }

        // very important for performance
        // if javaEncoding is the default one, set to null
        Charset javaCharset = ISC_ENCODINGS.get(iscEncoding.toLowerCase());
        if (javaCharset == null || javaCharset.equals(DEFAULT_ENCODING))
            return null;
        else
            return javaCharset.name();
    }

    /**
     * Get size of a character for the specified Java encoding, as it would be
     * encoded in Firebird.
     * 
     * @param javaCharset
     *            Java Charset
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     */
    public static int getJavaEncodingSize(Charset javaCharset) {
        return getIscEncodingSize(getIscEncoding(javaCharset));
    }

    /**
     * Get size of a character for the specified Java encoding, as it would be
     * encoded in Firebird.
     * 
     * @param javaEncoding
     *            Java Charset name
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     */
    public static int getJavaEncodingSize(String javaEncoding) {
        return getJavaEncodingSize(charsetForEncoding(javaEncoding));
    }

    /**
     * Get Java language encoding for a given Java encoding alias.
     * <p>
     * Ensures that naming is consistent even if a different alias was used.
     * </p>
     * 
     * @param javaAlias
     *            Java alias for the encoding
     * @return
     */
    public static String getJavaEncodingForAlias(String javaAlias) {
        Charset charset = charsetForEncoding(javaAlias);
        // very important for performance
        // if javaEncoding is the default one, set to null
        if (charset == null || charset.equals(DEFAULT_ENCODING)) {
            return null;
        } else {
            return charset.name();
        }
    }

    public static CharacterTranslator getTranslator(String mappingPath) throws SQLException {
        CharacterTranslator translator = TRANSLATIONS.get(mappingPath);
        if (translator != null) {
            return translator;
        }

        translator = new CharacterTranslator();
        translator.init(mappingPath);
        TRANSLATIONS.putIfAbsent(mappingPath, translator);

        return TRANSLATIONS.get(mappingPath);
    }

    /**
     * Load mapping between Java and Firebird encodings. This method loads the
     * mapping using the classloader that loaded this class.
     * 
     * @param iscEncodingsTarget Map instance for the iscEncodings to Charset mapping
     * @param javaEncodingsTarget Map instance for the Charset to iscEncodings mapping
     */
    private static void loadEncodings(Map<String, Charset> iscEncodingsTarget, Map<Charset, String> javaEncodingsTarget) {
        Properties props;
        try {
            props = loadProperties(ISC_ENCODINGS_RESOURCE);
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return;
        }

        // fill the direct and inversed mappings
        for (Entry<Object, Object> entry : props.entrySet()) {
            String iscEncoding = (String) entry.getKey();
            String javaEncoding = (String) entry.getValue();
            Charset javaCharset = Charset.forName(javaEncoding);

            iscEncodingsTarget.put(iscEncoding.toLowerCase(), javaCharset);

            // special handling for UTF8 and UNICODE_FSS encodings
            // since UTF8 is an alias for UNICODE_FSS in Firebird 1.x
            // it is safe to return UTF8 for all cases
            if ("UNICODE_FSS".equals(iscEncoding))
                continue;

            // TODO: Remove iscEncoding if javaCharset is null?

            javaEncodingsTarget.put(javaCharset, iscEncoding);
        }
    }

    /**
     * Load mapping between Java and Firebird encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private static Map<String, Byte> loadEncodingSizes() {
        // Size is just to prevent resizes during loading
        Map<String, Byte> encodingSizes = new HashMap<String, Byte>(64);
        try {
            Properties props = loadProperties(ISC_ENCODING_SIZE_RESOURCE);
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String iscEncoding = (String) entry.getKey();
                String size = (String) entry.getValue();
                byte byteSize = Byte.parseByte(size);
                encodingSizes.put(iscEncoding.toLowerCase(), Byte.valueOf(byteSize));
            }
        } catch (IOException ex) {
            // TODO: Log instead?
            ex.printStackTrace();
        }
        return encodingSizes;
    }

    /**
     * Load properties from the specified resource. This method uses the same
     * class loader that loaded this class.
     * 
     * @param resource
     *            path to the resource relative to the root of the classloader.
     * 
     * @return instance of {@link Properties} containing loaded resources or
     *         <code>null</code> if resource was not found.
     * 
     * @throws IOException
     *             if I/O error occured.
     */
    private static Properties loadProperties(String resource) throws IOException {
        final ClassLoader cl = EncodingFactory.class.getClassLoader();
        final InputStream in;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(resource);
        else
            in = cl.getResourceAsStream(resource);

        if (in == null)
            return null;

        try {
            Properties props = new Properties();
            props.load(in);
            return props;
        } finally {
            in.close();
        }
    }
}
