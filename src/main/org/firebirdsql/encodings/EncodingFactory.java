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
import java.sql.SQLException;
import java.util.*;

public class EncodingFactory {
    
    private static final int[][] CHARSET_MAXIMUM_SIZE = new int[][] {
        { 0, 1}   // NONE
      , { 1, 1}   // OCTETS
      , { 2, 1}   // ASCII
      , { 3, 3}   // UNICODE_FSS
      , { 4, 4}   // UTF8 
      , { 5, 2}   // SJIS_0208
      , { 6, 2}   // EUJC_0208
      , { 9, 1}   // DOS737
      , {10, 1}   // DOS437
      , {11, 1}   // DOS850
      , {12, 1}   // DOS865
      , {13, 1}   // DOS775
      , {14, 1}   // DOS863
      , {15, 1}   // DOS775
      , {16, 1}   // DOS858
      , {17, 1}   // DOS862
      , {18, 1}   // DOS864
      , {19, 1}   // NEXT
      , {21, 1}   // ISO8859_1
      , {22, 1}   // ISO8859_2
      , {23, 1}   // ISO8859_3
      , {34, 1}   // ISO8859_4
      , {35, 1}   // ISO8859_5
      , {36, 1}   // ISO8859_6
      , {37, 1}   // ISO8859_7
      , {38, 1}   // ISO8859_8
      , {39, 1}   // ISO8859_9
      , {40, 1}   // ISO8859_13
      , {44, 2}   // KSC_5601
      , {45, 1}   // DOS852
      , {46, 1}   // DOS857
      , {47, 1}   // DOS861
      , {48, 1}   // DOS866
      , {49, 1}   // DOS869
      , {50, 1}   // CYRL
      , {51, 1}   // WIN1250
      , {52, 1}   // WIN1251
      , {53, 1}   // WIN1252
      , {54, 1}   // WIN1253
      , {55, 1}   // WIN1254
      , {56, 2}   // BIG_5
      , {57, 2}   // GB2312
      , {58, 1}   // WIN1255
      , {59, 1}   // WIN1256
      , {60, 1}   // WIN1257
      , {63, 1}   // KOI8R
      , {64, 1}   // KOI8U
      , {65, 1}   // WIN1258
  };
    /**
     * Default mapping table, provides an "identity" mapping.
     */
    public static final char[] DEFAULT_MAPPING = new char[256 * 256];

    static String defaultEncoding = null;

    private static boolean encodingSizesLoaded = false;
    
    private static boolean encodingsLoaded = false;
    
    public static final String ISC_ENCODING_SIZE_RESOURCE = 
        "isc_encoding_size.properties";
    
    public static final String ISC_ENCODINGS_RESOURCE =
        "isc_encodings.properties";

    private static final HashMap iscEncodings = new HashMap();
        
    private static final HashMap iscEncodingSizes = new HashMap();
    
    private static final HashMap javaEncodings = new HashMap();

