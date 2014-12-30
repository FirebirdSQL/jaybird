/*
 * $Id$
 * 
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

import java.sql.SQLException;
import java.sql.Types;
import java.time.*;

/**
 * Implementation of {@link org.firebirdsql.jdbc.field.ObjectConverter} to support JDBC 4.2 type conversions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class JDBC42ObjectConverter implements ObjectConverter {
    @Override
    public boolean setObject(FBField field, Object object) throws SQLException {
        if (object instanceof LocalDate) {
            switch (field.requiredType) {
            case Types.DATE:
                LocalDate localDate = (LocalDate) object;
                field.setFieldData(field.field.encodeLocalDate(
                        localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()));
                return true;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                field.setString(object.toString());
                return true;
            }
        } else if (object instanceof LocalTime) {
            switch (field.requiredType) {
            case Types.TIME:
                LocalTime localTime = (LocalTime) object;
                field.setFieldData(field.field.encodeLocalTime(
                        localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano()));
                return true;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                field.setString(object.toString());
                return true;
            }
        } else if (object instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) object;
            switch (field.requiredType) {
            case Types.DATE:
                field.setFieldData(field.field.encodeLocalDate(
                        localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()));
                return true;
            case Types.TIME:
                field.setFieldData(field.field.encodeLocalTime(
                        localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                        localDateTime.getNano()));
                return true;
            case Types.TIMESTAMP:
                field.setFieldData(field.field.encodeLocalDateTime(
                        localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                        localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                        localDateTime.getNano()));
                return true;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                field.setString(object.toString());
                return true;
            }
        } else if (object instanceof OffsetTime) {
            switch (field.requiredType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                field.setString(object.toString());
                return true;
            }
        } else if (object instanceof OffsetDateTime) {
            switch (field.requiredType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                field.setString(object.toString());
                return true;
            }
        }
        return false;
    }
}
