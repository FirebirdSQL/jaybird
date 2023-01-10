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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.SimpleWarningMessageCallback;
import org.firebirdsql.gds.ng.wire.AbstractWireOperations;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10WireOperationsTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(10);

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

    protected final SimpleWarningMessageCallback warningCallback = new SimpleWarningMessageCallback();
    private final V10CommonConnectionInfo commonConnectionInfo = commonConnectionInfo();

    protected V10CommonConnectionInfo commonConnectionInfo() {
        return new V10CommonConnectionInfo();
    }

    protected final AbstractWireOperations createDummyWireOperations() throws SQLException {
        return commonConnectionInfo.createDummyWireOperations(warningCallback);
    }

    /**
     * Test if processResponse does not throw an exception if the response does not contain an exception.
     */
    @Test
    public void testProcessResponse_noException() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        wire.processResponse(genericResponse);
    }

    /**
     * Test if processResponse throws the exception in the response if the exception is not a warning.
     */
    @Test
    public void testProcessResponse_exception() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();
        SQLException testException = new SQLException("test");
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, testException);

        SQLException exception = assertThrows(SQLException.class, () -> wire.processResponse(genericResponse));
        assertSame(testException, exception);
    }

    /**
     * Test if processResponse does not throw an exception if the response contains an exception that is warning.
     */
    @Test
    public void testProcessResponse_warning() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();
        SQLException testException = new SQLWarning("test");
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, testException);

        wire.processResponse(genericResponse);
    }

    /**
     * Test if no warning is registered with the callback if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessResponseWarnings_noException() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        wire.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = warningCallback.getWarnings();
        assertEquals(0, warnings.size(), "Expected no warnings to be registered");
    }

    /**
     * Test if no warning is registered with the callback if the response
     * contains an exception that is not a warning.
     */
    @Test
    public void testProcessResponseWarnings_exception() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        SQLException exception = new FbExceptionBuilder().exception(ISCConstants.isc_numeric_out_of_range).toSQLException();
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);
        wire.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = warningCallback.getWarnings();
        assertEquals(0, warnings.size(), "Expected no warnings to be registered");
    }

    /**
     * Test if a warning is registered with the callback if the response
     * contains an exception that is a warning.
     */
    @Test
    public void testProcessResponseWarnings_warning() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        SQLWarning warning = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException(SQLWarning.class);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, warning);
        wire.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = warningCallback.getWarnings();
        assertEquals(Collections.singletonList(warning), warnings,
                "Unexpected warnings registered or no warnings registered");
    }
}
