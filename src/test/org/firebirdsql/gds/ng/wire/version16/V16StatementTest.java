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
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbBatchConfig;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.version15.V15StatementTest;
import org.firebirdsql.jaybird.fb.constants.BatchItems;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.gds.ng.FbBatchConfig.CONTINUE_ON_ERROR;
import static org.firebirdsql.gds.ng.FbBatchConfig.HALT_AT_FIRST_ERROR;
import static org.firebirdsql.gds.ng.FbBatchConfig.SERVER_DEFAULT_BUFFER_SIZE;
import static org.firebirdsql.gds.ng.FbBatchConfig.SERVER_DEFAULT_DETAILED_ERRORS;
import static org.firebirdsql.gds.ng.FbBatchConfig.UPDATE_COUNTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version16.V16Statement} in the V16 protocol, reuses test for V15.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@ExtendWith(MockitoExtension.class)
public class V16StatementTest extends V15StatementTest {

    private static final String aEuro = "a\u20AC";

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(16);

    protected V16CommonConnectionInfo commonConnectionInfo() {
        return new V16CommonConnectionInfo();
    }

    @Test
    void testBatchExecute(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                HALT_AT_FIRST_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        final int rows = 7;
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) ->
                IntStream.rangeClosed(1, rows).mapToObj(
                        id -> RowValue.of(descriptor, coder.encodeInt(id), coder.encodeString(aEuro + id))));
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        BatchCompletion completion = statement.batchExecute();

        verify(createResponse).onResponse(null);
        verify(createResponse, never()).onException(any());
        verify(sendResponse).onResponse(null);
        verify(sendResponse, never()).onException(any());
        assertEquals(rows, completion.elementCount(), "elementCount");
        assertFalse(completion.hasErrors(), "expected no errors");
        int[] expectedUpdateCounts = new int[rows];
        Arrays.fill(expectedUpdateCounts, 1);
        assertArrayEquals(expectedUpdateCounts, completion.updateCounts(), "updateCounts");

        transaction.commit();

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT thekey, theUTFVarcharValue, theUTFCharValue FROM keyvalue order by thekey")) {
            IntStream.rangeClosed(1, rows).forEach(expectedId -> {
                try {
                    assertTrue(rs.next(), "expected a row");
                    assertEquals(expectedId, rs.getInt(1));
                    assertEquals(aEuro + expectedId, rs.getString(2));
                    assertNull(rs.getString(3));
                } catch (SQLException e) {
                    fail(e);
                }
            });
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void testBatchExecuteWithError_singleError(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                HALT_AT_FIRST_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) -> {
            RowValue rowValue1 = RowValue.of(descriptor, coder.encodeInt(1), coder.encodeString(aEuro));
            RowValue rowValue2 = RowValue.of(descriptor, coder.encodeInt(2), coder.encodeString(aEuro));
            // Add rowValue1 three times, should trigger primary key constraint violation
            return Stream.of(rowValue1, rowValue1, rowValue1, rowValue2);
        });
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        BatchCompletion completion = statement.batchExecute();

        assertEquals(2, completion.elementCount(), "elementCount includes successful row and first failure");
        assertTrue(completion.hasErrors(), "expected errors");
        assertArrayEquals(new int[] { 1, BatchItems.BATCH_EXECUTE_FAILED }, completion.updateCounts(), "updateCounts");
        assertEquals(0, completion.simplifiedErrors().length, "expected no simplified errors");
        List<BatchCompletion.DetailedError> detailedErrors = completion.detailedErrors();
        assertEquals(1, detailedErrors.size(), "expected one detailed error");
        BatchCompletion.DetailedError detailedError = detailedErrors.get(0);
        assertEquals(1, detailedError.element(), "expected error about second element (index 1)");
        assertThat(detailedError.error()).hasMessageContaining("violation of PRIMARY or UNIQUE KEY constraint");

        transaction.commit();

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT thekey, theUTFVarcharValue, theUTFCharValue FROM keyvalue order by thekey")) {
            assertTrue(rs.next(), "expected a row");
            assertEquals(1, rs.getInt(1));
            assertEquals(aEuro, rs.getString(2));
            assertNull(rs.getString(3));
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void testBatchExecuteWithError_multiError(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                CONTINUE_ON_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) -> {
            RowValue rowValue1 = RowValue.of(descriptor, coder.encodeInt(1), coder.encodeString(aEuro));
            RowValue rowValue2 = RowValue.of(descriptor, coder.encodeInt(2), coder.encodeString(aEuro));
            // Add rowValue1 three times, should trigger primary key constraint violation
            return Stream.of(rowValue1, rowValue1, rowValue1, rowValue2);
        });
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        BatchCompletion completion = statement.batchExecute();

        assertEquals(4, completion.elementCount(), "elementCount includes all elements");
        assertTrue(completion.hasErrors(), "expected errors");
        int[] expectedUpdateCounts = { 1, BatchItems.BATCH_EXECUTE_FAILED, BatchItems.BATCH_EXECUTE_FAILED, 1 };
        assertArrayEquals(expectedUpdateCounts, completion.updateCounts(), "updateCounts");
        assertEquals(0, completion.simplifiedErrors().length, "expected no simplified errors");
        List<BatchCompletion.DetailedError> detailedErrors = completion.detailedErrors();
        assertEquals(2, detailedErrors.size(), "expected two detailed errors");
        IntStream.rangeClosed(0, 1).forEach(errorIndex -> {
            BatchCompletion.DetailedError detailedError = detailedErrors.get(errorIndex);
            assertEquals(errorIndex + 1, detailedError.element(), "expected error for element " + (errorIndex + 1));
            assertThat(detailedError.error()).hasMessageContaining("violation of PRIMARY or UNIQUE KEY constraint");
        });

        transaction.commit();

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT thekey, theUTFVarcharValue, theUTFCharValue FROM keyvalue order by thekey")) {
            IntStream.rangeClosed(1, 2).forEach(expectedId -> {
                try {
                    assertTrue(rs.next(), "expected a row");
                    assertEquals(expectedId, rs.getInt(1));
                    assertEquals(aEuro, rs.getString(2));
                    assertNull(rs.getString(3));
                } catch (SQLException e) {
                    fail(e);
                }
            });
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void testBatchExecuteWithError_multiError_noDetailedErrors(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                CONTINUE_ON_ERROR, UPDATE_COUNTS, 0, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) -> {
            RowValue rowValue1 = RowValue.of(descriptor, coder.encodeInt(1), coder.encodeString(aEuro));
            RowValue rowValue2 = RowValue.of(descriptor, coder.encodeInt(2), coder.encodeString(aEuro));
            // Add rowValue1 three times, should trigger primary key constraint violation
            return Stream.of(rowValue1, rowValue1, rowValue1, rowValue2);
        });
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        BatchCompletion completion = statement.batchExecute();

        assertEquals(4, completion.elementCount(), "elementCount includes all elements");
        assertTrue(completion.hasErrors(), "expected errors");
        int[] expectedUpdateCounts = { 1, BatchItems.BATCH_EXECUTE_FAILED, BatchItems.BATCH_EXECUTE_FAILED, 1 };
        assertArrayEquals(expectedUpdateCounts, completion.updateCounts(), "updateCounts");
        assertEquals(2, completion.simplifiedErrors().length, "expected no simplified errors");
        assertArrayEquals(new int[] { 1, 2 }, completion.simplifiedErrors(),
                "expected simplified error for elements 1 and 2");
        List<BatchCompletion.DetailedError> detailedErrors = completion.detailedErrors();
        assertEquals(0, detailedErrors.size(), "expected no detailed errors");

        transaction.commit();

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT thekey, theUTFVarcharValue, theUTFCharValue FROM keyvalue order by thekey")) {
            IntStream.rangeClosed(1, 2).forEach(expectedId -> {
                try {
                    assertTrue(rs.next(), "expected a row");
                    assertEquals(expectedId, rs.getInt(1));
                    assertEquals(aEuro, rs.getString(2));
                    assertNull(rs.getString(3));
                } catch (SQLException e) {
                    fail(e);
                }
            });
            assertFalse(rs.next(), "expected no more rows");
        }
    }

    @Test
    void testCancelBatchFollowedByExecute(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                HALT_AT_FIRST_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        final int rows = 7;
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) ->
                IntStream.rangeClosed(1, rows).mapToObj(
                        id -> RowValue.of(descriptor, coder.encodeInt(id), coder.encodeString(aEuro + id))));
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        statement.batchCancel();
        BatchCompletion completion = statement.batchExecute();

        verify(createResponse).onResponse(null);
        verify(createResponse, never()).onException(any());
        verify(sendResponse).onResponse(null);
        verify(sendResponse, never()).onException(any());
        assertEquals(0, completion.elementCount(), "expected zero element count (batch should be empty");
        assertFalse(completion.hasErrors(), "expected no errors");
        assertArrayEquals(new int[0], completion.updateCounts(), "updateCounts");

        transaction.commit();

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT thekey, theUTFVarcharValue, theUTFCharValue FROM keyvalue order by thekey")) {
            assertFalse(rs.next(), "expected no rows");
        }
    }

    @Test
    void testBatchTooLarge(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse) throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            // table with maximum row size
            DdlHelper.executeDDL(connection, "create table max_sized(col1 binary(32766), col2 binary(32765))");
            allocateStatement();
            FbTransaction transaction = getOrCreateTransaction();
            statement.prepare("insert into max_sized (col1, col2) values (?, ?)");
            // Actual in-memory buffer will accommodate 2 rows, even if the specified size is less than the message size
            FbBatchConfig config = FbBatchConfig.of(
                    HALT_AT_FIRST_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, 128 * 1024);
            statement.deferredBatchCreate(config, createResponse);
            final int rows = 3;
            List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) -> {
                RowValue rowValue = RowValue.of(descriptor, new byte[32766], new byte[32765]);
                return IntStream.range(0, rows).mapToObj(i -> rowValue);
            });
            statement.deferredBatchSend(valuesToInsert, sendResponse);
            BatchCompletion completion = statement.batchExecute();

            verify(sendResponse, never()).onResponse(null);
            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            verify(sendResponse).onException(exceptionCaptor.capture());
            assertThat(exceptionCaptor.getValue()).isInstanceOf(SQLException.class)
                    .hasMessageContaining("Internal buffer overflow - batch too big");
            // Nothing should have been executed
            assertEquals(0, completion.elementCount());

            transaction.commit();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("select count(*) from max_sized")) {
                assertTrue(rs.next(), "expected row");
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void testBatchRelease(
            @Mock DeferredResponse<Void> createResponse, @Mock DeferredResponse<Void> sendResponse,
            @Mock DeferredResponse<Void> releaseResponse) throws Exception {
        allocateStatement();
        FbTransaction transaction = getOrCreateTransaction();
        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, NULL)");
        FbBatchConfig config = FbBatchConfig.of(
                HALT_AT_FIRST_ERROR, UPDATE_COUNTS, SERVER_DEFAULT_DETAILED_ERRORS, SERVER_DEFAULT_BUFFER_SIZE);
        statement.deferredBatchCreate(config, createResponse);
        List<RowValue> valuesToInsert = generateRowValues((descriptor, coder) ->
                Stream.of(RowValue.of(descriptor, coder.encodeInt(1), coder.encodeString(aEuro))));
        statement.deferredBatchSend(valuesToInsert, sendResponse);
        statement.batchExecute();
        transaction.commit();

        Mockito.<DeferredResponse<?>>reset(sendResponse);

        statement.deferredBatchRelease(releaseResponse);
        // Force deferred response to be sent (not strictly necessary, but allows early verification on releaseResponse)
        db.getDatabaseInfo(new byte[] { ISCConstants.isc_info_end }, 1);

        verify(releaseResponse).onResponse(null);
        verify(releaseResponse, never()).onException(any());

        // Verify by trying to execute another batch
        List<RowValue> valuesToInsert2 = generateRowValues((descriptor, coder) ->
                Stream.of(RowValue.of(descriptor, coder.encodeInt(2), coder.encodeString(aEuro))));
        statement.deferredBatchSend(valuesToInsert2, sendResponse);
        // Force deferred response to be sent
        db.getDatabaseInfo(new byte[] { ISCConstants.isc_info_end }, 1);

        verify(sendResponse, never()).onResponse(any());
        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(sendResponse).onException(exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue()).isInstanceOf(SQLException.class)
                .hasMessageContaining("invalid batch handle");
    }

    private List<RowValue> generateRowValues(
            BiFunction<RowDescriptor, DatatypeCoder, Stream<RowValue>> rowValueGenerator) {
        EncodingDefinition utf8 = db.getEncodingFactory().getEncodingDefinitionByFirebirdName("UTF8");
        DatatypeCoder datatypeCoder = db.getDatatypeCoder().forEncodingDefinition(utf8);
        RowDescriptor parametersInsert = statement.getParameterDescriptor();
        return rowValueGenerator.apply(parametersInsert, datatypeCoder).collect(toList());
    }

}
