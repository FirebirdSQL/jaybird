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
package org.firebirdsql.gds.ng.tz;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.junit.Test;

import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.ng.tz.TimeZoneCodecAbstractTest.*;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TimeZoneDatatypeCoder} that are not covered through {@link TimeZoneCodecStandardTimestampTzTest}
 * and {@link TimeZoneCodecExtendedTimestampTzTest}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TimeZoneDatatypeCoderTest {

    @Test
    public void getTimeZoneCode_nonTimeZoneType_throwsSQLException() {
        for (int baseType : new int[] { SQL_TEXT, SQL_VARYING, SQL_SHORT, SQL_LONG, SQL_FLOAT, SQL_DOUBLE,
                SQL_TIMESTAMP, SQL_BLOB, SQL_TYPE_TIME, SQL_TYPE_DATE, SQL_INT64, SQL_INT128, SQL_DEC16, SQL_DEC34,
                SQL_BOOLEAN }) {
            for (int type : new int[] { baseType, baseType | 1 }) {
                FieldDescriptor descriptor = rowDescriptorBuilder().setType(type).toFieldDescriptor();
                try {
                    getDefaultTzCoder(FIXED_AT_2019_03_09).getTimeZoneCodecFor(descriptor);
                    fail("Should have thrown SQLException for type " + type);
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }
}
