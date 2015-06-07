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
package org.firebirdsql.jca;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBTpbMapper;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.createFBManagedConnectionFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBTpbMapper extends FBJUnit4TestBase {

    public static final String TEST_TPB_MAPPING = "org.firebirdsql.jca.test_tpb_mapping";

    FBManagedConnectionFactory mcf;

    @Before
    public void setUp() throws Exception {
        mcf = createFBManagedConnectionFactory();
    }

    /**
     * Test if default isolation level is Connection.TRANSACTION_READ_COMMITTED
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testDefaultIsolationLevel() throws Exception {
        assertEquals("Default tx isolation level must be READ_COMMITTED",
                Connection.TRANSACTION_READ_COMMITTED, mcf.getDefaultTransactionIsolation());
    }

    /**
     * Test custom TPB mapper. This test case constructs customg TPB mapper,
     * assigns it to managed connection factory and checks if correct values
     * are obtained from TPB.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testTpbMapper() throws Exception {
        // TODO Why is this mapper created and then not used?
        FBTpbMapper mapper = new FBTpbMapper(TEST_TPB_MAPPING, getClass().getClassLoader());

        mcf.setTpbMapping(TEST_TPB_MAPPING);
        mcf.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        TransactionParameterBuffer tpbValue = mcf.getDefaultTpb().getTransactionParameterBuffer();

        assertTrue(
                "READ_COMMITED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_nowait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_nowait)
        );

        mcf.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        tpbValue = mcf.getDefaultTpb().getTransactionParameterBuffer();

        assertTrue(
                "REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_wait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_consistency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );

        mcf.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        tpbValue = mcf.getDefaultTpb().getTransactionParameterBuffer();

        assertTrue(
                "SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_concurrency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );
    }

}
