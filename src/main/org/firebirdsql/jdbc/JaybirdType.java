/*
 * Firebird Open Source JDBC Driver
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

    @Volatile(reason = "To be standardized by future version of JDBC, will be deprecated")
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
