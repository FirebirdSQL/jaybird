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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.SimpleWarningMessageCallback;
import org.firebirdsql.gds.ng.wire.AbstractWireOperations;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10WireOperations {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(10);

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludesNativeOnly();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected final SimpleWarningMessageCallback warningCallback = new SimpleWarningMessageCallback();
    private final V10CommonConnectionInfo commonConnectionInfo;

    public TestV10WireOperations() {
        this(new V10CommonConnectionInfo());
    }

    protected TestV10WireOperations(V10CommonConnectionInfo commonConnectionInfo) {
        this.commonConnectionInfo = commonConnectionInfo;
    }

    protected final AbstractWireOperations createDummyWireOperations() throws SQLException {
        return commonConnectionInfo.createDummyWireOperations(warningCallback);
    }

    /**
     * Test if processResponse does not throw an exception if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessResponse_noException() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        wire.processResponse(genericResponse);
    }

    /**
     * Test if processResponse throws the exception in the response if the
     * exception is not a warning.
     */
    @Test
    public void testProcessResponse_exception() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();
        SQLException exception = new FbExceptionBuilder().exception(ISCConstants.isc_numeric_out_of_range).toSQLException();
        expectedException.expect(sameInstance(exception));

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

        wire.processResponse(genericResponse);
    }

    /**
     * Test if processResponse does not throw an exception if the response
     * contains an exception that is warning.
     */
    @Test
    public void testProcessResponse_warning() throws Exception {
        AbstractWireOperations wire = createDummyWireOperations();

        SQLException exception = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException();
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

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
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
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
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
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
        assertEquals("Unexpected warnings registered or no warnings registered", Collections.singletonList(warning),
                warnings);
    }
}
