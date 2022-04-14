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
package org.firebirdsql.ds;

import org.firebirdsql.jaybird.props.internal.TransactionNameMapping;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Reference;

import static org.junit.Assert.*;

/**
 * Tests for {@link DataSourceFactory} and - indirectly - the correctness of the getReference() method of
 * {@link FBConnectionPoolDataSource}, {@link FBXADataSource}, and {@link FBSimpleDataSource}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestDataSourceFactory {
    
    private static final String ROLE_NAME = "someRoleName";
    private static final int LOGIN_TIMEOUT = 513;
    private static final String ENCODING = "WIN1252";
    private static final String PASSWORD = "somePassword";
    private static final String USER = "someUser";
    private static final String DATABASE_NAME = "/some/path/to/database.fdb";
    private static final int PORT_NUMBER = 33050;
    private static final String SERVER_NAME = "someServer.local";
    private static final String TYPE = "PURE_JAVA";
    private static final String DESCRIPTION = "Description of originalDS";

    /**
     * Fills the properties (exposed with JavaBeans setters) of FBAbstractCommonDataSource for testing. Does not set
     * nonStandardProperty and encoding (as it is set through charSet).
     * 
     * @param instance Instance to configure
     */
    private void fillFBAbstractCommonDataSourceProperties(FBAbstractCommonDataSource instance) throws SQLException {
        instance.setDescription(DESCRIPTION);
        instance.setType(TYPE);
        instance.setServerName(SERVER_NAME);
        instance.setPortNumber(PORT_NUMBER);
        instance.setDatabaseName(DATABASE_NAME);
        instance.setUser(USER);
        instance.setPassword(PASSWORD);
        instance.setEncoding(ENCODING);
        instance.setLoginTimeout(LOGIN_TIMEOUT);
        instance.setRoleName(ROLE_NAME);
    }
    
    /**
     * Validates if the instance of FBAbstractCommonDataSource has the values as set by
     * {@link TestDataSourceFactory#fillFBAbstractCommonDataSourceProperties(FBAbstractCommonDataSource)}.
     * 
     * @param instance Instance to validate
     */
    private void assertFBAbstractCommonDataSourceProperties(FBAbstractCommonDataSource instance) throws SQLException{
        assertEquals(DESCRIPTION, instance.getDescription());
        assertEquals(TYPE, instance.getType());
        assertEquals(SERVER_NAME, instance.getServerName());
        assertEquals(PORT_NUMBER, instance.getPortNumber());
        assertEquals(DATABASE_NAME, instance.getDatabaseName());
        assertEquals(USER, instance.getUser());
        assertEquals(PASSWORD, instance.getPassword());
        assertEquals(ENCODING, instance.getEncoding());
        assertEquals(LOGIN_TIMEOUT, instance.getLoginTimeout());
        assertEquals(ROLE_NAME, instance.getRoleName());
    }
    
    /**
     * Tests reconstruction of a {@link FBConnectionPoolDataSource} using a reference.
     * <p>
     * This test is done with the basic properties exposed through setters. It tests
     * <ol>
     * <li>If the reference returned has the right factory name</li>
     * <li>If the reference returned has the right classname</li>
     * <li>If the object returned by the factory is a distinct new instance</li>
     * <li>If all the properties set on the original are also set on the new instance</li>
     * </ol>
     * </p>
     */
    @Test
    public void testBuildFBConnectionPoolDataSource_basicProperties() throws Exception {
        final FBConnectionPoolDataSource originalDS = new FBConnectionPoolDataSource();
        
        fillFBAbstractCommonDataSourceProperties(originalDS);
        Reference ref = originalDS.getReference();
        
        assertEquals("Unexpected factory name", DataSourceFactory.class.getName(), ref.getFactoryClassName());
        assertEquals("Unexpected class name", FBConnectionPoolDataSource.class.getName(), ref.getClassName());
        
        FBConnectionPoolDataSource newDS = (FBConnectionPoolDataSource)new DataSourceFactory().getObjectInstance(ref, null, null, null);
        assertNotSame("Expected distinct new object", originalDS, newDS);
        assertFBAbstractCommonDataSourceProperties(newDS);
    }
    
    /**
     * Tests reconstruction of a {@link FBXADataSource} using a reference.
     * <p>
     * This test is done with the basic properties exposed through setters. It tests
     * <ol>
     * <li>If the reference returned has the right factory name</li>
     * <li>If the reference returned has the right classname</li>
     * <li>If the object returned by the factory is a distinct new instance</li>
     * <li>If all the properties set on the original are also set on the new instance</li>
     * </ol>
     * </p>
     */
    @Test
    public void testBuildFBXADataSource_basicProperties() throws Exception {
        final FBXADataSource originalDS = new FBXADataSource();
        
        fillFBAbstractCommonDataSourceProperties(originalDS);
        Reference ref = originalDS.getReference();
        
        assertEquals("Unexpected factory name", DataSourceFactory.class.getName(), ref.getFactoryClassName());
        assertEquals("Unexpected class name", FBXADataSource.class.getName(), ref.getClassName());
        
        FBXADataSource newDS = (FBXADataSource)new DataSourceFactory().getObjectInstance(ref, null, null, null);
        assertFBAbstractCommonDataSourceProperties(newDS);
    }
    
    /**
     * Tests reconstruction of a {@link FBConnectionPoolDataSource} using a reference.
     * <p>
     * This test is done with a selection of properties set through the {@link FBConnectionPoolDataSource#setNonStandardProperty(String)} methods. It tests
     * <ol>
     * <li>If the reference returned has the right factory name</li>
     * <li>If the reference returned has the right classname</li>
     * <li>If the object returned by the factory is a distinct new instance</li>
     * <li>If all the properties set on the original are also set on the new instance</li>
     * <li>If an unset property is handled correctly</li>
     * </ol>
     * </p>
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testBuildFBConnectionPoolDataSource_nonStandardProperties() throws Exception {
        final FBConnectionPoolDataSource originalDS = new FBConnectionPoolDataSource();
        
        originalDS.setNonStandardProperty("buffersNumber=127");
        originalDS.setNonStandardProperty("defaultTransactionIsolation",
                Integer.toString(Connection.TRANSACTION_SERIALIZABLE));
        originalDS.setNonStandardProperty("madeUpProperty", "madeUpValue");
        Reference ref = originalDS.getReference();
        
        FBConnectionPoolDataSource newDS = (FBConnectionPoolDataSource)new DataSourceFactory().getObjectInstance(ref, null, null, null);
        assertEquals("127", newDS.getProperty("buffersNumber"));
        assertEquals(TransactionNameMapping.TRANSACTION_SERIALIZABLE, newDS.getProperty("defaultTransactionIsolation"));
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, newDS.getIntProperty("defaultTransactionIsolation", -1));
        assertEquals("madeUpValue", newDS.getProperty("madeUpProperty"));
        assertNull(newDS.getDescription());
    }
    
    /**
     * Tests reconstruction of a {@link FBXADataSource} using a reference.
     * <p>
     * This test is done with a selection of properties set through the {@link FBXADataSource#setNonStandardProperty(String)} methods. It tests
     * <ol>
     * <li>If the reference returned has the right factory name</li>
     * <li>If the reference returned has the right classname</li>
     * <li>If the object returned by the factory is a distinct new instance</li>
     * <li>If all the properties set on the original are also set on the new instance</li>
     * <li>If an unset property is handled correctly</li> 
     * </ol>
     * </p>
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testBuildFBXADataSource_nonStandardProperties() throws Exception {
        final FBXADataSource originalDS = new FBXADataSource();
        
        originalDS.setNonStandardProperty("buffersNumber=127");
        originalDS.setNonStandardProperty("defaultTransactionIsolation",
                Integer.toString(Connection.TRANSACTION_SERIALIZABLE));
        originalDS.setNonStandardProperty("madeUpProperty", "madeUpValue");
        Reference ref = originalDS.getReference();
        
        FBXADataSource newDS = (FBXADataSource)new DataSourceFactory().getObjectInstance(ref, null, null, null);
        assertEquals("127", newDS.getProperty("buffersNumber"));
        assertEquals(TransactionNameMapping.TRANSACTION_SERIALIZABLE, newDS.getProperty("defaultTransactionIsolation"));
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, newDS.getIntProperty("defaultTransactionIsolation", -1));
        assertEquals("madeUpValue", newDS.getProperty("madeUpProperty"));
        assertNull(newDS.getDescription());
    }

    @Test
    public void testBuildFBSimpleDataSource() throws Exception {
        final FBSimpleDataSource originalDS = new FBSimpleDataSource();
        originalDS.setDescription(DESCRIPTION);
        originalDS.setType(TYPE);
        final String database = String.format("//%s:%d/%s", SERVER_NAME, PORT_NUMBER, DATABASE_NAME);
        originalDS.setDatabaseName(database);
        originalDS.setUser(USER);
        originalDS.setPassword(PASSWORD);
        originalDS.setEncoding(ENCODING);
        originalDS.setLoginTimeout(LOGIN_TIMEOUT);
        originalDS.setRoleName(ROLE_NAME);
        originalDS.setNonStandardProperty("buffersNumber=127");
        originalDS.setIntProperty("defaultTransactionIsolation", Connection.TRANSACTION_SERIALIZABLE);
        originalDS.setProperty("madeUpProperty", "madeUpValue");
        Reference ref = originalDS.getReference();

        assertEquals("Unexpected factory name", DataSourceFactory.class.getName(), ref.getFactoryClassName());
        assertEquals("Unexpected class name", FBSimpleDataSource.class.getName(), ref.getClassName());

        FBSimpleDataSource newDS =
                (FBSimpleDataSource) new DataSourceFactory().getObjectInstance(ref, null, null, null);

        assertEquals(DESCRIPTION, newDS.getDescription());
        assertEquals(TYPE, newDS.getType());
        assertEquals(database, newDS.getDatabaseName());
        assertEquals(USER, newDS.getUser());
        assertEquals(PASSWORD, newDS.getPassword());
        assertEquals(ENCODING, newDS.getEncoding());
        assertEquals(LOGIN_TIMEOUT, newDS.getLoginTimeout());
        assertEquals(ROLE_NAME, newDS.getRoleName());
        assertEquals(Integer.valueOf(127), newDS.getIntProperty("buffersNumber"));
        assertEquals(Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE),
                newDS.getIntProperty("defaultTransactionIsolation"));
        assertEquals("madeUpValue", newDS.getProperty("madeUpProperty"));
    }
}
