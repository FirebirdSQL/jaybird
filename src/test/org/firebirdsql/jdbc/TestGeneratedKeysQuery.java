/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * Tests for {@link AbstractGeneratedKeysQuery}.
 * <p>
 * This test does not test against a database server.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestGeneratedKeysQuery extends MockObjectTestCase {

    private static final String TEST_INSERT_QUERY = "INSERT INTO GENERATED_KEYS_TBL(NAME, TEXT_VALUE) VALUES (?, ?)";

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int)}
     * for the case of passing {@link Statement#NO_GENERATED_KEYS} with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
     * <li>getQueryString() returns query unmodified</code></li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_noGeneratedKeys() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // In combination with NO_GENERATED_KEYS the metadata should not be retrieved
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                Statement.NO_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with NO_GENERATED_KEYS should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY,
                query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int)}
     * for the case of passing {@link Statement#NO_GENERATED_KEYS} with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query unmodified</code></li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_noGeneratedKeys_withReturning() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // In combination with NO_GENERATED_KEYS the metadata should not be retrieved
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(testQuery,
                Statement.NO_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with NO_GENERATED_KEYS, but with RETURNING clause should generate keys",
                query.generatesKeys());
        assertEquals("Query string should not be modified", testQuery, query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int)}
     * for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with a normal
     * INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query with added RETURNING clause containing
     * all columns reported by the databasemetadata</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_returnGeneratedKeys() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);
        final ResultSet columnRs = mock(ResultSet.class);

        checking(new Expectations() {
            {
                // Metadata for table in query will be retrieved
                oneOf(dbMetadata).getColumns(null, null, "GENERATED_KEYS_TBL", null);
                will(returnValue(columnRs));
                // We want to return three columns, so for next() three return true, fourth returns false
                exactly(4).of(columnRs).next();
                will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                        returnValue(false)));
                // NOTE: Implementation detail that this only calls getString for column 4 (COLUMN_NAME)
                exactly(3).of(columnRs).getString(4);
                will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME"),
                        returnValue("TEXT_VALUE")));
                oneOf(columnRs).close();
            }
        });

        // NOTE Implementation detail
        String expectedSuffix = "\nRETURNING \"ID\", \"NAME\", \"TEXT_VALUE\"";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                Statement.RETURN_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        String queryString = query.getQueryString();
        assertFalse("Query string should be modified", TEST_INSERT_QUERY.equals(queryString));
        assertTrue("Query string should start with original query",
                queryString.startsWith(TEST_INSERT_QUERY));
        assertTrue(queryString, queryString.endsWith(expectedSuffix));
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int)}
     * for the case of passing {@link Statement#RETURN_GENERATED_KEYS} with an INSERT
     * containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_returnGeneratedKeys_withReturning() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // Metadata should never be requested, as the query already has
                // a RETURNING clause
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(testQuery,
                Statement.RETURN_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int)}
     * for the case of passing a normal INSERT with an invalid autGeneratedKeys
     * value (not {@link Statement#NO_GENERATED_KEYS} or
     * {@link Statement#RETURN_GENERATED_KEYS}).
     * <p>
     * <ul>
     * <li>Throws SQLException with sqlstate HY092 (invalid option identifier)</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_invalidAutoGeneratedKeys_value() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // In combination with NO_GENERATED_KEYS the metadata should not be retrieved
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        try {
            new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY, 3) {
                @Override
                DatabaseMetaData getDatabaseMetaData() throws SQLException {
                    return dbMetadata;
                }
            };
            fail("Expected SQLException to be thrown for invalid autoGeneratedKeys value");
        } catch (SQLException ex) {
            assertEquals(FBSQLException.SQL_STATE_INVALID_OPTION_IDENTIFIER, ex.getSQLState());
        }
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing a normal INSERT and indexed columns, in ascending
     * order of ordinal position.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query RETURNING clause with columns
     * specified</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);
        final ResultSet columnRs = mock(ResultSet.class);

        checking(new Expectations() {
            {
                // Metadata for table in query will be retrieved
                oneOf(dbMetadata).getColumns(null, null, "GENERATED_KEYS_TBL", null);
                will(returnValue(columnRs));
                // We want to return three columns, so for next() three return true, fourth returns false
                exactly(4).of(columnRs).next();
                will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                        returnValue(false)));
                // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice (for the indexed columns)
                exactly(2).of(columnRs).getString(4);
                will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
                // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
                exactly(3).of(columnRs).getInt(17);
                will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
                oneOf(columnRs).close();
            }
        });

        // NOTE Implementation detail
        String expectedSuffix = "\nRETURNING \"ID\", \"NAME\"";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new int[] { 1, 2 }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        String queryString = query.getQueryString();
        assertFalse("Query string should be modified", TEST_INSERT_QUERY.equals(queryString));
        assertTrue("Query string should start with original query",
                queryString.startsWith(TEST_INSERT_QUERY));
        assertTrue(queryString.endsWith(expectedSuffix));
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing a normal INSERT and indexed columns, including a
     * non existent column index.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query RETURNING clause with only columns
     * with an existing ordinal position</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes_includingNonExistentIndex() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);
        final ResultSet columnRs = mock(ResultSet.class);

        checking(new Expectations() {
            {
                // Metadata for table in query will be retrieved
                oneOf(dbMetadata).getColumns(null, null, "GENERATED_KEYS_TBL", null);
                will(returnValue(columnRs));
                // We want to return three columns, so for next() three return true, fourth returns false
                exactly(4).of(columnRs).next();
                will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                        returnValue(false)));
                // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice (for the indexed columns)
                exactly(2).of(columnRs).getString(4);
                will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
                // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
                exactly(3).of(columnRs).getInt(17);
                will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
                oneOf(columnRs).close();
            }
        });

        // NOTE Implementation detail
        String expectedSuffix = "\nRETURNING \"ID\", \"NAME\"";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new int[] { 1, 2, 5 }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        String queryString = query.getQueryString();
        assertFalse("Query string should be modified", TEST_INSERT_QUERY.equals(queryString));
        assertTrue("Query string should start with original query",
                queryString.startsWith(TEST_INSERT_QUERY));
        assertTrue(queryString.endsWith(expectedSuffix));
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing a normal INSERT and indexed columns, in
     * non-ascending order of ordinal position.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query RETURNING clause with columns
     * specified in ascending order by ordinal position</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes_unOrdered() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);
        final ResultSet columnRs = mock(ResultSet.class);

        checking(new Expectations() {
            {
                // Metadata for table in query will be retrieved
                oneOf(dbMetadata).getColumns(null, null, "GENERATED_KEYS_TBL", null);
                will(returnValue(columnRs));
                // We want to return three columns, so for next() three return true, fourth returns false
                exactly(4).of(columnRs).next();
                will(onConsecutiveCalls(returnValue(true), returnValue(true), returnValue(true),
                        returnValue(false)));
                // NOTE: Implementation detail that this calls getString for column 4 (COLUMN_NAME) twice (for the indexed columns)
                exactly(2).of(columnRs).getString(4);
                will(onConsecutiveCalls(returnValue("ID"), returnValue("NAME")));
                // NOTE: Implementation detail that this calls getInt for column 17 (ORDINAL_POSITION)
                exactly(3).of(columnRs).getInt(17);
                will(onConsecutiveCalls(returnValue(1), returnValue(2), returnValue(3)));
                oneOf(columnRs).close();
            }
        });

        // NOTE Implementation detail
        String expectedSuffix = "\nRETURNING \"ID\", \"NAME\"";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new int[] { 2, 1 }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        String queryString = query.getQueryString();
        assertFalse("Query string should be modified", TEST_INSERT_QUERY.equals(queryString));
        assertTrue("Query string should start with original query",
                queryString.startsWith(TEST_INSERT_QUERY));
        assertTrue(queryString.endsWith(expectedSuffix));
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing an INSERT containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes_withReturning() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // Metadata should never be requested, as the query already has a RETURNING clause
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(testQuery, new int[] { 1,
                2, 3 }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with columnIndexes should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing an INSERT and a null columnIndexes array.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
     * <li>getQueryString() returns original query</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes_nullColumns() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // Metadata should never be requested, as no columns have been specified
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                (int[]) null) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with null columnIndexes should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY,
                query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing an INSERT and a empty columnIndexes array.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
     * <li>getQueryString() returns original query</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnIndexes_emptyColumns() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // Metadata should never be requested, as no columns have been specified
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new int[] {}) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with empty columnIndexes should not generate keys",
                query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY,
                query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, String[])}
     * for the case of passing a normal INSERT and an array of columnNames.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query RETURNING clause with columns
     * specified</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnNames() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // dbMetadData getColumns will not be accessed for using columnNames
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        // NOTE Implementation detail
        String expectedSuffix = "\nRETURNING NAME, ID";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new String[] { "NAME", "ID" }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with columnNames should generate keys", query.generatesKeys());
        String queryString = query.getQueryString();
        assertFalse("Query string should be modified", TEST_INSERT_QUERY.equals(queryString));
        assertTrue("Query string should start with original query",
                queryString.startsWith(TEST_INSERT_QUERY));
        assertTrue(queryString.endsWith(expectedSuffix));
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, String[])}
     * for the case of passing an INSERT containing a RETURNING clause.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>true</code></li>
     * <li>getQueryString() returns query with original RETURNING clause only</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnNames_withReturning() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // dbMetadData getColumns will not be accessed for using columnNames
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(testQuery, new String[] {
                "ID", "NAME" }) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with RETURN_GENERATED_KEYS should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified as it already includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, String[])}
     * for the case of passing an INSERT and a null columnNames array.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
     * <li>getQueryString() returns original query</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnNames_nullColumns() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // dbMetadData getColumns will not be accessed for using columnNames
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                (String[]) null) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with null columnNames should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY,
                query.getQueryString());
    }

    /**
     * Test
     * {@link AbstractGeneratedKeysQuery#AbstractGeneratedKeysQuery(String, int[])}
     * for the case of passing an INSERT and a empty columnNames array.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
     * <li>getQueryString() returns original query</li>
     * </ul>
     * </p>
     * 
     * @throws SQLException
     */
    public void testGeneratedKeys_columnNames_emptyColumns() throws SQLException {
        final DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);

        checking(new Expectations() {
            {
                // dbMetadData getColumns will not be accessed for using columnNames
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY,
                new String[] {}) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with empty columnNames should not generate keys",
                query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY,
                query.getQueryString());
    }

    // TODO Consider including tests for DELETE, UPDATE, UPDATE OR INSERT and SELECT
}
