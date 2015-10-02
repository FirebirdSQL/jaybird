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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBTpbMapper;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBTpbMapper {

    public static final String TEST_TPB_MAPPING = "org.firebirdsql.jca.test_tpb_mapping";

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
        FBTpbMapper mapper = new FBTpbMapper(TEST_TPB_MAPPING, getClass().getClassLoader());
        TransactionParameterBuffer tpbValue = mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED);

        assertTrue(
                "READ_COMMITED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_nowait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_nowait)
        );

        tpbValue = mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ);

        assertTrue(
                "REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_wait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_consistency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );

        tpbValue = mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE);

        assertTrue(
                "SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait",
                tpbValue.hasArgument(ISCConstants.isc_tpb_concurrency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );
    }

    @Test
    public void testProcessMappingWithLockTimeout() throws Exception {
        String testMapping = "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5";

        TransactionParameterBuffer tpbValue = FBTpbMapper.processMapping(testMapping);

        assertTrue(
                "must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_wait,isc_tpb_lock_timeout=5",
                tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_lock_timeout) &&
                        tpbValue.getArgumentAsInt(ISCConstants.isc_tpb_lock_timeout) == 5
        );
    }
}
