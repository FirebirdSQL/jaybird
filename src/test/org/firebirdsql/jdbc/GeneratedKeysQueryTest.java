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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GeneratedKeysSupport} and related classes.
 * <p>
 * This test does not test against a database server.
 * </p>
 *
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class GeneratedKeysQueryTest {

    private static final String TEST_INSERT_QUERY = "INSERT INTO GENERATED_KEYS_TBL(NAME, TEXT_VALUE) VALUES (?, ?)";

    @Mock
    private FirebirdDatabaseMetaData dbMetadata;
    @Mock
    private ResultSet columnRs;
    private GeneratedKeysSupport generatedKeysSupport;

    /**
     * Test generated keys for the case of passing {@link Statement#NO_GENERATED_KEYS} with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code false}</li>
     * <li>getQueryString() returns query unmodified</code></li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_noGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Query with NO_GENERATED_KEYS should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Query string should not be modified");
        // In combination with NO_GENERATED_KEYS the column metadata should not be retrieved
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#NO_GENERATED_KEYS} with an INSERT with a RETURNING.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query unmodified</code></li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_noGeneratedKeys_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.NO_GENERATED_KEYS);

        assertTrue(query.generatesKeys(),
                "Query with NO_GENERATED_KEYS, but with RETURNING clause should generate keys");
        assertEquals(testQuery, query.getQueryString(), "Query string should not be modified");
        // In combination with NO_GENERATED_KEYS the metadata should not be retrieved
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with added RETURNING clause containing all columns reported by the
     * databasemetadata with quotes</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_returnGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(3);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
        when(columnRs.getString(4)).thenReturn("ID", "NAME", "TEXT_VALUE");

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\",\"TEXT_VALUE\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a normal INSERT on Firebird 4.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with added RETURNING * clause/li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_returnGeneratedKeys_firebird4() throws SQLException {
        initDefaultGeneratedKeysSupport(4, 0);

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING *";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertThat("Query has RETURNING * clause added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with added RETURNING clause containing all columns reported by the
     * databasemetadata without quotes</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_returnGeneratedKeys_dialect1() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(1);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
        when(columnRs.getString(4)).thenReturn("ID", "NAME", "TEXT_VALUE");

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING ID,NAME,TEXT_VALUE";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a normal INSERT with
     * a table name with whitespace.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with added RETURNING clause containing all columns reported by the
     * databasemetadata</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_returnGeneratedKeys_tableName_with_whitespace() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(3);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED KEYS TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
        when(columnRs.getString(4)).thenReturn("ID", "NAME", "TEXT_VALUE");

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\",\"TEXT_VALUE\"";
        final String testQuery = "INSERT INTO \"GENERATED KEYS TBL\"(NAME, TEXT_VALUE) VALUES (?, ?)";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.RETURN_GENERATED_KEYS);

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(testQuery)),
                startsWith(testQuery),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with an INSERT
     * containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_returnGeneratedKeys_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.RETURN_GENERATED_KEYS);

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertEquals(testQuery, query.getQueryString(),
                "Query string should not be modified as it already includes RETURNING clause");
        // Metadata should never be requested, as the query already has a RETURNING clause
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT with an invalid autGeneratedKeys
     * value (not {@link Statement#NO_GENERATED_KEYS} or {@link Statement#RETURN_GENERATED_KEYS}).
     * <p>
     * <ul>
     * <li>Throws SQLException with sqlstate HY092 (invalid option identifier)</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_invalidAutoGeneratedKeys_value() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        SQLException exception = assertThrows(SQLNonTransientException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, 3));
        assertThat(exception, allOf(
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidGeneratedKeysOption),
                sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_OPTION_IDENTIFIER)));
        // In combination with invalid value for autoGeneratedKeys parameter the metadata should not be retrieved
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT and indexed columns, in order of ordinal position.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query RETURNING clause with columns specified and quoted</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnIndexes() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(3);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
        when(columnRs.getString(4)).thenReturn("ID", "NAME");
        // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
        when(columnRs.getInt(17)).thenReturn(1, 2, 3);

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertTrue(query.generatesKeys(), "Query with columnIndexes should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT and indexed columns, in order of ordinal position.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query RETURNING clause with columns specified unquoted</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnIndexes_dialect1() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(1);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
        when(columnRs.getString(4)).thenReturn("ID", "NAME");
        // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
        when(columnRs.getInt(17)).thenReturn(1, 2, 3);

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING ID,NAME";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertTrue(query.generatesKeys(), "Query with columnIndexes should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT and indexed columns, including a
     * non-existent column index should throw an exception
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query RETURNING clause with only columns with an existing ordinal position</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnIndexes_includingNonExistentIndex() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(3);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
        when(columnRs.getString(4)).thenReturn("ID", "NAME");
        // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
        when(columnRs.getInt(17)).thenReturn(1, 2, 3);

        SQLException exception = assertThrows(SQLNonTransientException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2, 5 }));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysInvalidColumnPosition),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysInvalidColumnPosition, "5", "GENERATED_KEYS_TBL"),
                sqlStateEquals("22023")));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT and indexed columns, in non-ascending order of
     * ordinal position.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query RETURNING clause with columns specified in defined order by ordinal
     * position</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnIndexes_unOrdered() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        prepareConnectionDialectCheck(3);
        // Metadata for table in query will be retrieved
        when(dbMetadata.getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null)).thenReturn(columnRs);
        // We want to return three columns, so for next() three return true, fourth returns false
        when(columnRs.next()).thenReturn(true, true, true, false);
        // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
        when(columnRs.getString(4)).thenReturn("ID", "NAME");
        // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
        when(columnRs.getInt(17)).thenReturn(1, 2, 3);

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"NAME\",\"ID\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 2, 1 });

        assertTrue(query.generatesKeys(), "Query with columnIndexes should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        verify(columnRs).close();
    }

    /**
     * Test generated keys for the case of passing an INSERT containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnIndexes_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, new int[] { 1, 2, 3 });

        assertTrue(query.generatesKeys(), "Query with columnIndexes should generate keys");
        assertEquals(testQuery, query.getQueryString(),
                "Query string should not be modified as it already includes RETURNING clause");
        // Metadata should never be requested, as the query already has a RETURNING clause
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing an INSERT and a null columnIndexes array should throw exception
     */
    @Test
    void testGeneratedKeys_columnIndexes_nullColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        SQLException exception = assertThrows(SQLException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, (int[]) null));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnIndexes"),
                sqlStateEquals("22023")));
        // Metadata should never be requested, as no columns have been specified
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing an INSERT and a empty columnIndexes array should throw exception
     */
    @Test
    void testGeneratedKeys_columnIndexes_emptyColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        SQLException exception = assertThrows(SQLException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[0]));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnIndexes"),
                sqlStateEquals("22023")));
        // Metadata should never be requested, as no columns have been specified
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing a normal INSERT and an array of columnNames.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query RETURNING clause with columns specified</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnNames() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING NAME,ID";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new String[] { "NAME", "ID" });

        assertTrue(query.generatesKeys(), "Query with columnNames should generate keys");
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
        // dbMetaData getColumns will not be accessed for using columnNames
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing an INSERT containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code true}</li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_columnNames_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, new String[] { "ID", "NAME" });

        assertTrue(query.generatesKeys(), "Query with RETURN_GENERATED_KEYS should generate keys");
        assertEquals(testQuery, query.getQueryString(),
                "Query string should not be modified as it already includes RETURNING clause");
        // dbMetaData getColumns will not be accessed for using columnNames
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing an INSERT and a null columnNames array should throw exception
     */
    @Test
    void testGeneratedKeys_columnNames_nullColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        SQLException exception = assertThrows(SQLException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, (String[]) null));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnNames"),
                sqlStateEquals("22023")));
        // dbMetaData getColumns will not be accessed for using columnNames
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing an INSERT and a empty columnNames array should throw exception
     */
    @Test
    void testGeneratedKeys_columnNames_emptyColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        SQLException exception = assertThrows(SQLException.class,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new String[0]));
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnNames"),
                sqlStateEquals("22023")));
        // dbMetaData getColumns will not be accessed for using columnNames
        verifyNoGetColumnsCall();
    }

    /**
     * Test generated keys for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a SELECT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns {@code false}</li>
     * <li>getQueryString() returns query unmodified</code></li>
     * </ul>
     * </p>
     */
    @Test
    void testGeneratedKeys_select_returnGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);

        String testQuery = "select * from rdb$database";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.NO_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Query with SELECT, should not generate keys");
        assertEquals(testQuery, query.getQueryString(), "Query string should not be modified");
        // In combination with SELECT the metadata should not be retrieved
        verifyNoGetColumnsCall();
    }

    // TODO Consider including tests for DELETE, UPDATE, UPDATE OR INSERT and SELECT

    @SuppressWarnings("SameParameterValue")
    private void initDefaultGeneratedKeysSupport(int major, int minor) throws SQLException {
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(major);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(minor);
        generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("default", dbMetadata);
    }

    private void prepareConnectionDialectCheck(final int connectionDialect) throws SQLException {
        lenient().when(dbMetadata.getConnectionDialect()).thenReturn(connectionDialect);
    }

    private void verifyNoGetColumnsCall() throws SQLException {
        verify(dbMetadata, never()).getColumns(anyString(), anyString(), anyString(), anyString());
    }
}
