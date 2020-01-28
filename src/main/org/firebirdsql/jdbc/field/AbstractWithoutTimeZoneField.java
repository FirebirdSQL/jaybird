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

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Superclass for {@link FBTimeField}, {@link FBTimestampField} and {@link FBDateField} to handle session time zone.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
abstract class AbstractWithoutTimeZoneField extends FBField {

    private Calendar calendar;

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

    final Calendar getCalendar() {
        if (calendar == null) {
            return initCalendar();
        }
        return calendar;
    }

    private Calendar initCalendar() {
        TimeZone sessionTimeZone = gdsHelper != null ? gdsHelper.getSessionTimeZone() : null;
        return calendar = sessionTimeZone != null ? Calendar.getInstance(sessionTimeZone) : Calendar.getInstance();
    }

}
