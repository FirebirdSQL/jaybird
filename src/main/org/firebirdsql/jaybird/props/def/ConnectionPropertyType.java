// SPDX-FileCopyrightText: Copyright 2020-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props.def;

import org.firebirdsql.jaybird.props.DpbType;
import org.firebirdsql.jaybird.props.internal.TransactionNameMapping;
import org.firebirdsql.util.InternalApi;

/**
 * Basic connection property types supported by Jaybird.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public enum ConnectionPropertyType {

    STRING(DpbType.STRING) {
        @Override
        public String toType(String stringValue) {
            return stringValue;
        }

        @Override
        public String toType(Integer intValue) {
            return intValue != null ? String.valueOf(intValue) : null;
        }

        @Override
        public String toType(Boolean booleanValue) {
            return booleanValue != null ? String.valueOf(booleanValue) : null;
        }

        @Override
        public Integer asInteger(Object value) {
            return (Integer) INT.toType((String) value);
        }

        @Override
        public Boolean asBoolean(Object value) {
            return (Boolean) BOOLEAN.toType((String) value);
        }
    },
    INT(DpbType.INT) {
        @Override
        public Integer toType(String stringValue) {
            return stringValue != null ? Integer.valueOf(stringValue) : null;
        }

        @Override
        public Integer toType(Integer intValue) {
            return intValue;
        }

        @Override
        public Integer toType(Boolean booleanValue) {
            if (booleanValue == null) return null;
            return booleanValue ? 1 : 0;
        }

        @Override
        public Integer asInteger(Object value) {
            return (Integer) value;
        }

        @Override
        public Boolean asBoolean(Object value) {
            return (Boolean) BOOLEAN.toType((Integer) value);
        }
    },
    BOOLEAN(DpbType.SINGLE) {
        @Override
        @SuppressWarnings("java:S2447")
        public Boolean toType(String stringValue) {
            if (stringValue == null) {
                return null;
            } else if (stringValue.isEmpty()) {
                // For backwards compatibility, empty string means true
                return Boolean.TRUE;
            } else {
                return Boolean.valueOf(stringValue);
            }
        }

        @Override
        @SuppressWarnings("java:S2447")
        public Boolean toType(Integer intValue) {
            if (intValue == null) {
                return null;
            } else if (intValue == 0) {
                return false;
            } else if (intValue == 1) {
                return true;
            }
            throw new IllegalArgumentException("Cannot convert integer other than null, 0 or 1 to boolean");
        }

        @Override
        public Boolean toType(Boolean booleanValue) {
            return booleanValue;
        }

        @Override
        public Integer asInteger(Object value) {
            return (Integer) INT.toType((Boolean) value);
        }

        @Override
        public Boolean asBoolean(Object value) {
            return (Boolean) value;
        }
    },
    /**
     * Maps between transaction isolation level names and JDBC transaction isolation level codes.
     * For internal use only.
     */
    @InternalApi
    TRANSACTION_ISOLATION(DpbType.NONE) {
        @Override
        public Object toType(String stringValue) {
            if (stringValue == null) return null;
            return TransactionNameMapping.toIsolationLevel(stringValue);
        }

        @Override
        public Object toType(Integer intValue) {
            return intValue;
        }

        @Override
        public Object toType(Boolean booleanValue) {
            if (booleanValue == null) return null;
            throw new IllegalArgumentException("Cannot convert Boolean to transaction isolation");
        }

        @Override
        public String asString(Object value) {
            if (value == null) return null;
            return TransactionNameMapping.toIsolationLevelName((int) value, true);
        }

        @Override
        public Integer asInteger(Object value) {
            return (Integer) value;
        }

        @Override
        @SuppressWarnings("java:S2447")
        public Boolean asBoolean(Object value) {
            if (value == null) return null;
            throw new IllegalArgumentException("Cannot convert transaction isolation to Boolean");
        }
    };

    private final DpbType defaultDpbType;

    ConnectionPropertyType(DpbType defaultDpbType) {
        this.defaultDpbType = defaultDpbType;
    }

    public final DpbType getDefaultParameterType() {
        return defaultDpbType;
    }

    /**
     * Convert a string to a value of this property type.
     *
     * @param stringValue
     *         String value
     * @return Appropriate value of this type
     * @throws IllegalArgumentException
     *         For conversion errors
     */
    @InternalApi
    public abstract Object toType(String stringValue);

    /**
     * Convert an integer to a value of this property type.
     *
     * @param intValue
     *         integer value
     * @return Appropriate value of this type
     * @throws IllegalArgumentException
     *         For conversion errors
     */
    @InternalApi
    public abstract Object toType(Integer intValue);

    /**
     * Convert a boolean to a value of this property type.
     *
     * @param booleanValue
     *         boolean value
     * @return Appropriate value of this type
     * @throws IllegalArgumentException
     *         For conversion errors
     */
    @InternalApi
    public abstract Object toType(Boolean booleanValue);

    /**
     * Convert a value of this property type to integer.
     *
     * @param value
     *         value of this property type
     * @return Integer equivalent
     * @throws ClassCastException
     *         if {@code value} is not of this type
     */
    @InternalApi
    public abstract Integer asInteger(Object value);

    /**
     * Convert a value of this property type to string.
     *
     * @param value
     *         value of this property type
     * @return String equivalent
     */
    @InternalApi
    public String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Convert a value of this property type to boolean.
     *
     * @param value
     *         value of this property type
     * @return Boolean equivalent
     * @throws ClassCastException
     *         if {@code value} is not of this type
     */
    @InternalApi
    public abstract Boolean asBoolean(Object value);

}
