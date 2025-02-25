// SPDX-FileCopyrightText: Copyright 2019-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Superclass for {@link FBTimeField}, {@link FBTimestampField} and {@link FBDateField} to handle session time zone.
 *
 * @author Mark Rotteveel
 */
abstract class AbstractWithoutTimeZoneField extends FBField {

    private Calendar calendar;

    @NullMarked
    AbstractWithoutTimeZoneField(FieldDescriptor fieldDescriptor, FieldDataProvider dataProvider, int requiredType)
            throws SQLException {
        super(fieldDescriptor, dataProvider, requiredType);
    }

    @Override
    public final Time getTime() throws SQLException {
        return getTime(getCalendar());
    }

    @Override
    public final Timestamp getTimestamp() throws SQLException {
        return getTimestamp(getCalendar());
    }

    @Override
    public final Date getDate() throws SQLException {
        return getDate(getCalendar());
    }

    @Override
    public final void setTime(Time value) throws SQLException {
        setTime(value, getCalendar());
    }

    @Override
    public final void setTimestamp(Timestamp value) throws SQLException {
        setTimestamp(value, getCalendar());
    }

    @Override
    public final void setDate(Date value) throws SQLException {
        setDate(value, getCalendar());
    }

    final @NonNull Calendar getCalendar() {
        return calendar != null ? calendar : initCalendar();
    }

    private @NonNull Calendar initCalendar() {
        TimeZone sessionTimeZone = gdsHelper != null ? gdsHelper.getSessionTimeZone() : null;
        return calendar = sessionTimeZone != null ? Calendar.getInstance(sessionTimeZone) : Calendar.getInstance();
    }

}
