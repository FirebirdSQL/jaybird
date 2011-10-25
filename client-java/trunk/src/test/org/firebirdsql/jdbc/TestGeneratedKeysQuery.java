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
     * Test AbstractGeneratedKeysQuery for the case of passing
     * Statement.NO_GENERATED_KEYS with a normal INSERT.
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
                // In combination with NO_GENERATED_KEYS the metadata should not
                // be retrieved
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertFalse("Query with NO_GENERATED_KEYS should not generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", TEST_INSERT_QUERY, query.getQueryString());
    }
    
    /**
     * Test AbstractGeneratedKeysQuery for the case of passing
     * Statement.NO_GENERATED_KEYS with a normal INSERT.
     * <p>
     * <ul>
     * <li>generatesKeys() returns <code>false</code></li>
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
                // In combination with NO_GENERATED_KEYS the metadata should not
                // be retrieved
                never(dbMetadata).getColumns(with(any(String.class)), with(any(String.class)),
                        with(any(String.class)), with(any(String.class)));
            }
        });
        
        String testQuery = TEST_INSERT_QUERY + " RETURNING id";

        AbstractGeneratedKeysQuery query = new AbstractGeneratedKeysQuery(testQuery, Statement.NO_GENERATED_KEYS) {
            @Override
            DatabaseMetaData getDatabaseMetaData() throws SQLException {
                return dbMetadata;
            }
        };

        assertTrue("Query with NO_GENERATED_KEYS, but with RETURNING clause should generate keys", query.generatesKeys());
        assertEquals("Query string should not be modified", testQuery, query.getQueryString());
    }

    /**
     * Test AbstractGeneratedKeysQuery for the case of passing
     * Statement.RETURN_GENERATED_KEYS with a normal INSERT.
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
        String expectedSuffix = "\nRETURNING ID, NAME, TEXT_VALUE";

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
        assertTrue(queryString.endsWith(expectedSuffix));
    }

    /**
     * Test AbstractGeneratedKeysQuery for the case of passing
     * Statement.RETURN_GENERATED_KEYS with an INSERT containing a RETURNING
     * clause.
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
                // Metadata should never be requested, as the query already has a RETURNING clause
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
        assertEquals("Query string should not be modified as it alreayd includes RETURNING clause",
                testQuery, query.getQueryString());
    }

    // TODO Add tests for columnIndexes and columnNames
}
