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

import org.firebirdsql.jdbc.FBDriver;

/**
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestDriverConnectionPoolDataSource extends
        TestFBConnectionPoolDataSource {

    public TestDriverConnectionPoolDataSource(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        DriverConnectionPoolDataSource connectionPool = 
            new DriverConnectionPoolDataSource();

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
        super.tearDown();
    }

    public void testBlocking() throws Exception {
        super.testBlocking();
    }

    public void testConnection() throws Exception {
        super.testConnection();
    }

    public void testFalseConnectionUsage() throws Exception {
        super.testFalseConnectionUsage();
    }

    public void testIdleRemover() throws Exception {
        super.testIdleRemover();
    }

    public void testJNDI() throws Exception {
        String JNDI_FACTORY = "com.sun.jndi.fscontext.RefFSContextFactory";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        props.put(Context.OBJECT_FACTORIES, DriverConnectionPoolDataSource.class.getName());
        
        checkJNDI(props);
    }

    public void testPoolStart() throws Exception {
        super.testPoolStart();
    }

    public void testPreparedStatement() throws Exception {
        super.testPreparedStatement();
    }
    
}
