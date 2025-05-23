// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.sql.*;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FbExceptionBuilder}
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
class FbExceptionBuilderTest {

    private final FbExceptionBuilder builder = new FbExceptionBuilder();

    /**
     * Test simple use of {@link FbExceptionBuilder#exception(int)}.
     */
    @Test
    void exception_Simple() {
        SQLException result = builder.exception(isc_req_wrong_db).toSQLException();

        assertNotNull(result);
        assertEquals(SQLException.class, result.getClass(), "Expected exception to be SQLException (no subclass!)");
        assertEquals(isc_req_wrong_db, result.getErrorCode(), "Unexpected errorCode");
        assertEquals("HY000", result.getSQLState(), "Unexpected SQLState");
        assertEquals("request referenced an unavailable database [SQLState:HY000, ISC error code:335544365]",
                result.getMessage(), "Unexpected message");
        assertNull(result.getCause(), "Expected no cause set");
    }

    /**
     * Test simple use of {@link FbExceptionBuilder#warning(int)}.
     */
    @Test
    void warning_Simple() {
        SQLException result = builder.warning(isc_unavailable).toSQLException();

        assertNotNull(result);
        assertEquals(SQLWarning.class, result.getClass(), "Expected exception to be SQLWarning (no subclass!)");
        assertEquals(isc_unavailable, result.getErrorCode(), "Unexpected errorCode");
        assertEquals("08001", result.getSQLState(), "Unexpected SQLState");
        assertEquals("unavailable database [SQLState:08001, ISC error code:335544375]", result.getMessage(),
                "Unexpected message");
        assertNull(result.getCause(), "Expected no cause set");
    }

    /**
     * Test simple use of {@link FbExceptionBuilder#timeoutException(int)}.
     */
    @Test
    void timeoutException_Simple() {
        SQLException result = builder.timeoutException(isc_net_connect_err).toSQLException();

        assertNotNull(result);
        assertEquals(SQLTimeoutException.class, result.getClass(),
                "Expected exception to be SQLTimeoutException (no subclass!)");
        assertEquals(isc_net_connect_err, result.getErrorCode(), "Unexpected errorCode");
        assertEquals("08006", result.getSQLState(), "Unexpected SQLState");
        assertEquals("Failed to establish a connection. [SQLState:08006, ISC error code:335544722]",
                result.getMessage(), "Unexpected message");
        assertNull(result.getCause(), "Expected no cause set");
    }

    /**
     * Test use of {@link FbExceptionBuilder#exception(int)} with an error code associated with an SQLState 0A000 to
     * test if an {@link SQLFeatureNotSupportedException} is created.
     */
    @Test
    void exception_featureNotSupported() {
        SQLException result = builder.exception(isc_wish_list).toSQLException();

        assertNotNull(result);
        assertEquals(SQLFeatureNotSupportedException.class,
                result.getClass(), "Expected exception to be SQLFeatureNotSupportedException (no subclass!)");
        assertEquals(isc_wish_list, result.getErrorCode(), "Unexpected errorCode");
        assertEquals("0A000", result.getSQLState(), "Unexpected SQLState");
        assertEquals("feature is not supported [SQLState:0A000, ISC error code:335544378]", result.getMessage(),
                "Unexpected message");
        assertNull(result.getCause(), "Expected no cause set");
    }

    /**
     * Tests parameter substitution in exception messages.
     */
    @Test
    void exception_parameterSubstitution() {
        builder.exception(isc_wrong_ods).messageParameter("the filename");
        builder.messageParameter(11).messageParameter(2);
        builder.messageParameter(10).messageParameter(0);

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals("unsupported on-disk structure for file the filename; found 11.2, support 10.0 [SQLState:HY000, ISC error code:335544379]",
                result.getMessage(), "Unexpected message with parameter substitution");
    }

    /**
     * Test incomplete parameter substitution in exception messages.
     */
    @Test
    void exception_parameterIncompleteSubstitution() {
        builder.exception(isc_wrong_ods).messageParameter("the filename");
        builder.messageParameter(11).messageParameter(2);

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals("unsupported on-disk structure for file the filename; found 11.2, support (null).(null) [SQLState:HY000, ISC error code:335544379]",
                result.getMessage(), "Unexpected message with parameter substitution");
    }

    /**
     * Test setting cause of exception.
     */
    @Test
    void exception_cause() {
        Throwable throwable = new IOException("the message");

        builder.exception(isc_req_wrong_db);
        builder.cause(throwable);
        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertSame(throwable, result.getCause(), "Unexpected exception cause");
    }

    /**
     * Tests if override the SQLState works.
     */
    @Test
    void exception_overrideSQLState_valid() {
        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        builder.sqlState("42000");

        SQLException result = builder.toSQLException();

        assertNotNull(result);
        assertEquals(isc_req_wrong_db, result.getErrorCode(), "Unexpected errorCode");
        assertEquals("42000", result.getSQLState(), "Unexpected SQLState");
    }

    /**
     * Tests if overriding the SQLState throws an {@link NullPointerException} if null is passed
     */
    @Test
    void exception_overrideSQLState_null() {
        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        assertThrows(NullPointerException.class, () -> builder.sqlState(null));
    }

