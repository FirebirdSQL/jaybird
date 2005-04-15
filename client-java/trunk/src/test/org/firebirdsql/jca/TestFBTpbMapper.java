/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jca;


import java.sql.Connection;
import java.util.Set;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

import org.firebirdsql.common.FBTestBase;

/**
 * <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBTpbMapper extends FBTestBase {
    
    public static final String TEST_TPB_MAPPING = "org.firebirdsql.jca.test_tpb_mapping";
    

    public TestFBTpbMapper(String string) {
        super(string);
    }

    FBManagedConnectionFactory mcf;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        mcf = super.createFBManagedConnectionFactory();//new FBManagedConnectionFactory();
    }
    

    /**
     * Test if default isolation level is Connection.TRANSACTION_READ_COMMITTED
     * 
     * @throws Exception if something went wrong.
     */
    public void testDefaultIsolationLevel() throws Exception {
        assertTrue("Default tx isolation level must be READ_COMMITTED", 
            mcf.getTransactionIsolation().intValue() == Connection.TRANSACTION_READ_COMMITTED);
    }
    
    /**
     * Test custom TPB mapper. This test case constructs customg TPB mapper,
     * assigns it to managed connection factory and checks if correct values
     * are obtained from TPB.
     * 
     * @throws Exception if something went wrong.
     */
    public void testTpbMapper() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper(mcf.getGDS(), TEST_TPB_MAPPING, getClass().getClassLoader());
        
        mcf.setTpbMapper(mapper);
        
        mcf.setTransactionIsolation(
            new Integer(Connection.TRANSACTION_READ_COMMITTED));
        
        TransactionParameterBuffer tpbValue = mcf.getTpb().getTransactionParameterBuffer();
        
        assertTrue(
            "READ_COMMITED must be isc_tpb_read_committed+" + 
            "isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_nowait",
            tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_nowait)
        );
        
        mcf.setTransactionIsolation(
            new Integer(Connection.TRANSACTION_REPEATABLE_READ));
        
        tpbValue = mcf.getTpb().getTransactionParameterBuffer();
        
        assertTrue(
            "REPEATABLE_READ must be isc_tpb_consistency+" + 
            "isc_tpb_write+isc_tpb_wait",
            tpbValue.hasArgument(ISCConstants.isc_tpb_consistency) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );

        mcf.setTransactionIsolation(
            new Integer(Connection.TRANSACTION_SERIALIZABLE));
        
        tpbValue = mcf.getTpb().getTransactionParameterBuffer();
        
        assertTrue(
            "SERIALIZABLE must be isc_tpb_concurrency+" + 
            "isc_tpb_write+isc_tpb_wait",
            tpbValue.hasArgument(ISCConstants.isc_tpb_concurrency) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
            tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );
        
    }

}
