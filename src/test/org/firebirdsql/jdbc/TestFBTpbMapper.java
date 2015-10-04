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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jca.FBResourceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBTpbMapper {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final String TEST_TPB_MAPPING = "org.firebirdsql.jdbc.test_tpb_mapping";

    @Test
    public void testNewWithMappingFile() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper(TEST_TPB_MAPPING, getClass().getClassLoader());
        TransactionParameterBuffer tpbValue = mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED);

        assertTrue("READ_COMMITTED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_nowait",
                tpbValue.size() == 4 &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_nowait)
        );

        tpbValue = mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ);

        assertTrue("REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_wait",
                tpbValue.size() == 3 &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_consistency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );

        tpbValue = mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE);

        assertTrue("SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait",
                tpbValue.size() == 3 &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_concurrency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait)
        );
    }

    @Test
    public void testNewMappingFileDoesNotExist_throwsFBResourceException() throws Exception {
        expectedException.expect(FBResourceException.class);

        new FBTpbMapper(TEST_TPB_MAPPING + "does_not_exist", getClass().getClassLoader());
    }

    @Test
    public void testNewWithIncompleteMap_unspecifiedUseDefaults() throws Exception {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        final Map<String, String> map = new HashMap<>();
        map.put(FBTpbMapper.TRANSACTION_REPEATABLE_READ,
                "isc_tpb_concurrency,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5");

        FBTpbMapper mapper = new FBTpbMapper(map);


        // Check if matches specified
        TransactionParameterBuffer tpbValue = mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ);
        assertTrue("REPEATABLE_READ must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait+isc_tpb_lock_timeout=5",
                tpbValue.size() == 4 &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_concurrency) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_lock_timeout) &&
                        tpbValue.getArgumentAsInt(ISCConstants.isc_tpb_lock_timeout) == 5
        );
        // Check other isolation levels match default:
        assertEquals(defaultMapper.getMapping(Connection.TRANSACTION_SERIALIZABLE),
                mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE));
        assertEquals(defaultMapper.getMapping(Connection.TRANSACTION_SERIALIZABLE),
                mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE));
    }

    @Test
    public void testNewMapContainsInvalidName_throwsFBResourceException() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("special", "isc_tpb_concurrency,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5");
        expectedException.expect(FBResourceException.class);

        new FBTpbMapper(map);
    }

    @Test
    public void testProcessMappingWithLockTimeout() throws Exception {
        String testMapping =
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5";

        TransactionParameterBuffer tpbValue = FBTpbMapper.processMapping(testMapping);

        assertTrue("must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_wait,5",
                tpbValue.size() == 5 &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_wait) &&
                        tpbValue.hasArgument(ISCConstants.isc_tpb_lock_timeout) &&
                        tpbValue.getArgumentAsInt(ISCConstants.isc_tpb_lock_timeout) == 5
        );
    }

    @Test
    public void testProcessMapping_TokenIsNotATpbArgument_throwsFBResourceException() throws Exception {
        String testMapping = "not_a_tpb_argument";
        expectedException.expect(FBResourceException.class);

        FBTpbMapper.processMapping(testMapping);
    }

    @Test
    public void testProcessMapping_ValueIsNotAnInteger_throwsFBResourceException() throws Exception {
        String testMapping = "isc_tpb_lock_timeout=x";
        expectedException.expect(FBResourceException.class);

        FBTpbMapper.processMapping(testMapping);
    }

    @Test
    public void testInitialDefaultTransactionIsolationLevel_READ_COMMITTED() {
        FBTpbMapper mapper = new FBTpbMapper();

        assertEquals(Connection.TRANSACTION_READ_COMMITTED, mapper.getDefaultTransactionIsolation());
    }

    @Test
    public void testSetDefaultTransactionIsolationLevel() {
        FBTpbMapper mapper = new FBTpbMapper();

        mapper.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        assertEquals(Connection.TRANSACTION_SERIALIZABLE, mapper.getDefaultTransactionIsolation());
    }

    @Test
    public void testGetDefaultMapping_returnsMappingOfDefaultTransactionIsolationLevel() {
        FBTpbMapper mapper = new FBTpbMapper();

        assertEquals("Default mapping doesn't match initial default isolation level",
                mapper.getMapping(mapper.getDefaultTransactionIsolation()), mapper.getDefaultMapping());

        mapper.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        assertEquals("Default mapping doesn't match default isolation set to TRANSACTION_SERIALIZABLE",
                mapper.getMapping(mapper.getDefaultTransactionIsolation()), mapper.getDefaultMapping());
    }

    @Test
    public void testGetDefaultMapperReturnsNewInstanceOnEveryInvocation() {
        FBTpbMapper instance1 = FBTpbMapper.getDefaultMapper();
        FBTpbMapper instance2 = FBTpbMapper.getDefaultMapper();

        assertEquals("Instances returns from getDefaultMapper should be equals", instance1, instance2);
        assertNotSame("Instances returns from getDefaultMapper should not have the same identity",
                instance1, instance2);
    }

    @Test
    public void testGetTransactionIsolationName() {
        assertEquals(FBTpbMapper.TRANSACTION_NONE,
                FBTpbMapper.getTransactionIsolationName(Connection.TRANSACTION_NONE));
        assertEquals(FBTpbMapper.TRANSACTION_READ_COMMITTED,
                FBTpbMapper.getTransactionIsolationName(Connection.TRANSACTION_READ_COMMITTED));
        assertEquals(FBTpbMapper.TRANSACTION_READ_UNCOMMITTED,
                FBTpbMapper.getTransactionIsolationName(Connection.TRANSACTION_READ_UNCOMMITTED));
        assertEquals(FBTpbMapper.TRANSACTION_REPEATABLE_READ,
                FBTpbMapper.getTransactionIsolationName(Connection.TRANSACTION_REPEATABLE_READ));
        assertEquals(FBTpbMapper.TRANSACTION_SERIALIZABLE,
                FBTpbMapper.getTransactionIsolationName(Connection.TRANSACTION_SERIALIZABLE));
    }

    @Test
    public void testGetTransactionIsolationName_throwsIllegalArgumentExceptionForUnsupportedValue() {
        expectedException.expect(IllegalArgumentException.class);

        FBTpbMapper.getTransactionIsolationName(-1);
    }

    @Test
    public void testGetTransactionIsolationLevel() {
        assertEquals(Connection.TRANSACTION_NONE,
                FBTpbMapper.getTransactionIsolationLevel(FBTpbMapper.TRANSACTION_NONE));
        assertEquals(Connection.TRANSACTION_READ_COMMITTED,
                FBTpbMapper.getTransactionIsolationLevel(FBTpbMapper.TRANSACTION_READ_COMMITTED));
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED,
                FBTpbMapper.getTransactionIsolationLevel(FBTpbMapper.TRANSACTION_READ_UNCOMMITTED));
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ,
                FBTpbMapper.getTransactionIsolationLevel(FBTpbMapper.TRANSACTION_REPEATABLE_READ));
        assertEquals(Connection.TRANSACTION_SERIALIZABLE,
                FBTpbMapper.getTransactionIsolationLevel(FBTpbMapper.TRANSACTION_SERIALIZABLE));
    }

    @Test
    public void testGetTransactionIsolationLevel_throwsIllegalArgumentExceptionForUnsupportedValue() {
        expectedException.expect(IllegalArgumentException.class);

        FBTpbMapper.getTransactionIsolationLevel("asdf");
    }

    @Test
    public void testGetMappingForREAD_UNCOMMITED_Returns_READ_COMMITTED() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        final TransactionParameterBuffer readCommitted = defaultMapper
                .getMapping(Connection.TRANSACTION_READ_COMMITTED);

        assertEquals("Expected same mapping for READ_UNCOMMITTED as for READ_COMMITTED",
                readCommitted, defaultMapper.getMapping(Connection.TRANSACTION_READ_UNCOMMITTED));
    }

    @Test
    public void testGetMappingForNONE_throwsIllegalArgumentException() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        expectedException.expect(IllegalArgumentException.class);

        defaultMapper.getMapping(Connection.TRANSACTION_NONE);
    }

    @Test
    public void testGetMappingForUnsupportedValue_throwsIllegalArgumentException() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        expectedException.expect(IllegalArgumentException.class);

        defaultMapper.getMapping(-1);
    }

    @Test
    public void testProcessMappingToConnectionProperties() throws Exception {
        final Properties props = new Properties();
        props.setProperty(FBTpbMapper.TRANSACTION_READ_COMMITTED,
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        props.setProperty(FBTpbMapper.TRANSACTION_SERIALIZABLE, "isc_tpb_consistency,isc_tpb_read,isc_tpb_wait");
        props.setProperty(FBTpbMapper.TRANSACTION_REPEATABLE_READ, "isc_tpb_concurrency,isc_tpb_write,isc_tpb_nowait");
        props.setProperty("not_a_transaction_isolation_level", "some_value");
        FBConnectionProperties connectionProps = new FBConnectionProperties();

        assertNull("TRANSACTION_READ_COMMITTED should not have a TPB before processing",
                connectionProps.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED));
        assertNull("TRANSACTION_SERIALIZABLE should not have a TPB before processing",
                connectionProps.getTransactionParameters(Connection.TRANSACTION_SERIALIZABLE));
        assertNull("TRANSACTION_REPEATABLE_READ should not have a TPB before processing",
                connectionProps.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ));

        FBTpbMapper.processMapping(connectionProps, props);

        TransactionParameterBuffer tpbReadCommitted = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);
        assertTrue("READ_COMMITTED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_wait",
                tpbReadCommitted.size() == 4 &&
                        tpbReadCommitted.hasArgument(ISCConstants.isc_tpb_read_committed) &&
                        tpbReadCommitted.hasArgument(ISCConstants.isc_tpb_no_rec_version) &&
                        tpbReadCommitted.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbReadCommitted.hasArgument(ISCConstants.isc_tpb_wait)
        );

        TransactionParameterBuffer tpbRepeatableRead = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ);

        assertTrue("REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_nowait",
                tpbRepeatableRead.size() == 3 &&
                        tpbRepeatableRead.hasArgument(ISCConstants.isc_tpb_concurrency) &&
                        tpbRepeatableRead.hasArgument(ISCConstants.isc_tpb_write) &&
                        tpbRepeatableRead.hasArgument(ISCConstants.isc_tpb_nowait)
        );

        TransactionParameterBuffer tpbSerializable = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_SERIALIZABLE);

        assertTrue("SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_read+isc_tpb_wait",
                tpbSerializable.size() == 3 &&
                        tpbSerializable.hasArgument(ISCConstants.isc_tpb_consistency) &&
                        tpbSerializable.hasArgument(ISCConstants.isc_tpb_read) &&
                        tpbSerializable.hasArgument(ISCConstants.isc_tpb_wait)
        );
    }

    @Test
    public void testSetMapping_updatesMapping() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper();

        TransactionParameterBuffer newReadCommitted =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        mapper.setMapping(Connection.TRANSACTION_READ_COMMITTED, newReadCommitted);
        TransactionParameterBuffer newSerializable =
                FBTpbMapper.processMapping("isc_tpb_consistency,isc_tpb_read,isc_tpb_wait");
        mapper.setMapping(Connection.TRANSACTION_SERIALIZABLE, newSerializable);
        TransactionParameterBuffer newRepeatableRead =
                FBTpbMapper.processMapping("isc_tpb_concurrency,isc_tpb_write,isc_tpb_nowait");
        mapper.setMapping(Connection.TRANSACTION_REPEATABLE_READ, newRepeatableRead);

        assertEquals(newReadCommitted, mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED));
        assertEquals(newSerializable, mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE));
        assertEquals(newRepeatableRead, mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ));
    }

    @Test
    public void testSetMapping_READ_UNCOMMITTED_throwsIllegalArgumentException() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper();
        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        expectedException.expect(IllegalArgumentException.class);

        mapper.setMapping(Connection.TRANSACTION_READ_UNCOMMITTED, newTpb);
    }

    @Test
    public void testSetMapping_NONE_throwsIllegalArgumentException() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper();
        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        expectedException.expect(IllegalArgumentException.class);

        mapper.setMapping(Connection.TRANSACTION_NONE, newTpb);
    }

    @Test
    public void testClone_equalToOriginal() {
        FBTpbMapper original = new FBTpbMapper();
        FBTpbMapper clone = (FBTpbMapper) original.clone();

        assertEquals(original, clone);
    }

    @Test
    public void testModifyingClone_doesNotModifyOriginal() throws Exception {
        FBTpbMapper original = new FBTpbMapper();
        FBTpbMapper clone = (FBTpbMapper) original.clone();

        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        clone.setMapping(Connection.TRANSACTION_READ_COMMITTED, newTpb);

        assertNotEquals(original, clone);
    }
}
