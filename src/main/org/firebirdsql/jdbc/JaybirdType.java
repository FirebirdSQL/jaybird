// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

import java.sql.SQLType;

/**
 * {@link SQLType} definitions specific to Jaybird.
 *
 * @author Mark Rotteveel
 */
@Volatile(reason = "Defined types may receive a different type number when standardized in JDBC")
public enum JaybirdType implements SQLType {

    /**
     * Type for {@code DECFLOAT}.
     * <p>
     * When using Java 26 or higher, use {@code java.sql.JDBCType.DECFLOAT}. This constant might be deprecated and
     * removed once Jaybird only supports versions after Java 26.
     * </p>
     */
    @Volatile(reason = "Prefer java.sql.JDBCType.DECFLOAT when using Java 26 or higher")
    DECFLOAT(JaybirdTypeCodes.DECFLOAT);

    private final Integer type;

    JaybirdType(Integer type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getVendor() {
        return "org.firebirdsql";
    }

    @Override
    public Integer getVendorTypeNumber() {
        return type;
    }

    /**
     * Returns the {@code JaybirdType} for a {@link JaybirdTypeCodes} value.
     *
     * @param type
     *         {@code JaybirdTypeCodes} value
     * @return The {@code JaybirdType} constant
     * @throws IllegalArgumentException
     *         if this enum type has no constant with the specified {@code JaybirdTypeCodes} value
     */
    public static JaybirdType valueOf(int type) {
        for (JaybirdType jaybirdType : values()) {
            if (type == jaybirdType.type)
                return jaybirdType;
        }
        throw new IllegalArgumentException("Type:" + type + " is not a valid JaybirdTypeCodes value.");
    }

}