    private static final Map translations = Collections.synchronizedMap(new HashMap());
    static {
        for (int i = 0; i < DEFAULT_MAPPING.length; i++) {
            DEFAULT_MAPPING[i] = (char)i;
        }
    }
    static {
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(new byte[2])); 
        defaultEncoding = reader.getEncoding();
        try {
            reader.close();
        }
        catch (IOException ioe){
        }
    }
    
    public static Encoding createEncoding(String encoding) {
        if (encoding.equals("NONE"))
            encoding = defaultEncoding;
        
        if (encoding.equals("Cp1250"))
            return new Encoding_Cp1250();
        else if (encoding.equals("Cp1251"))
            return new Encoding_Cp1251();
        else if (encoding.equals("Cp1252"))
            return new Encoding_Cp1252();
        else if (encoding.equals("Cp1253"))
            return new Encoding_Cp1253();
        else if (encoding.equals("Cp1254"))
            return new Encoding_Cp1254();
        else if (encoding.equals("Cp1255"))
            return new Encoding_Cp1255();
        else if (encoding.equals("Cp1256"))
            return new Encoding_Cp1256();
        else if (encoding.equals("Cp1257"))
            return new Encoding_Cp1257();
        else if (encoding.equals("Cp437"))
            return new Encoding_Cp437();
        else if (encoding.equals("Cp737"))
            return new Encoding_Cp737();
        else if (encoding.equals("Cp775"))
            return new Encoding_Cp775();
        else if (encoding.equals("Cp850"))
            return new Encoding_Cp850();
        else if (encoding.equals("Cp852"))
            return new Encoding_Cp852();
        else if (encoding.equals("Cp857"))
            return new Encoding_Cp857();
        else if (encoding.equals("Cp858"))
            return new Encoding_Cp858();
        else if (encoding.equals("Cp860"))
            return new Encoding_Cp860();
        else if (encoding.equals("Cp861"))
            return new Encoding_Cp861();
        else if (encoding.equals("Cp862"))
            return new Encoding_Cp862();
        else if (encoding.equals("Cp863"))
            return new Encoding_Cp863();
        else if (encoding.equals("Cp864"))
            return new Encoding_Cp864();
        else if (encoding.equals("Cp865"))
            return new Encoding_Cp865();
        else if (encoding.equals("Cp866"))
            return new Encoding_Cp866();
        else if (encoding.equals("Cp869"))
            return new Encoding_Cp869();
        else if (encoding.equals("ISO8859_1"))
            return new Encoding_ISO8859_1();
        else if (encoding.equals("ISO8859_2"))
            return new Encoding_ISO8859_2();
        else if (encoding.equals("ISO8859_3"))
            return new Encoding_ISO8859_3();
        else if (encoding.equals("ISO8859_4"))
            return new Encoding_ISO8859_4();
        else if (encoding.equals("ISO8859_5"))
            return new Encoding_ISO8859_5();
        else if (encoding.equals("ISO8859_6"))
            return new Encoding_ISO8859_6();
        else if (encoding.equals("ISO8859_7"))
            return new Encoding_ISO8859_7();
        else if (encoding.equals("ISO8859_8"))
            return new Encoding_ISO8859_8();
        else if (encoding.equals("ISO8859_9"))
            return new Encoding_ISO8859_9();
        else if (encoding.equals("ISO8859_13"))
            return new Encoding_ISO8859_13();
        else 
            return new Encoding_NotOneByte(encoding);
    }
    /**
     * Get size of a character for the specified character set.
     *
     * @param characterSetId of the character set.
     * @return maximum size of the character in bytes or 1 if charset was 
     * not found.
     */
    public static int getCharacterSetSize(int characterSetId) {
        
        for (int i = 0; i < CHARSET_MAXIMUM_SIZE.length; i++) {
            if (CHARSET_MAXIMUM_SIZE[i][0] == characterSetId)
                return CHARSET_MAXIMUM_SIZE[i][1];
        }

        // let's assume that default length is 1
        return 1;
    }

    public static Encoding getEncoding(String encoding){
        if (encoding == null)
            encoding = defaultEncoding;
        
        return createEncoding(encoding);
    }

    public static Encoding getEncoding(String encoding, char[] charMapping){
        if (encoding == null || encoding.equals("NONE"))
            encoding = defaultEncoding;
        
        if (encoding.equals("Cp1250"))
            return new Encoding_Cp1250(charMapping);
        else if (encoding.equals("Cp1251"))
            return new Encoding_Cp1251(charMapping);
        else if (encoding.equals("Cp1252"))
            return new Encoding_Cp1252(charMapping);
        else if (encoding.equals("Cp1253"))
            return new Encoding_Cp1253(charMapping);
        else if (encoding.equals("Cp1254"))
            return new Encoding_Cp1254(charMapping);
        else if (encoding.equals("Cp1255"))
            return new Encoding_Cp1255(charMapping);
        else if (encoding.equals("Cp1256"))
            return new Encoding_Cp1256(charMapping);
        else if (encoding.equals("Cp1257"))
            return new Encoding_Cp1257(charMapping);
        else if (encoding.equals("Cp437"))
            return new Encoding_Cp437(charMapping);
        else if (encoding.equals("Cp737"))
            return new Encoding_Cp737(charMapping);
        else if (encoding.equals("Cp775"))
            return new Encoding_Cp775(charMapping);
        else if (encoding.equals("Cp850"))
            return new Encoding_Cp850(charMapping);
        else if (encoding.equals("Cp852"))
            return new Encoding_Cp852(charMapping);
        else if (encoding.equals("Cp857"))
            return new Encoding_Cp857(charMapping);
        else if (encoding.equals("Cp858"))
            return new Encoding_Cp858(charMapping);
        else if (encoding.equals("Cp860"))
            return new Encoding_Cp860(charMapping);
        else if (encoding.equals("Cp861"))
            return new Encoding_Cp861(charMapping);
        else if (encoding.equals("Cp862"))
            return new Encoding_Cp862(charMapping);
        else if (encoding.equals("Cp863"))
            return new Encoding_Cp863(charMapping);
        else if (encoding.equals("Cp864"))
            return new Encoding_Cp864(charMapping);
        else if (encoding.equals("Cp865"))
            return new Encoding_Cp865(charMapping);
        else if (encoding.equals("Cp866"))
            return new Encoding_Cp866(charMapping);
        else if (encoding.equals("Cp869"))
            return new Encoding_Cp869(charMapping);
        else if (encoding.equals("ISO8859_1"))
            return new Encoding_ISO8859_1(charMapping);
        else if (encoding.equals("ISO8859_2"))
            return new Encoding_ISO8859_2(charMapping);
        else if (encoding.equals("ISO8859_3"))
            return new Encoding_ISO8859_3(charMapping);
        else if (encoding.equals("ISO8859_4"))
            return new Encoding_ISO8859_4(charMapping);
        else if (encoding.equals("ISO8859_5"))
            return new Encoding_ISO8859_5(charMapping);
        else if (encoding.equals("ISO8859_6"))
            return new Encoding_ISO8859_6(charMapping);
        else if (encoding.equals("ISO8859_7"))
            return new Encoding_ISO8859_7(charMapping);
        else if (encoding.equals("ISO8859_8"))
            return new Encoding_ISO8859_8(charMapping);
        else if (encoding.equals("ISO8859_9"))
            return new Encoding_ISO8859_9(charMapping);
        else if (encoding.equals("ISO8859_13"))
            return new Encoding_ISO8859_13(charMapping);
        else 
            return new Encoding_NotOneByte(encoding, charMapping);
    }
    
    public static Encoding getEncoding(String encoding, String mappingPath) throws SQLException {
        
        if (mappingPath == null)
            return getEncoding(encoding);
        
        CharacterTranslator translator = getTranslator(mappingPath);
        
        return getEncoding(encoding, translator.getMapping());
    }
    
    /**
     * Get InterBase encoding for given Java language encoding.
     *
     * @param javaEncoding Java language encoding.
     * @return corresponding InterBase encoding or <code>null</code> if none
     * found.
     */
    public static String getIscEncoding(String javaEncoding) {
        if ("UTF8".equals(javaEncoding))
            javaEncoding = "UTF-8";
        
        if (!encodingsLoaded)
            loadEncodings();

        return (String)javaEncodings.get(javaEncoding);
    }
    
    /**
     * Get size of a character for the specified InterBase encoding.
     *
     * @param iscEncoding InterBase encoding.
     * @return maximum size of the character in bytes or 1 if encoding was 
     * not found.
     */
    public static int getIscEncodingSize(String iscEncoding) {
        if (!encodingSizesLoaded)
            loadEncodingSizes();
            
        Byte result = (Byte)iscEncodingSizes.get(iscEncoding);
        if (result == null)
            return 1;
        else
            return result.byteValue();
    }
        
    /**
     * Get Java language encoding for given InterBase encoding.
     *
     * @param iscEncoding InterBase encoding
     * @return corresponding Java encoding or <code>null</code> if none found.
     */
    public static String getJavaEncoding(String iscEncoding) {
        if (!encodingsLoaded)
            loadEncodings();

        // 
        // very important for performance
        // if javaEncoding is the default one, set to null
        //
        String javaEncoding = (String)iscEncodings.get(iscEncoding);
        String defaultEncoding = System.getProperty("file.encoding");
        if (javaEncoding == null || javaEncoding.equalsIgnoreCase(defaultEncoding)) 
            return null;
        else 
            return javaEncoding;
    }
    
    
    public static CharacterTranslator getTranslator(String mappingPath) throws SQLException {
        CharacterTranslator translator;
        
        translator = (CharacterTranslator)translations.get(mappingPath);
        
        if (translator == null) {
            translator = new CharacterTranslator();
            translator.init(mappingPath);
            translations.put(mappingPath, translator);
        }
        return translator;
    }

    /**
     * Load mapping between Java and InterBase encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private synchronized static void loadEncodings() {
        
        if (encodingsLoaded)
            return;
        
        Properties props;
        try {
            props = loadProperties(ISC_ENCODINGS_RESOURCE);
        } catch(IOException ioex) {
            ioex.printStackTrace();
            return;
        } 

        // fill the direct and inversed mappings
        iscEncodings.putAll(props);

        Iterator iterator = props.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String iscEncoding = (String)entry.getKey();
            
            // special handling for UTF8 and UNICODE_FSS encodings
            // since UTF8 is an alias for UNICODE_FSS in Firebird 1.x
            // it is safe to return UTF8 for all cases
            if ("UNICODE_FSS".equals(iscEncoding))
                continue;
            
            String javaEncoding = (String)entry.getValue();
            javaEncodings.put(javaEncoding, iscEncoding);
        }

        encodingsLoaded = true;
    }
    
    /**
     * Load mapping between Java and InterBase encodings. This method loads the
     * mapping using the classloader that loaded this class.
     */
    private synchronized static void loadEncodingSizes() {
        
        if (encodingSizesLoaded)
            return;
        
        Properties props;
        try {
            props = loadProperties(ISC_ENCODING_SIZE_RESOURCE);
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }

        Iterator iterator = props.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String iscEncoding = (String)entry.getKey();
            String size = (String)entry.getValue();
            iscEncodingSizes.put(iscEncoding, new Byte(size));
        }

        encodingSizesLoaded = true;
    }
    
    /**
     * Load properties from the specified resource. This method uses the same
     * class loader that loaded this class.
     * 
     * @param resource path to the resource relative to the root of the 
     * classloader.
     * 
     * @return instance of {@link Properties} containing loaded resources or
     * <code>null</code> if resource was not found.
     * 
     * @throws IOException if I/O error occured.
     */
    private static Properties loadProperties(String resource) throws IOException {
        ClassLoader cl = EncodingFactory.class.getClassLoader();

        InputStream in = null;

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
