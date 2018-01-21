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
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

import java.sql.SQLType;

/**
 * {@link SQLType} definitions specific to Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@Volatile(reason = "Defined types may receive a different type number when standardized in JDBC")
public enum JaybirdType implements SQLType {

    @Volatile(reason = "To be standardized by future version of JDBC, will be deprecated")
    DECFLOAT(JaybirdTypeCodes.DECFLOAT);

    private Integer type;

    JaybirdType(final Integer type) {
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
    
}