    /**
     * Tests if overriding the SQLState throws an {@link IllegalArgumentException} if a string is passed
     * that is not 5 characters.
     */
    @Test
    void exception_overrideSQLState_not5Characters() {
        builder.exception(isc_req_wrong_db).toSQLException();
        // isc_req_wrong_db defaults to SQLState HY000
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> builder.sqlState("4200"));
        assertThat(exception, message(equalTo("Value of sqlState must be a 5 character string")));
    }

    /**
     * Test exception chain produced with builder.
     */
    @Test
    void exception_chaining() {
        // Exception one
        builder.exception(isc_req_wrong_db);
        // Exception two
        builder.exception(isc_wish_list);

        SQLException result = builder.toSQLException();
        assertNotNull(result);

        // Exception one
        assertEquals(SQLException.class, result.getClass(), "Expected exception to be SQLException (no subclass!)");
        assertEquals(isc_req_wrong_db, result.getErrorCode(), "Unexpected errorCode");
        // Exception two
        SQLException result2 = result.getNextException();
        assertNotNull(result2);
        assertEquals(SQLFeatureNotSupportedException.class, result2.getClass(), "Expected exception to be SQLFeatureNotSupportedException (no subclass!)");
        assertEquals(isc_wish_list, result2.getErrorCode(), "Unexpected errorCode");
        // No further results
        assertNull(result2.getNextException(), "Expected no further exceptions");
    }

    /**
     * Test call to {@link FbExceptionBuilder#toSQLException(Class)} works if
     * the produced exception is of the specified type.
     */
    @Test
    void toSQLException_withCasting_valid() {
        builder.warning(isc_req_wrong_db);

        SQLWarning warning = builder.toSQLException(SQLWarning.class);
        assertNotNull(warning, "Expected non-null warning");
    }

    /**
     * Test call to {@link FbExceptionBuilder#toSQLException(Class)} fails if
     * the produced exception is not of the specified type.
     */
    @Test
    void toSQLException_withCasting_invalid() {
        builder.exception(isc_req_wrong_db);

        assertThrows(ClassCastException.class, () -> builder.toSQLException(SQLWarning.class));
    }

    /**
     * Calling {@link FbExceptionBuilder#sqlState(String)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    void SQLState_ThrowsIllegalState() {
        assertIllegalStateForUninitializedExceptionType(() -> builder.sqlState("0A000"));
    }

    /**
     * Calling {@link FbExceptionBuilder#messageParameter(int)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    void messageParameter_int_ThrowsIllegalState() {
        assertIllegalStateForUninitializedExceptionType(() -> builder.messageParameter(5));
    }

    /**
     * Calling {@link FbExceptionBuilder#messageParameter(Object)} before the exception type and code has been set
     * should result in an {@link IllegalStateException}
     */
    @Test
    void messageParameter_String_ThrowsIllegalState() {
        assertIllegalStateForUninitializedExceptionType(() -> builder.messageParameter(""));
    }

    @Test
    void toSQLException_empty_throwsIllegalState() {
        assertIllegalStateForEmptyBuilder(builder::toSQLException);
    }

    @Test
    void toFlatSQLException_empty_throwsIllegalState() {
        assertIllegalStateForEmptyBuilder(builder::toFlatSQLException);
    }

    @Test
    void forException() {
        SQLException result = FbExceptionBuilder.forException(isc_req_wrong_db).toSQLException();

        assertNotNull(result);
        assertEquals(SQLException.class, result.getClass(), "Expected exception to be SQLException (no subclass!)");
        // Rest covered by test exception_Simple
    }

    @Test
    void forWarning() {
        SQLException result = FbExceptionBuilder.forWarning(isc_unavailable).toSQLException();

        assertNotNull(result);
        assertEquals(SQLWarning.class, result.getClass(), "Expected exception to be SQLWarning (no subclass!)");
        // Rest covered by test warning_Simple
    }

    @Test
    void exceptionTypeUpgrade_EXCEPTION_to_NON_TRANSIENT() {
        SQLException result = FbExceptionBuilder.forException(isc_login).toFlatSQLException();

        assertNotNull(result);
        // Definition to map isc_login to SQLInvalidAuthorizationSpecException is part of NON_TRANSIENT
        assertEquals(SQLInvalidAuthorizationSpecException.class, result.getClass(),
                "Expected result to be SQLInvalidAuthorizationSpecException (no subclass!)");
    }

    @Test
    void toSQLExceptionStripsBuilderStackTraceElements() {
        SQLException result = FbExceptionBuilder.forException(isc_login).toSQLException();

        assertEquals(getClass().getName(), result.getStackTrace()[0].getClassName(),
                "Expected first StackTraceElement to be from this class");
    }

    @Test
    void toFlatSQLExceptionStripsBuilderStackTraceElements() {
        SQLException result = FbExceptionBuilder.forException(isc_login).toFlatSQLException();

        assertEquals(getClass().getName(), result.getStackTrace()[0].getClassName(),
                "Expected first StackTraceElement to be from this class");
    }

    /**
     * Helper method to assert exception for an uninitialized exception type.
     */
    private void assertIllegalStateForUninitializedExceptionType(Executable executable) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
        assertThat(exception, message(equalTo("FbExceptionBuilder requires call to warning() or exception() first")));
    }

    private void assertIllegalStateForEmptyBuilder(Executable executable) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
        assertThat(exception, message(equalTo("No information available to build an SQLException")));
    }
}
