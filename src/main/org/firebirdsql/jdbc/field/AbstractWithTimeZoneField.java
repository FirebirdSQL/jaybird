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
import java.time.*;
import java.util.Calendar;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;

/**
 * Superclass for {@link FBTimeTzField}, {@link FBTimestampTzField} to handle legacy date/time types and common behaviour.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
abstract class AbstractWithTimeZoneField extends FBField {

    private ZoneId defaultZoneId;
    private final TimeZoneDatatypeCoder.TimeZoneCodec timeZoneCodec;

    AbstractWithTimeZoneField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
        timeZoneCodec = TimeZoneDatatypeCoder.getInstanceFor(getDatatypeCoder())
                .getTimeZoneCodecFor(fieldDescriptor);
    }

    final OffsetDateTime getOffsetDateTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeOffsetDateTime(getFieldData());
    }

    final void setOffsetDateTime(OffsetDateTime offsetDateTime) throws SQLException {
        setFieldData(getTimeZoneCodec().encodeOffsetDateTime(offsetDateTime));
    }

    final OffsetTime getOffsetTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeOffsetTime(getFieldData());
    }

    final void setOffsetTime(OffsetTime offsetTime) throws SQLException {
        setFieldData(timeZoneCodec.encodeOffsetTime(offsetTime));
    }

    final ZonedDateTime getZonedDateTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeZonedDateTime(getFieldData());
    }

    final void setZonedDateTime(ZonedDateTime zonedDateTime) throws SQLException {
        setFieldData(timeZoneCodec.encodeZonedDateTime(zonedDateTime));
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
        case ZONED_DATE_TIME_CLASS_NAME:
            return (T) getZonedDateTime();
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
        } else if (value instanceof ZonedDateTime) {
            setZonedDateTime((ZonedDateTime) value);
        } else {
            super.setObject(value);
        }
    }

    @Override
    public java.sql.Time getTime() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        if (offsetDateTime == null) {
            return null;
        }
        return new java.sql.Time(offsetDateTime.toInstant().toEpochMilli());
    }

    @Override
    public java.sql.Time getTime(Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        return getTime();
    }

    @Override
    public void setTime(java.sql.Time value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        OffsetDateTime offsetDateTime = ZonedDateTime.of(LocalDate.now(), value.toLocalTime(), getDefaultZoneId())
                .toOffsetDateTime();
        setOffsetDateTime(offsetDateTime);
    }

    @Override
    public void setTime(java.sql.Time value, Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        setTime(value);
    }

    @Override
    public java.sql.Timestamp getTimestamp() throws SQLException {
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        if (offsetDateTime == null) {
            return null;
        }
        return new java.sql.Timestamp(offsetDateTime.toInstant().toEpochMilli());
    }

    @Override
    public java.sql.Timestamp getTimestamp(Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        return getTimestamp();
    }

    @Override
    public void setTimestamp(java.sql.Timestamp value) throws SQLException {
        if (value == null) {
            setNull();
            return;
        }

        OffsetDateTime offsetDateTime = value.toLocalDateTime()
                .atZone(getDefaultZoneId())
                .toOffsetDateTime();
        setOffsetDateTime(offsetDateTime);
    }

    @Override
    public void setTimestamp(java.sql.Timestamp value, Calendar cal) throws SQLException {
        // Intentionally ignoring calendar, see jdp-2019-03
        setTimestamp(value);
    }

    final TimeZoneDatatypeCoder.TimeZoneCodec getTimeZoneCodec() {
        return timeZoneCodec;
    }

    final ZoneId getDefaultZoneId() {
        if (defaultZoneId != null) {
            return defaultZoneId;
        }
        return defaultZoneId = ZoneId.systemDefault();
    }

    void setStringParse(String value) throws SQLException {
        // TODO Better way to do this?
        // TODO More lenient parsing?
        if (value.indexOf('T') != -1) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(value.trim());
            setOffsetDateTime(offsetDateTime);
        } else {
            OffsetTime offsetTime = OffsetTime.parse(value.trim());
            setOffsetTime(offsetTime);
        }
    }
}
