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
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

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

    @Override
    final OffsetDateTime getOffsetDateTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeOffsetDateTime(getFieldData());
    }

    @Override
    final void setOffsetDateTime(OffsetDateTime offsetDateTime) throws SQLException {
        setFieldData(getTimeZoneCodec().encodeOffsetDateTime(offsetDateTime));
    }

    @Override
    final OffsetTime getOffsetTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeOffsetTime(getFieldData());
    }

    @Override
    final void setOffsetTime(OffsetTime offsetTime) throws SQLException {
        setFieldData(timeZoneCodec.encodeOffsetTime(offsetTime));
    }

    @Override
    final ZonedDateTime getZonedDateTime() throws SQLException {
        if (isNull()) return null;

        return timeZoneCodec.decodeZonedDateTime(getFieldData());
    }

    @Override
    final void setZonedDateTime(ZonedDateTime zonedDateTime) throws SQLException {
        setFieldData(timeZoneCodec.encodeZonedDateTime(zonedDateTime));
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

    private void setStringParse(String value) throws SQLException {
        // TODO Better way to do this?
        // TODO More lenient parsing?
        if (value.indexOf('T') != -1) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(value);
            setOffsetDateTime(offsetDateTime);
        } else {
            OffsetTime offsetTime = OffsetTime.parse(value);
            setOffsetTime(offsetTime);
        }
    }

    @Override
    public void setString(String value) throws SQLException {
        if (setWhenNull(value)) return;

        String string = value.trim();
        try {
            setStringParse(string);
        } catch (DateTimeParseException e) {
            SQLException conversionException = invalidSetConversion(String.class, string);
            conversionException.initCause(e);
            throw conversionException;
        }
    }
}
