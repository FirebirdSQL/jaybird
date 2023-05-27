/*
 * Firebird Open Source JDBC Driver
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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link GDSExceptionHelper}.
 *
 * @author Mark Rotteveel
 */
class GDSExceptionHelperTest {

    /**
     * Checks if a message of the Jaybird range of error messages can be retrieved
     */
    @Test
    void getMessage_JaybirdRange() {
        // Test expectation is tied to the actual error message defined.
        final String expected = "getSegment called with sizeRequested (null), should be > 0";

        var message = GDSExceptionHelper.getMessage(JaybirdErrorCodes.jb_blobGetSegmentNegative);

        assertEquals(expected, message.toString());
    }

    /**
     * Checks if a message of the Firebird range of error messages can be retrieved
     */
    @Test
    void getMessage_FirebirdRange() {
        // Test expectation is tied to the actual error message defined.
        final String expected = "Invalid parameter to FETCH or FIRST. Only integers >= 0 are allowed.";

        var message = GDSExceptionHelper.getMessage(ISCConstants.isc_bad_limit_param);

        assertEquals(expected, message.toString());
    }

    @Test
    void getMessage_noMessageFound() {
        final String expected = "No message for code 1 found.";

        var message = GDSExceptionHelper.getMessage(1);

        assertEquals(expected, message.toString());
    }

    @Test
    void getSQLState_JaybirdRange() {
        // Test expectation is tied to the actual state mapping defined.
        final String expected = "HY090";

        String sqlState = GDSExceptionHelper.getSQLState(JaybirdErrorCodes.jb_blobGetSegmentNegative);

        assertEquals(expected, sqlState);
    }

    @Test
    void getSQLState_FirebirdRange() {
        // Test expectation is tied to the actual state mapping defined.
        final String expected = "27000";

        String sqlState = GDSExceptionHelper.getSQLState(ISCConstants.isc_integ_fail);

        assertEquals(expected, sqlState);
    }

    @Test
    void messageWithQuotesAroundParameter() {
        /* NOTE: Reason for this test is that if we were using MessageFormat (we aren't), the current format using
           "something '{0}'" would not work (that would require "something ''{0}''"). So, if we ever switch to using
           MessageFormat or a class using the exact same parse rules as MessageFormat, this test will fail unless the
           message text is fixed. */
        var message = GDSExceptionHelper.getMessage(ISCConstants.isc_ctx_var_not_found);
        message.setParameters(List.of("Parameter 1", "Parameter 2"));

        assertEquals("Context variable 'Parameter 1' is not found in namespace 'Parameter 2'", message.toString());
    }

    @Test
    void extraParametersOfFormattedExceptionIgnored() {
        /* Formatted exception are already formatted on the server, but the server also sends the format parameters to
           the client. The normal rendering includes extra parameters in the message, but here it doesn't make sense */
        var message = GDSExceptionHelper.getMessage(ISCConstants.isc_formatted_exception);
        message.setParameters(List.of("Already formatted exception", "original parameter 1", "original parameter 2"));

        assertEquals("Already formatted exception", message.toString());
    }
}