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
package org.firebirdsql.gds;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link GDSExceptionHelper}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class GDSExceptionHelperTest {

    /**
     * Checks if a message of the Jaybird range of error messages can be retrieved
     */
    @Test
    public void getMessage_JaybirdRange() {
        // Test expectation is tied to the actual error message defined.
        final String expected = "getSegment called with sizeRequested (null), should be > 0";

        GDSExceptionHelper.GDSMessage message = GDSExceptionHelper.getMessage(
                JaybirdErrorCodes.jb_blobGetSegmentNegative);

        assertEquals(expected, message.toString());
    }

    /**
     * Checks if a message of the Firebird range of error messages can be retrieved
     */
    @Test
    public void getMessage_FirebirdRange() {
        // Test expectation is tied to the actual error message defined.
        final String expected = "Invalid parameter to FIRST.  Only integers >= 0 are allowed.";

        GDSExceptionHelper.GDSMessage message = GDSExceptionHelper.getMessage(
                ISCConstants.isc_bad_limit_param);

        assertEquals(expected, message.toString());
    }

    @Test
    public void getMessage_noMessageFound() {
        final String expected = "No message for code 1 found.";

        GDSExceptionHelper.GDSMessage message = GDSExceptionHelper.getMessage(1);

        assertEquals(expected, message.toString());
    }

    @Test
    public void getSQLState_JaybirdRange() {
        // Test expectation is tied to the actual state mapping defined.
        final String expected = "HY090";

        String sqlState = GDSExceptionHelper.getSQLState(JaybirdErrorCodes.jb_blobGetSegmentNegative);

        assertEquals(expected, sqlState);
    }

    @Test
    public void getSQLState_FirebirdRange() {
        // Test expectation is tied to the actual state mapping defined.
        final String expected = "27000";

        String sqlState = GDSExceptionHelper.getSQLState(ISCConstants.isc_integ_fail);

        assertEquals(expected, sqlState);
    }
}