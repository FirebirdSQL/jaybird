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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import static org.junit.Assert.*;

/**
 * Validator for columns of metadata result sets.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 *
 * @param <T> Enum type, implementing {@link MetaDataInfo} for metadata column information
 */
class MetaDataValidator<T extends Enum<T> & MetaDataValidator.MetaDataInfo> {

    private T mdi;

    protected MetaDataValidator(T mdi) {
        this.mdi = mdi;
    }

    /**
     * Asserts the expected position of this column in the resultset.
     *
     * @param rs ResultSet to use for asserting the column position
     * @throws SQLException
     */
    public void assertColumnPosition(ResultSet rs) throws SQLException {
        assertEquals(String.format("Unexpected column position for %s", mdi), mdi.getPosition(), rs.findColumn(mdi.name()));
    }

    /**
     * Asserts the type of this column as reported by the ResultSetMetaData of this ResultSet.
     *
     * @param rs ResultSet
     * @throws SQLException
     */
    public void assertColumnType(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int sqlType = md.getColumnType(mdi.getPosition());
        assertTrue(String.format("Unexpected SQL Type %d for column %s", sqlType, mdi), isAllowedSqlType(sqlType));
    }

    /**
     * Asserts the value of this column on the current row of the resultset.
     *
     * @param rs ResultSet
     * @param expectedValue Value expected
     * @throws SQLException
     */
    public void assertColumnValue(ResultSet rs, Object expectedValue) throws SQLException {
        if (mdi.getColumnClass().isInstance(expectedValue) || expectedValue == null) {
            if (mdi.getColumnClass().equals(String.class)) {
                assertStringColumnValue(rs, (String) expectedValue);
            } else if (mdi.getColumnClass().equals(Integer.class)) {
                assertIntegerColumnValue(rs, (Integer) expectedValue);
            } else if (mdi.getColumnClass().equals(Short.class)) {
                assertShortColumnValue(rs, (Short) expectedValue);
            } else {
                assertObjectColumnValue(rs, expectedValue);
            }
        } else {
            assertObjectColumnValue(rs, expectedValue);
        }
    }

    private void assertObjectColumnValue(ResultSet rs, Object expectedValue) throws SQLException {
        Object value = rs.getObject(mdi.name());
        assertEquals(String.format("Unexpected value for %s", mdi), expectedValue, value);
    }

    private void assertShortColumnValue(ResultSet rs, Short expectedValue) throws SQLException {
        short value = rs.getShort(mdi.name());
        if (expectedValue != null) {
            assertEquals(String.format("Unexpected value for %s", mdi), expectedValue.shortValue(), value);
            assertFalse(String.format("%s should not be actual NULL", mdi), rs.wasNull());
        } else {
            assertEquals(String.format("Unexpected value for %s (expected NULL/0)", mdi), 0, value);
            assertTrue(String.format("%s should be actual NULL", mdi), rs.wasNull());
        }
    }

    private void assertIntegerColumnValue(ResultSet rs, Integer expectedValue) throws SQLException {
        int value = rs.getInt(mdi.name());
        if (expectedValue != null) {
            assertEquals(String.format("Unexpected value for %s", mdi), expectedValue.intValue(), value);
            assertFalse(String.format("%s should not be actual NULL", mdi), rs.wasNull());
        } else {
            assertEquals(String.format("Unexpected value for %s (expected NULL/0)", mdi), 0, value);
            assertTrue(String.format("%s should be actual NULL", mdi), rs.wasNull());
        }
    }

    private void assertStringColumnValue(ResultSet rs, String expectedValue)
            throws SQLException {
        String value = rs.getString(mdi.name());
        assertEquals(String.format("Unexpected value for %s", mdi), expectedValue, value);
    }

    private boolean isAllowedSqlType(int sqlType) {
        if (mdi.getColumnClass() == String.class) {
            return (sqlType == Types.CHAR || sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR);
        } else if (mdi.getColumnClass() == Integer.class) {
            return (sqlType == Types.INTEGER);
        } else if (mdi.getColumnClass() == Short.class) {
            return (sqlType == Types.SMALLINT);
        }
        return false;
    }

    /**
     * Interface for the information enums for metadata columns should be able to provide for the {@link MetaDataValidator}.
     */
    public interface MetaDataInfo {

        /**
         * Position of this metadata column in the resultset
         *
         * @return 1-based position of the column
         */
        int getPosition();

        /**
         * Java class of the expected column type (String=> Varchar, Integer=> Integer, Short=> Smallint)
         */
        Class<?> getColumnClass();

        /**
         * @return MetaDataValidator for this MetaDataInfo instance
         */
        MetaDataValidator<?> getValidator();
    }
}
