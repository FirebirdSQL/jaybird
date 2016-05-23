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

import org.firebirdsql.gds.ng.DatatypeCoder;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeParseException;

/**
 * Implementation of {@link ObjectConverter} to support JDBC 4.2 type conversions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
@SuppressWarnings("Since15")
public class JDBC42ObjectConverter implements ObjectConverter {

    @Override
    public boolean setObject(final FBField field, final Object object) throws SQLException {
        if (object instanceof LocalDate) {
            switch (field.requiredType) {
            case Types.DATE:
                LocalDate localDate = (LocalDate) object;
                field.setFieldData(field.getDatatypeCoder().encodeLocalDate(
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
                field.setFieldData(field.getDatatypeCoder().encodeLocalTime(
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
                field.setFieldData(field.getDatatypeCoder().encodeLocalDate(
                        localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()));
                return true;
            case Types.TIME:
                field.setFieldData(field.getDatatypeCoder().encodeLocalTime(
                        localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                        localDateTime.getNano()));
                return true;
            case Types.TIMESTAMP:
                field.setFieldData(field.getDatatypeCoder().encodeLocalDateTime(
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
        } else if (object instanceof OffsetTime || object instanceof OffsetDateTime) {
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(FBField field, Class<T> type) throws SQLException {
        switch (field.requiredType) {
        case Types.DATE:
            switch (type.getName()) {
            case "java.time.LocalDate": {
                if (field.isNull()) return null;
                final DatatypeCoder.RawDateTimeStruct raw =
                        field.getDatatypeCoder().decodeDateRaw(field.getFieldData());
                return (T) LocalDate.of(raw.year, raw.month, raw.day);
            }
            case "java.time.LocalDateTime": {
                if (field.isNull()) return null;
                final DatatypeCoder.RawDateTimeStruct raw =
                        field.getDatatypeCoder().decodeDateRaw(field.getFieldData());
                return (T) LocalDate.of(raw.year, raw.month, raw.day).atStartOfDay();
            }
            }
            break;
        case Types.TIME:
            switch (type.getName()) {
            case "java.time.LocalTime": {
                if (field.isNull()) return null;
                final DatatypeCoder.RawDateTimeStruct raw =
                        field.getDatatypeCoder().decodeTimeRaw(field.getFieldData());
                return (T) LocalTime.of(raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos());
            }
            case "java.time.LocalDateTime": {
                if (field.isNull()) return null;
                final DatatypeCoder.RawDateTimeStruct raw =
                        field.getDatatypeCoder().decodeTimeRaw(field.getFieldData());
                return (T) LocalTime.of(raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos())
                        .atDate(LocalDate.of(1970, 1, 1));
            }
            }
            break;
        case Types.TIMESTAMP:
            if ("java.time.LocalDateTime".equals(type.getName())) {
                if (field.isNull()) return null;
                final DatatypeCoder.RawDateTimeStruct raw =
                        field.getDatatypeCoder().decodeTimestampRaw(field.getFieldData());
                return (T) LocalDateTime.of(raw.year, raw.month, raw.day, raw.hour, raw.minute, raw.second,
                        raw.getFractionsAsNanos());
            }
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            try {
                switch (type.getName()) {
                case "java.time.LocalDate":
                    return field.isNull() ? null : (T) LocalDate.parse(field.getString().trim());
                case "java.time.LocalTime":
                    return field.isNull() ? null : (T) LocalTime.parse(field.getString().trim());
                case "java.time.LocalDateTime":
                    return field.isNull() ? null : (T) LocalDateTime.parse(field.getString().trim());
                case "java.time.OffsetTime":
                    return field.isNull() ? null : (T) OffsetTime.parse(field.getString().trim());
                case "java.time.OffsetDateTime":
                    return field.isNull() ? null : (T) OffsetDateTime.parse(field.getString().trim());
                }
            } catch (DateTimeParseException e) {
                throw new SQLException("Unable to convert value '" + field.getString() + "' to type " + type, e);
            }
            break;
        }
        throw new SQLNonTransientException(String.format(
                "Unsupported conversion requested for field %s (JDBC type %s) requested type: %s",
                field.getName(), JDBCType.valueOf(field.requiredType), type.getName()));
    }
}
