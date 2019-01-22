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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link GeneratedKeysSupport} and related classes.
 * <p>
 * This test does not test against a database server.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestGeneratedKeysQuery {

    private static final String TEST_INSERT_QUERY = "INSERT INTO GENERATED_KEYS_TBL(NAME, TEXT_VALUE) VALUES (?, ?)";

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
    public void testGeneratedKeys_noGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // In combination with NO_GENERATED_KEYS the column metadata should not be retrieved
        expectNoGetColumnsCall();

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse("Query with NO_GENERATED_KEYS should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY, query.getQueryString());
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
    public void testGeneratedKeys_noGeneratedKeys_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // In combination with NO_GENERATED_KEYS the metadata should not be retrieved
        expectNoGetColumnsCall();

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.NO_GENERATED_KEYS);

        assertTrue("Query with NO_GENERATED_KEYS, but with RETURNING clause should generate keys",
                query.generatesKeys());
        assertEquals("Query string should not be modified", testQuery, query.getQueryString());
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
    public void testGeneratedKeys_returnGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(3);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));
            // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
            exactly(3).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME"), returnValue("TEXT_VALUE")));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\",\"TEXT_VALUE\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_returnGeneratedKeys_firebird4() throws SQLException {
        initDefaultGeneratedKeysSupport(4, 0);
        expectNoGetColumnsCall();

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING *";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING * clause added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_returnGeneratedKeys_dialect1() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(1);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));
            // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
            exactly(3).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME"), returnValue("TEXT_VALUE")));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING ID,NAME,TEXT_VALUE";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_returnGeneratedKeys_tableName_with_whitespace() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(3);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED KEYS TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true), returnValue(false)));
            // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
            exactly(3).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME"), returnValue("TEXT_VALUE")));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\",\"TEXT_VALUE\"";
        final String testQuery = "INSERT INTO \"GENERATED KEYS TBL\"(NAME, TEXT_VALUE) VALUES (?, ?)";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.RETURN_GENERATED_KEYS);

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(testQuery)),
                startsWith(testQuery),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_returnGeneratedKeys_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // Metadata should never be requested, as the query already has a RETURNING clause
        expectNoGetColumnsCall();

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.RETURN_GENERATED_KEYS);

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
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
    public void testGeneratedKeys_invalidAutoGeneratedKeys_value() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // In combination with invalid value for autoGeneratedKeys parameter the metadata should not be retrieved
        expectNoGetColumnsCall();

        expectedException.expect(allOf(
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidGeneratedKeysOption),
                sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_OPTION_IDENTIFIER)));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, 3);
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
    public void testGeneratedKeys_columnIndexes() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(3);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                    returnValue(false)));
            // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
            exactly(2).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
            // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
            exactly(3).of(columnRs).getInt(17);
            will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"ID\",\"NAME\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_columnIndexes_dialect1() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(1);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                    returnValue(false)));
            // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
            exactly(2).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
            // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
            exactly(3).of(columnRs).getInt(17);
            will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING ID,NAME";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_columnIndexes_includingNonExistentIndex() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(3);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                    returnValue(false)));
            // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
            exactly(2).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
            // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
            exactly(3).of(columnRs).getInt(17);
            will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
            oneOf(columnRs).close();
        }});

        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysInvalidColumnPosition),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysInvalidColumnPosition, "5", "GENERATED_KEYS_TBL"),
                sqlStateEquals("22023")
        ));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2, 5 });
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
    public void testGeneratedKeys_columnIndexes_unOrdered() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        expectConnectionDialectCheck(3);
        context.checking(new Expectations() {{
            // Metadata for table in query will be retrieved
            oneOf(dbMetadata).getColumns(null, null, "GENERATED\\_KEYS\\_TBL", null);
            will(returnValue(columnRs));
            // We want to return three columns, so for next() three return true, fourth returns false
            exactly(4).of(columnRs).next();
            will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                    returnValue(false)));
            // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice
            exactly(2).of(columnRs).getString(4);
            will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
            // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
            exactly(3).of(columnRs).getInt(17);
            will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
            oneOf(columnRs).close();
        }});

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING \"NAME\",\"ID\"";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 2, 1 });

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_columnIndexes_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // Metadata should never be requested, as the query already has a RETURNING clause
        expectNoGetColumnsCall();

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, new int[] { 1, 2, 3 });

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    /**
     * Test generated keys for the case of passing an INSERT and a null columnIndexes array should throw exception
     */
    @Test
    public void testGeneratedKeys_columnIndexes_nullColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // Metadata should never be requested, as no columns have been specified
        expectNoGetColumnsCall();

        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnIndexes"),
                sqlStateEquals("22023")
        ));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, (int[]) null);
    }

    /**
     * Test generated keys for the case of passing an INSERT and a empty columnIndexes array should throw exception
     */
    @Test
    public void testGeneratedKeys_columnIndexes_emptyColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // Metadata should never be requested, as no columns have been specified
        expectNoGetColumnsCall();

        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnIndexes"),
                sqlStateEquals("22023")
        ));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[0]);
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
    public void testGeneratedKeys_columnNames() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // dbMetaData getColumns will not be accessed for using columnNames
        expectNoGetColumnsCall();

        // NOTE Implementation detail
        final String expectedSuffix = "\nRETURNING NAME,ID";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new String[] { "NAME", "ID" });

        assertTrue("Query with columnNames should generate keys", query.generatesKeys());
        assertThat("Query has RETURNING clauses added", query.getQueryString(), allOf(
                not(equalTo(TEST_INSERT_QUERY)),
                startsWith(TEST_INSERT_QUERY),
                endsWith(expectedSuffix)));
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
    public void testGeneratedKeys_columnNames_withReturning() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // dbMetaData getColumns will not be accessed for using columnNames
        expectNoGetColumnsCall();

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, new String[] { "ID", "NAME" });

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    /**
     * Test generated keys for the case of passing an INSERT and a null columnNames array should throw exception
     */
    @Test
    public void testGeneratedKeys_columnNames_nullColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // dbMetaData getColumns will not be accessed for using columnNames
        expectNoGetColumnsCall();

        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnNames"),
                sqlStateEquals("22023")
        ));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, (String[]) null);
    }

    /**
     * Test generated keys for the case of passing an INSERT and a empty columnNames array should throw exception
     */
    @Test
    public void testGeneratedKeys_columnNames_emptyColumns() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // dbMetaData getColumns will not be accessed for using columnNames
        expectNoGetColumnsCall();

        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull),
                fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysArrayEmptyOrNull, "columnNames"),
                sqlStateEquals("22023")
        ));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new String[0]);
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
    public void testGeneratedKeys_select_returnGeneratedKeys() throws SQLException {
        initDefaultGeneratedKeysSupport(3, 0);
        // In combination with SELECT the metadata should not be retrieved
        expectNoGetColumnsCall();

        String testQuery = "select * from rdb$database";

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(testQuery, Statement.NO_GENERATED_KEYS);

        assertFalse("Query with SELECT, should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", testQuery, query.getQueryString());
    }

    // TODO Consider including tests for DELETE, UPDATE, UPDATE OR INSERT and SELECT

    private void initDefaultGeneratedKeysSupport(int major, int minor) throws SQLException {
        expectDatabaseVersionCheck(major, minor);
        generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("default", dbMetadata);
    }

    private void expectDatabaseVersionCheck(final int major, final int minor) throws SQLException {
        context.checking(new Expectations() {{
            atLeast(1).of(dbMetadata).getDatabaseMajorVersion(); will(returnValue(major));
            oneOf(dbMetadata).getDatabaseMinorVersion(); will(returnValue(minor));
        }});
    }

    private void expectConnectionDialectCheck(final int connectionDialect) throws SQLException {
        context.checking(new Expectations() {{
            oneOf(dbMetadata).getConnectionDialect(); will(returnValue(connectionDialect));
        }});
    }

    private void expectNoGetColumnsCall() throws SQLException {
        context.checking(new Expectations() {{
            never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                    with(any(String.class)), with(any(String.class)));
        }});
    }
}
