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
package org.firebirdsql.pool;

import java.util.Properties;

import javax.naming.Context;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBDriver;

/**
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestDriverConnectionPoolDataSource extends
        TestFBConnectionPoolDataSource {

    public TestDriverConnectionPoolDataSource(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        AbstractDriverConnectionPoolDataSource connectionPool = 
            FBPooledDataSourceFactory.createDriverConnectionPoolDataSource();

        connectionPool.setJdbcUrl(getUrl());
        connectionPool.setDriverClassName(
            FBDriver.class.getName());
        
        connectionPool.setMinPoolSize(DEFAULT_MIN_CONNECTIONS);
        connectionPool.setMaxPoolSize(DEFAULT_MAX_CONNECTIONS);
        connectionPool.setPingInterval(DEFAULT_PING_INTERVAL);
        
        connectionPool.setProperties(getDefaultPropertiesForConnection());
        
        this.pool = connectionPool;
        
    }

    protected void tearDown() throws Exception {
        pool.shutdown();
        
        super.tearDown();
    }

    public void testJNDI() throws Exception {
        if (getGdsType() != GDSType.getType("PURE_JAVA"))
            fail("This test case does not work with JNI connections.");
        
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, ClassFactory.get(ClassFactory.DriverConnectionPoolDataSource).getName());
        
        checkJNDI(props);
    }

    public void testReleaseResultSet() throws Exception {
        // test is not defined for this type of pool
    }
    
    public void testEncoding() throws Exception {
        // test is not defined for this type of pool
    }

    public void testCustomTpbMapping() throws Exception {
        // test is not defined for this type of pool
    }
    
}
