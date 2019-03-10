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

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.OFFSET_DATE_TIME_CLASS_NAME;
import static org.firebirdsql.jdbc.JavaTypeNameConstants.OFFSET_TIME_CLASS_NAME;

/**
 * Field for {@code TIMESTAMP WITH TIME ZONE}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
class FBTimestampTzField extends FBField {

    FBTimestampTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    private OffsetDateTime getOffsetDateTime() throws SQLException {
        if (isNull()) return null;

        return getTimeZoneDatatypeCoder().decodeTimestampTz(getFieldData());
    }

    private void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        setFieldData(getTimeZoneDatatypeCoder().encodeTimestampTz(offsetDateTime));
    }

    private OffsetTime getOffsetTime() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        return offsetDateTime != null ? offsetDateTime.toOffsetTime() : null;
    }

    private void setOffsetTime(OffsetTime offsetTime) throws SQLException {
        // We need to base on a date to determine value, we use the current date; this will be inconsistent depending
        // on the date, but this aligns closest with Firebird behaviour and SQL standard
        ZoneOffset offset = offsetTime.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        OffsetDateTime timeToday = OffsetDateTime.of(today.toLocalDate(), offsetTime.toLocalTime(), offset);

        setOffsetDateTime(timeToday);
    }

    @Override
    public Object getObject() throws SQLException {
        return getOffsetDateTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLNonTransientException("getObject called with type null");
        }
        switch (type.getName()) {
        case OFFSET_DATE_TIME_CLASS_NAME:
            return (T) getOffsetDateTime();
        case OFFSET_TIME_CLASS_NAME:
            return (T) getOffsetTime();
        }
        return super.getObject(type);
    }

    @Override
    public void setObject(Object value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        if (value instanceof OffsetDateTime) {
            setOffsetDateTime((OffsetDateTime) value);
        } else if (value instanceof OffsetTime) {
            setOffsetTime((OffsetTime) value);
        } else {
            super.setObject(value);
        }
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getOffsetDateTime());
    }

    @Override
    public void setString(String value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        try {
            // TODO Better way to do this?
            // TODO More lenient parsing?
            if (value.indexOf('T') != -1) {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(value.trim());
                setOffsetDateTime(offsetDateTime);
            } else {
                OffsetTime offsetTime = OffsetTime.parse(value.trim());
                setOffsetTime(offsetTime);
            }
        } catch (DateTimeParseException e) {
            throw new TypeConversionException("Unable to convert value '" + value + "' to type TIMESTAMP WITH TIME ZONE", e);
        }
    }

    private TimeZoneDatatypeCoder getTimeZoneDatatypeCoder() {
        return TimeZoneDatatypeCoder.getInstanceFor(getDatatypeCoder());
    }
}
