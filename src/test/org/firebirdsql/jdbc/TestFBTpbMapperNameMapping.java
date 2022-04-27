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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

/**
 * Tests for the mapping between Connection.TRANSACTION_* integer values and names in {@link org.firebirdsql.jdbc.FBTpbMapper}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@RunWith(Parameterized.class)
public class TestFBTpbMapperNameMapping {

    @SuppressWarnings("deprecation")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final Integer connectionTransactionValue;
    private final String fbTpbMapperTransactionValue;

    /**
     * Constructor for the parametrized tests.
     * <p>
     * The parameters define test and expectation for the tests.
     * </p>
     *
     * @param connectionTransactionValue
     *         Transaction value of <code>Connection.TRANSACTION_*</code> to test or expect, or <code>null</code> to expect {@link
     *         java.lang.IllegalArgumentException} in {@link org.firebirdsql.jdbc.FBTpbMapper#getTransactionIsolationLevel(String)}
     * @param fbTpbMapperTransactionValue
     *         Transaction value of <code>FBTpbMapper.TRANSACTION_*</code> to test or expect, or <code>null</code> to expect
     *         {@link java.lang.IllegalArgumentException} in {@link org.firebirdsql.jdbc.FBTpbMapper#getTransactionIsolationName(int)}
     */
    public TestFBTpbMapperNameMapping(Integer connectionTransactionValue, String fbTpbMapperTransactionValue) {
        this.connectionTransactionValue = connectionTransactionValue;
        this.fbTpbMapperTransactionValue = fbTpbMapperTransactionValue;
    }

    /**
     * Tests that the isolation level is matched to the right isolation name.
     */
    @Test
    public void testGetTransactionIsolationName() {
        assumeThat("Ignoring test as connectionTransactionValue is null", connectionTransactionValue, notNullValue());
        if (fbTpbMapperTransactionValue == null) {
            expectedException.expect(IllegalArgumentException.class);
        }

        assertEquals(String.format("Unexpected transactionIsolation name for level %d", connectionTransactionValue),
                fbTpbMapperTransactionValue, FBTpbMapper.getTransactionIsolationName(connectionTransactionValue));
    }

    /**
     * Tests that the isolation name is matched to the right isolation level.
     */
    @Test
    public void testGetTransactionIsolationLevel() {
        assumeThat("Ignoring test as fbTpbMapperTransactionValue is null", fbTpbMapperTransactionValue, notNullValue());
        if (connectionTransactionValue == null) {
            expectedException.expect(IllegalArgumentException.class);
        }

        assertEquals(String.format("Unexpected transactionIsolation level for name %s", fbTpbMapperTransactionValue),
                connectionTransactionValue, (Integer) FBTpbMapper.getTransactionIsolationLevel(fbTpbMapperTransactionValue));
    }

    @Parameterized.Parameters(name = "{index}: {0} <=> {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Connection.TRANSACTION_NONE, FBTpbMapper.TRANSACTION_NONE },
                { Connection.TRANSACTION_READ_UNCOMMITTED, FBTpbMapper.TRANSACTION_READ_UNCOMMITTED },
                { Connection.TRANSACTION_READ_COMMITTED, FBTpbMapper.TRANSACTION_READ_COMMITTED },
                { Connection.TRANSACTION_REPEATABLE_READ, FBTpbMapper.TRANSACTION_REPEATABLE_READ },
                { Connection.TRANSACTION_SERIALIZABLE, FBTpbMapper.TRANSACTION_SERIALIZABLE },
                { -1, null },
                { null, "ABC" }
        });
    }
}
