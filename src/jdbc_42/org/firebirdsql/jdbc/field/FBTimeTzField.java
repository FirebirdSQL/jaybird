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
 * Field for {@code TIME WITH TIME ZONE}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public class FBTimeTzField extends FBField {

    FBTimeTzField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    private OffsetTime getOffsetTime() throws SQLException {
        if (isNull()) return null;

        return getTimeZoneDatatypeCoder().decodeTimeTz(getFieldData());
    }

    private void setOffsetTime(OffsetTime offsetTime) {
        setFieldData(getTimeZoneDatatypeCoder().encodeTimeTz(offsetTime));
    }

    private OffsetDateTime getOffsetDateTime() throws SQLException {
        OffsetTime offsetTime = getOffsetTime();
        if (offsetTime == null) {
            return null;
        }
        // We need to base on a date to determine value, we use the current date; this will be inconsistent depending
        // on the date, but this aligns closest with Firebird behaviour and SQL standard
        ZoneOffset offset = offsetTime.getOffset();
        OffsetDateTime today = OffsetDateTime.now(offset);
        return OffsetDateTime.of(today.toLocalDate(), offsetTime.toLocalTime(), offset);
    }

    private void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        setOffsetTime(offsetDateTime.toOffsetTime());
    }

    @Override
    public Object getObject() throws SQLException {
        return getOffsetTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLNonTransientException("getObject called with type null");
        }
        switch (type.getName()) {
        case OFFSET_TIME_CLASS_NAME:
            return (T) getOffsetTime();
        case OFFSET_DATE_TIME_CLASS_NAME:
            return (T) getOffsetDateTime();
        }
        return super.getObject(type);
    }

    @Override
    public void setObject(Object value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        if (value instanceof OffsetTime) {
            setOffsetTime((OffsetTime) value);
        } else if (value instanceof OffsetDateTime) {
            setOffsetDateTime((OffsetDateTime) value);
        } else {
            super.setObject(value);
        }
    }

    @Override
    public String getString() throws SQLException {
        if (isNull()) return null;

        return String.valueOf(getOffsetTime());
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
            throw new TypeConversionException("Unable to convert value '" + value + "' to type TIME WITH TIME ZONE", e);
        }
    }

    private TimeZoneDatatypeCoder getTimeZoneDatatypeCoder() {
        return TimeZoneDatatypeCoder.getInstanceFor(getDatatypeCoder());
    }

}
