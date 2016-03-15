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
package org.firebirdsql.gds;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class maps the extended JDBC properties to parameter buffer types (for transaction and database
 * parameter buffers). It uses <code>java.lang.reflection</code> to determine correct type of the parameter
 * passed to the {@link java.sql.Driver#connect(String, Properties)} method.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class ParameterBufferHelper {

    private static final Logger log = LoggerFactory.getLogger(ParameterBufferHelper.class);
    private static final DpbParameterType UNKNOWN_DPB_TYPE = new DpbParameterType(null, null, DpbValueType.TYPE_UNKNOWN);

    public static final String DPB_PREFIX = "isc_dpb_";
    public static final String TPB_PREFIX = "isc_tpb_";

    public static final String ISC_DPB_TYPES_RESOURCE = "isc_dpb_types.properties";

    private static final Map<String, Integer> dpbTypes;
    private static final Map<String, DpbParameterType> dpbParameterTypes;
    private static final Map<String, Integer> tpbTypes;

    /*
     * Initialize mappings between various GDS constant names and
     * their values. This operation should be executed only once.
     */
    static {
        final Map<String, Integer> tempDpbTypes = new HashMap<>();
        final Map<String, Integer> tempTpbTypes = new HashMap<>();
        Class<ISCConstants> iscClass = ISCConstants.class;

        Field[] fields = iscClass.getFields();

        for (Field field : fields) {
            if (!field.getType().getName().equals("int"))
                continue;

            String name = field.getName();
            Integer value;
            try {
                value = (Integer) field.get(null);
            } catch (IllegalAccessException iaex) {
                continue;
            }

            if (name.startsWith(DPB_PREFIX)) {
                // put the correct parameter name
                tempDpbTypes.put(name.substring(DPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tempDpbTypes.put(name, value);
            } else if (name.startsWith(TPB_PREFIX)) {
                // put the correct parameter name
                tempTpbTypes.put(name.substring(TPB_PREFIX.length()), value);
                // put the full name to tolerate people's mistakes
                tempTpbTypes.put(name, value);
            }
        }

        dpbTypes = Collections.unmodifiableMap(tempDpbTypes);
        tpbTypes = Collections.unmodifiableMap(tempTpbTypes);
        dpbParameterTypes = Collections.unmodifiableMap(loadDpbParameterTypes());
    }

    /**
     * Get integer value of the DPB key corresponding to the specified name.
     *
     * @param name
     *         name of the key.
     * @return instance of {@link Integer} corresponding to the specified name
     *         or <code>null</code> if value is not known.
     */
    public static Integer getDpbKey(String name) {
        return dpbTypes.get(name);
    }

    /**
     * Gets the {@link DpbParameterType} for the specified dpb item name (short or long)
     *
     * @param name
     *         Name of the dpb item
     * @return <code>DpbParameterType</code> instance, or <code>null</code> if there is no item with this name
     */
    public static DpbParameterType getDpbParameterType(final String name) {
        DpbParameterType dpbParameterType = dpbParameterTypes.get(name);
        if (dpbParameterType == null) {
            // No explicit type defined
            Integer dpbKey = getDpbKey(name);
            if (dpbKey != null) {
                final String canonicalName = name.startsWith(DPB_PREFIX) ? name : DPB_PREFIX + name;
                dpbParameterType = new DpbParameterType(canonicalName, dpbKey, DpbValueType.TYPE_UNKNOWN);
            }
        }
        return dpbParameterType;
    }

    /**
     * Get mapping between DPB names and their keys.
     *
     * @return instance of {@link Map}, where key is the name of DPB parameter,
     *         value is its DPB key.
     */
    public static Map<String, Integer> getDpbMap() {
        return dpbTypes;
    }

    public static Object parseDpbString(String name, Object value) {
        DpbParameterType type = dpbParameterTypes.get(name);

        if (type == null)
            type = UNKNOWN_DPB_TYPE;

        return type.parseDpbString(value);
    }

    /**
     * Get value of TPB parameter for the specified name. This method tries to
     * match string representation of the TPB parameter with its value.
     *
     * @param name
     *         string representation of TPB parameter, can have "isc_tpb_"
     *         prefix.
     * @return value corresponding to the specified parameter name or null if
     *         nothing was found.
     */
    public static Integer getTpbParam(String name) {
        return tpbTypes.get(name);
    }

    /**
     * Load properties from the specified resource. This method uses the same
     * class loader that loaded this class.
     *
     * @param resource
     *         path to the resource relative to the root of the
     *         classloader.
     * @return instance of {@link Properties} containing loaded resources or
     *         <code>null</code> if resource was not found.
     * @throws IOException
     *         if I/O error occured.
     */
    private static Properties loadProperties(String resource) throws IOException {
        ClassLoader cl = ParameterBufferHelper.class.getClassLoader();
        InputStream in;

        // get the stream from the classloader or system classloader
        if (cl == null)
            in = ClassLoader.getSystemResourceAsStream(resource);
        else
            in = cl.getResourceAsStream(resource);

        if (in == null)
            throw new IOException("Unable to load resource file " + resource);

        try {
            Properties props = new Properties();
            props.load(in);
            return props;
        } finally {
            in.close();
        }
    }

    /**
     * Load mapping between DPB key and their parameter types.
     */
    private static Map<String, DpbParameterType> loadDpbParameterTypes() {
        Properties props;
        try {
            props = loadProperties(ISC_DPB_TYPES_RESOURCE);
        } catch (IOException ex) {
            log.error("Could not load " + ISC_DPB_TYPES_RESOURCE, ex);
            return Collections.emptyMap();
        }
        final Map<String, DpbParameterType> tempDpbParameterTypes = new HashMap<>();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // Remove text intended as comment (which due to the properties format is part of the value)
            int hashIndex = value.indexOf('#');
            if (hashIndex != -1) {
                value = value.substring(0, hashIndex).trim();
            }

            final DpbValueType typeValue;
            if ("boolean".equals(value)) {
                typeValue = DpbValueType.TYPE_BOOLEAN;
            } else if ("byte".equals(value)) {
                typeValue = DpbValueType.TYPE_BYTE;
            } else if ("int".equals(value)) {
                typeValue = DpbValueType.TYPE_INT;
            } else if ("string".equals(value)) {
                typeValue = DpbValueType.TYPE_STRING;
            } else {
                continue;
            }
            final Integer dpbKey = dpbTypes.get(key);
            final DpbParameterType dpbParameterType = new DpbParameterType(key, dpbKey, typeValue);
            tempDpbParameterTypes.put(key, dpbParameterType);
            tempDpbParameterTypes.put(dpbParameterType.getShortName(), dpbParameterType);
        }
        return tempDpbParameterTypes;
    }

    /**
     * Enum with the various Dpb value types, and conversion from String to that type.
     */
    public enum DpbValueType {
        TYPE_UNKNOWN {
            @Override
            public Object parseDpbString(String value) {
                /* set the value of the DPB by probing to convert string
                 * into int or byte value, this method gives very good result
                 * for guessing the method to call from the actual value;
                 * null values and empty strings are assumed to be boolean.
                 */
                if (value == null || "".equals(value))
                    return Boolean.TRUE;

                try {
                    // try to deal with a value as a byte or int
                    int intValue = Integer.parseInt(value);
                    // TODO Find out if this is intentional
                    if (intValue < 256)
                        return (byte) intValue;
                    else
                        return intValue;
                } catch (NumberFormatException nfex) {
                    // all else fails: return as is (string)
                    return value;
                }
            }
        },
        TYPE_BOOLEAN {
            @Override
            public Object parseDpbString(String value) {
                return "".equals(value) ? Boolean.TRUE : Boolean.valueOf(value);
            }
        },
        TYPE_BYTE {
            @Override
            public Object parseDpbString(String value) {
                return Byte.valueOf(value);
            }
        },
        TYPE_INT {
            @Override
            public Object parseDpbString(String value) {
                return Integer.valueOf(value);
            }
        },
        TYPE_STRING {
            @Override
            public Object parseDpbString(String value) {
                return value;
            }
        };

        /**
         * Parses the supplied Object (which should be a String, Boolean, Byte or Integer) to
         * the type appropriate for this DpbValueType.
         *
         * @param value
         *         The value to parse
         * @return Parsed value (either a Boolean, Byte, Integer or String)
         */
        public abstract Object parseDpbString(String value);
    }

    /**
     * Dpb type, which is the name, the key for the dpb and its value type.
     * <p>
     * Provides conversion to the required type.
     * </p>
     */
    public static class DpbParameterType {
        private final String name;
        private final String shortName;
        private final Integer dpbKey;
        private final DpbValueType type;

        public DpbParameterType(String name, Integer dpbKey, DpbValueType type) {
            this.name = name;
            shortName = name != null ? name.substring(DPB_PREFIX.length()) : null;
            this.dpbKey = dpbKey;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public Integer getDpbKey() {
            return dpbKey;
        }

        public DpbValueType getType() {
            return type;
        }

        /**
         * Parses the supplied Object (which should be a String, Boolean, Byte or Integer) to
         * the type appropriate for this DpbParameterType.
         *
         * @param value
         *         The value to parse
         * @return Parsed value (either a Boolean, Byte, Integer or String)
         */
        public Object parseDpbString(Object value) {
            // for the sake of unification we allow passing boolean, byte and integer
            // types too, we loose some cycles here, but that is called relatively
            // rarely, a trade off between code maintainability and CPU cycles.
            if (!(value instanceof String)) {
                if (value instanceof Boolean || value instanceof Byte || value instanceof Integer)
                    return value;

                // if passed value is not string, throw an exception
                if (value != null)
                    throw new ClassCastException(value.getClass().getName());
            }
            return type.parseDpbString((String) value);
        }
    }
}
