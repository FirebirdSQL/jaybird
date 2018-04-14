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
package org.firebirdsql.gds.ng;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.sql.*;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link FbExceptionBuilder}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class TestFbExceptionBuilder {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * Test simple use of {@link FbExceptionBuilder#exception(int)}.
     */
    @Test
    public void exception_Simple() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        SQLException result = builder.exception(isc_req_wrong_db).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLException (no subclass!)", SQLException.class, result.getClass());
        assertEquals("Unexpected errorCode", isc_req_wrong_db, result.getErrorCode());
        assertEquals("Unexpected SQLState", "HY000", result.getSQLState());
        assertEquals("Unexpected message",
                "request referenced an unavailable database [SQLState:HY000, ISC error code:335544365]",
                result.getMessage());
        assertNull("Expected no cause set", result.getCause());
    }

    /**
     * Test simple use of {@link FbExceptionBuilder#warning(int)}.
     */
    @Test
    public void warning_Simple() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        SQLException result = builder.warning(isc_unavailable).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLWarning (no subclass!)", SQLWarning.class, result.getClass());
        assertEquals("Unexpected errorCode", isc_unavailable, result.getErrorCode());
        assertEquals("Unexpected SQLState", "08001", result.getSQLState());
        assertEquals("Unexpected message",
                "unavailable database [SQLState:08001, ISC error code:335544375]", result.getMessage());
        assertNull("Expected no cause set", result.getCause());
    }

    /**
     * Test simple use of {@link FbExceptionBuilder#timeoutException(int)}.
     */
    @Test
    public void timeoutException_Simple() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        SQLException result = builder.timeoutException(isc_net_connect_err).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLTimeoutException (no subclass!)",
                SQLTimeoutException.class, result.getClass());
        assertEquals("Unexpected errorCode", isc_net_connect_err, result.getErrorCode());
        assertEquals("Unexpected SQLState", "08006", result.getSQLState());
        assertEquals("Unexpected message",
                "Failed to establish a connection. [SQLState:08006, ISC error code:335544722]", result.getMessage());
        assertNull("Expected no cause set", result.getCause());
    }

    /**
     * Test use of {@link FbExceptionBuilder#exception(int)} with an error code associated with an SQLState 0A000 to
     * test
     * if an {@link SQLFeatureNotSupportedException} is created.
     */
    @Test
    public void exception_featureNotSupported() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        SQLException result = builder.exception(isc_wish_list).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLFeatureNotSupportedException (no subclass!)",
                SQLFeatureNotSupportedException.class, result.getClass());
        assertEquals("Unexpected errorCode", isc_wish_list, result.getErrorCode());
        assertEquals("Unexpected SQLState", "0A000", result.getSQLState());
        assertEquals("Unexpected message",
                "feature is not supported [SQLState:0A000, ISC error code:335544378]", result.getMessage());
        assertNull("Expected no cause set", result.getCause());
    }

    /**
     * Tests parameter substitution in exception messages.
     */
    @Test
    public void exception_parameterSubstitution() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_wrong_ods).messageParameter("the filename");
        builder.messageParameter(11).messageParameter(2);
        builder.messageParameter(10).messageParameter(0);

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals("Unexpected message with parameter substitution",
                "unsupported on-disk structure for file the filename; found 11.2, support 10.0 [SQLState:HY000, ISC error code:335544379]",
                result.getMessage());
    }

    /**
     * Test incomplete parameter substitution in exception messages.
     * TODO: Move to {@link org.firebirdsql.gds.GDSExceptionHelper}?
     */
    @Test
    public void exception_parameterIncompleteSubstitution() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_wrong_ods).messageParameter("the filename");
        builder.messageParameter(11).messageParameter(2);

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals("Unexpected message with parameter substitution",
                "unsupported on-disk structure for file the filename; found 11.2, support (null).(null) [SQLState:HY000, ISC error code:335544379]",
                result.getMessage());
    }

    /**
     * Test setting cause of exception.
     */
    @Test
    public void exception_cause() {
        FbExceptionBuilder builder = new FbExceptionBuilder();
        Throwable throwable = new IOException("the message");

        builder.exception(isc_req_wrong_db);
        builder.cause(throwable);
        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertSame("Unexpected exception cause", throwable, result.getCause());
    }

    /**
     * Tests if override the SQLState works.
     */
    @Test
    public void exception_overrideSQLState_valid() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        builder.sqlState("42000");

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals("Unexpected errorCode", isc_req_wrong_db, result.getErrorCode());
        assertEquals("Unexpected SQLState", "42000", result.getSQLState());
    }

    /**
     * Tests if overriding the SQLState throws an {@link IllegalArgumentException} if null is passed
     */
    @Test
    public void exception_overrideSQLState_null() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Value of sqlState must be a 5 character string");
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        builder.sqlState(null);
    }

    /**
     * Tests if overriding the SQLState throws an {@link IllegalArgumentException} if a string is passed
     * that is not 5 characters.
     */
    @Test
    public void exception_overrideSQLState_not5Characters() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Value of sqlState must be a 5 character string");
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        builder.sqlState("4200");
    }

    /**
     * Test exception chain produced with builder.
     */
    @Test
    public void exception_chaining() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        // Exception one
        builder.exception(isc_req_wrong_db);
        // Exception two
        builder.exception(isc_wish_list);

        SQLException result = builder.toSQLException();
        assertNotNull(result);

        // Exception one
        assertEquals("Expected exception to be SQLException (no subclass!)", SQLException.class, result.getClass());
        assertEquals("Unexpected errorCode", isc_req_wrong_db, result.getErrorCode());
        // Exception two
        SQLException result2 = result.getNextException();
        assertNotNull(result2);
        assertEquals("Expected exception to be SQLFeatureNotSupportedException (no subclass!)", SQLFeatureNotSupportedException.class, result2.getClass());
        assertEquals("Unexpected errorCode", isc_wish_list, result2.getErrorCode());
        // No further results
        assertNull("Expected no further exceptions", result2.getNextException());
    }

    /**
     * Test call to {@link FbExceptionBuilder#toSQLException(Class)} works if
     * the produced exception is of the specified type.
     */
    @Test
    public void toSQLException_withCasting_valid() {
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.warning(isc_req_wrong_db);

        SQLWarning warning = builder.toSQLException(SQLWarning.class);
        assertNotNull("Expected non-null warning", warning);
    }

    /**
     * Test call to {@link FbExceptionBuilder#toSQLException(Class)} fails if
     * the produced exception is not of the specified type.
     */
    @Test
    public void toSQLException_withCasting_invalid() {
        exceptionRule.expect(ClassCastException.class);
        FbExceptionBuilder builder = new FbExceptionBuilder();

        builder.exception(isc_req_wrong_db);

        builder.toSQLException(SQLWarning.class);
    }

    /**
     * Calling {@link FbExceptionBuilder#sqlState(String)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    public void SQLState_ThrowsIllegalState() {
        expectIllegalStateForUninitializedExceptionType();

        FbExceptionBuilder builder = new FbExceptionBuilder();
        builder.sqlState("0A000");
    }

    /**
     * Calling {@link FbExceptionBuilder#messageParameter(int)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    public void messageParameter_int_ThrowsIllegalState() {
        expectIllegalStateForUninitializedExceptionType();

        FbExceptionBuilder builder = new FbExceptionBuilder();
        builder.messageParameter(5);
    }

    /**
     * Calling {@link FbExceptionBuilder#messageParameter(String)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    public void messageParameter_String_ThrowsIllegalState() {
        expectIllegalStateForUninitializedExceptionType();

        FbExceptionBuilder builder = new FbExceptionBuilder();
        builder.messageParameter("");
    }

    @Test
    public void toSQLException_empty_throwsIllegalState() {
        expectIllegalStateForEmptyBuilder();

        FbExceptionBuilder builder = new FbExceptionBuilder();
        builder.toSQLException();
    }

    @Test
    public void toFlatSQLException_empty_throwsIllegalState() {
        expectIllegalStateForEmptyBuilder();

        FbExceptionBuilder builder = new FbExceptionBuilder();
        builder.toFlatSQLException();
    }

    @Test
    public void forException() {
        SQLException result = FbExceptionBuilder.forException(isc_req_wrong_db).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLException (no subclass!)", SQLException.class, result.getClass());
        // Rest covered by test exception_Simple
    }

    @Test
    public void forWarning() {
        SQLException result = FbExceptionBuilder.forWarning(isc_unavailable).toSQLException();

        assertNotNull(result);
        assertEquals("Expected exception to be SQLWarning (no subclass!)", SQLWarning.class, result.getClass());
        // Rest covered by test warning_Simple
    }

    @Test
    public void exceptionTypeUpgrade_EXCEPTION_to_NON_TRANSIENT() {
        SQLException result = FbExceptionBuilder.forException(isc_login).toFlatSQLException();

        assertNotNull(result);
        // Definition to map isc_login to SQLInvalidAuthorizationSpecException is part of NON_TRANSIENT
        assertEquals("Expected result to be SQLInvalidAuthorizationSpecException (no subclass!)",
                SQLInvalidAuthorizationSpecException.class, result.getClass());
    }

    /**
     * Helper method to set the expected exception for an uninitialized exception type.
     */
    private void expectIllegalStateForUninitializedExceptionType() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("FbExceptionBuilder requires call to warning() or exception() first");
    }

    private void expectIllegalStateForEmptyBuilder() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("No information available to build an SQLException");
    }
}
