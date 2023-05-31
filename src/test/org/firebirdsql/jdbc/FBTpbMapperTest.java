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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
class FBTpbMapperTest {

    private static final String TEST_TPB_MAPPING = "org.firebirdsql.jdbc.test_tpb_mapping";

    @ParameterizedTest
    @ValueSource(strings = { TEST_TPB_MAPPING, "res:" + TEST_TPB_MAPPING })
    void testNewWithMappingFile(String mappingResource) throws Exception {
        FBTpbMapper mapper = new FBTpbMapper(mappingResource, getClass().getClassLoader());

        TransactionParameterBuffer tpbValue = mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED);
        assertTrue(tpbValue.size() == 4 &&
                        tpbValue.hasArgument(isc_tpb_read_committed) &&
                        tpbValue.hasArgument(isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(isc_tpb_write) &&
                        tpbValue.hasArgument(isc_tpb_nowait),
                "READ_COMMITTED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_nowait");

        tpbValue = mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ);
        assertTrue(tpbValue.size() == 3 &&
                        tpbValue.hasArgument(isc_tpb_consistency) &&
                        tpbValue.hasArgument(isc_tpb_write) &&
                        tpbValue.hasArgument(isc_tpb_wait),
                "REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_wait");

        tpbValue = mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE);
        assertTrue(tpbValue.size() == 3 &&
                        tpbValue.hasArgument(isc_tpb_concurrency) &&
                        tpbValue.hasArgument(isc_tpb_write) &&
                        tpbValue.hasArgument(isc_tpb_wait),
                "SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait");
    }

    @Test
    void testNewMappingFileDoesNotExist_throwsSQLException() {
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> new FBTpbMapper(TEST_TPB_MAPPING + "does_not_exist", getClass().getClassLoader()));
    }

    @Test
    void testNewWithIncompleteMap_unspecifiedUseDefaults() throws Exception {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        final Map<String, String> map = new HashMap<>();
        map.put(FBTpbMapper.TRANSACTION_REPEATABLE_READ,
                "isc_tpb_concurrency,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5");

        FBTpbMapper mapper = new FBTpbMapper(map);

        // Check if matches specified
        TransactionParameterBuffer tpbValue = mapper.getMapping(Connection.TRANSACTION_REPEATABLE_READ);
        assertTrue(tpbValue.size() == 4 &&
                        tpbValue.hasArgument(isc_tpb_concurrency) &&
                        tpbValue.hasArgument(isc_tpb_write) &&
                        tpbValue.hasArgument(isc_tpb_wait) &&
                        tpbValue.hasArgument(isc_tpb_lock_timeout) &&
                        tpbValue.getArgumentAsInt(isc_tpb_lock_timeout) == 5,
                "REPEATABLE_READ must be isc_tpb_concurrency+isc_tpb_write+isc_tpb_wait+isc_tpb_lock_timeout=5");
        // Check other isolation levels match default:
        assertEquals(defaultMapper.getMapping(Connection.TRANSACTION_SERIALIZABLE),
                mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE));
        assertEquals(defaultMapper.getMapping(Connection.TRANSACTION_SERIALIZABLE),
                mapper.getMapping(Connection.TRANSACTION_SERIALIZABLE));
    }

    @Test
    void testNewMapContainsInvalidName_throwsSQLException() {
        final Map<String, String> map = new HashMap<>();
        map.put("special", "isc_tpb_concurrency,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5");

        assertThatExceptionOfType(SQLException.class).isThrownBy(() -> new FBTpbMapper(map));
    }

    @Test
    void testProcessMappingWithLockTimeout() throws Exception {
        String testMapping =
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait,isc_tpb_lock_timeout=5";

        TransactionParameterBuffer tpbValue = FBTpbMapper.processMapping(testMapping);

        assertTrue(tpbValue.size() == 5 &&
                        tpbValue.hasArgument(isc_tpb_read_committed) &&
                        tpbValue.hasArgument(isc_tpb_no_rec_version) &&
                        tpbValue.hasArgument(isc_tpb_write) &&
                        tpbValue.hasArgument(isc_tpb_wait) &&
                        tpbValue.hasArgument(isc_tpb_lock_timeout) &&
                        tpbValue.getArgumentAsInt(isc_tpb_lock_timeout) == 5,
                "must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_wait,5");
    }

    @Test
    void testProcessMapping_TokenIsNotATpbArgument_throwsSQLException() {
        String testMapping = "not_a_tpb_argument";

        assertThatExceptionOfType(SQLException.class).isThrownBy(() -> FBTpbMapper.processMapping(testMapping));
    }

    @Test
    void testProcessMapping_ValueIsNotAnInteger_throwsSQLException() {
        String testMapping = "isc_tpb_lock_timeout=x";

        assertThatExceptionOfType(SQLException.class).isThrownBy(() -> FBTpbMapper.processMapping(testMapping));
    }

    @Test
    void testInitialDefaultTransactionIsolationLevel_READ_COMMITTED() {
        FBTpbMapper mapper = new FBTpbMapper();

        assertEquals(Connection.TRANSACTION_READ_COMMITTED, mapper.getDefaultTransactionIsolation());
    }

    @Test
    void testSetDefaultTransactionIsolationLevel() {
        FBTpbMapper mapper = new FBTpbMapper();

        mapper.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        assertEquals(Connection.TRANSACTION_SERIALIZABLE, mapper.getDefaultTransactionIsolation());
    }

    @Test
    void testGetDefaultMapping_returnsMappingOfDefaultTransactionIsolationLevel() {
        FBTpbMapper mapper = new FBTpbMapper();

        assertEquals(mapper.getMapping(mapper.getDefaultTransactionIsolation()), mapper.getDefaultMapping(),
                "Default mapping doesn't match initial default isolation level");

        mapper.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        assertEquals(mapper.getMapping(mapper.getDefaultTransactionIsolation()), mapper.getDefaultMapping(),
                "Default mapping doesn't match default isolation set to TRANSACTION_SERIALIZABLE");
    }

    @Test
    void testGetDefaultMapperReturnsNewInstanceOnEveryInvocation() {
        FBTpbMapper instance1 = FBTpbMapper.getDefaultMapper();
        FBTpbMapper instance2 = FBTpbMapper.getDefaultMapper();

        assertEquals(instance1, instance2, "Instances returns from getDefaultMapper should be equals");
        assertNotSame(instance1, instance2,
                "Instances returns from getDefaultMapper should not have the same identity");
    }

    @Test
    void testGetTransactionIsolationName() {
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
    void testGetTransactionIsolationName_throwsIllegalArgumentExceptionForUnsupportedValue() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FBTpbMapper.getTransactionIsolationName(-1));
    }

    @Test
    void testGetTransactionIsolationLevel() {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testGetTransactionIsolationLevel_throwsIllegalArgumentExceptionForUnsupportedValue() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FBTpbMapper.getTransactionIsolationLevel("asdf"));
    }

    @Test
    void testGetMappingForREAD_UNCOMMITED_Returns_READ_COMMITTED() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();
        final TransactionParameterBuffer readCommitted = defaultMapper
                .getMapping(Connection.TRANSACTION_READ_COMMITTED);

        assertEquals(readCommitted, defaultMapper.getMapping(Connection.TRANSACTION_READ_UNCOMMITTED),
                "Expected same mapping for READ_UNCOMMITTED as for READ_COMMITTED");
    }

    @Test
    void testGetMappingForNONE_throwsIllegalArgumentException() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> defaultMapper.getMapping(Connection.TRANSACTION_NONE));
    }

    @Test
    void testGetMappingForUnsupportedValue_throwsIllegalArgumentException() {
        final FBTpbMapper defaultMapper = FBTpbMapper.getDefaultMapper();

        assertThatIllegalArgumentException().isThrownBy(() -> defaultMapper.getMapping(-1));
    }

    @Test
    void testProcessMappingToConnectionProperties() throws Exception {
        final Properties props = new Properties();
        props.setProperty(FBTpbMapper.TRANSACTION_READ_COMMITTED,
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        props.setProperty(FBTpbMapper.TRANSACTION_SERIALIZABLE, "isc_tpb_consistency,isc_tpb_read,isc_tpb_wait");
        props.setProperty(FBTpbMapper.TRANSACTION_REPEATABLE_READ, "isc_tpb_concurrency,isc_tpb_write,isc_tpb_nowait");
        props.setProperty("not_a_transaction_isolation_level", "some_value");
        FBConnectionProperties connectionProps = new FBConnectionProperties();

        assertNoTransactionPropsSet(connectionProps);

        FBTpbMapper.processMapping(connectionProps, props);

        assertProcessMappingResult(connectionProps);
    }

    private void assertNoTransactionPropsSet(FBConnectionProperties connectionProps) {
        assertNull(connectionProps.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED),
                "TRANSACTION_READ_COMMITTED should not have a TPB before processing");
        assertNull(connectionProps.getTransactionParameters(Connection.TRANSACTION_SERIALIZABLE),
                "TRANSACTION_SERIALIZABLE should not have a TPB before processing");
        assertNull(connectionProps.getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ),
                "TRANSACTION_REPEATABLE_READ should not have a TPB before processing");
    }

    private void assertProcessMappingResult(FBConnectionProperties connectionProps) {
        TransactionParameterBuffer tpbReadCommitted = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);
        assertTrue(tpbReadCommitted.size() == 4 &&
                        tpbReadCommitted.hasArgument(isc_tpb_read_committed) &&
                        tpbReadCommitted.hasArgument(isc_tpb_no_rec_version) &&
                        tpbReadCommitted.hasArgument(isc_tpb_write) &&
                        tpbReadCommitted.hasArgument(isc_tpb_wait),
                "READ_COMMITTED must be isc_tpb_read_committed+isc_tpb_no_rec_version+isc_tpb_write+isc_tpb_wait");

        TransactionParameterBuffer tpbRepeatableRead = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_REPEATABLE_READ);
        assertTrue(tpbRepeatableRead.size() == 3 &&
                        tpbRepeatableRead.hasArgument(isc_tpb_concurrency) &&
                        tpbRepeatableRead.hasArgument(isc_tpb_write) &&
                        tpbRepeatableRead.hasArgument(isc_tpb_nowait),
                "REPEATABLE_READ must be isc_tpb_consistency+isc_tpb_write+isc_tpb_nowait");

        TransactionParameterBuffer tpbSerializable = connectionProps
                .getTransactionParameters(Connection.TRANSACTION_SERIALIZABLE);
        assertTrue(tpbSerializable.size() == 3 &&
                        tpbSerializable.hasArgument(isc_tpb_consistency) &&
                        tpbSerializable.hasArgument(isc_tpb_read) &&
                        tpbSerializable.hasArgument(isc_tpb_wait),
                "SERIALIZABLE must be isc_tpb_concurrency+isc_tpb_read+isc_tpb_wait");
    }

    @Test
    void testProcessMappingToConnectionProperties_withMap() throws Exception {
        final Map<String, String> props = new HashMap<>();
        props.put(FBTpbMapper.TRANSACTION_READ_COMMITTED,
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        props.put(FBTpbMapper.TRANSACTION_SERIALIZABLE, "isc_tpb_consistency,isc_tpb_read,isc_tpb_wait");
        props.put(FBTpbMapper.TRANSACTION_REPEATABLE_READ, "isc_tpb_concurrency,isc_tpb_write,isc_tpb_nowait");
        props.put("not_a_transaction_isolation_level", "some_value");
        FBConnectionProperties connectionProps = new FBConnectionProperties();

        assertNoTransactionPropsSet(connectionProps);

        FBTpbMapper.processMapping(connectionProps, props);

        assertProcessMappingResult(connectionProps);
    }

    @Test
    void testSetMapping_updatesMapping() throws Exception {
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
    void testSetMapping_READ_UNCOMMITTED_throwsIllegalArgumentException() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper();
        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> mapper.setMapping(Connection.TRANSACTION_READ_UNCOMMITTED, newTpb));
    }

    @Test
    void testSetMapping_NONE_throwsIllegalArgumentException() throws Exception {
        FBTpbMapper mapper = new FBTpbMapper();
        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> mapper.setMapping(Connection.TRANSACTION_NONE, newTpb));
    }

    @Test
    void testClone_equalToOriginal() {
        FBTpbMapper original = new FBTpbMapper();
        FBTpbMapper clone = (FBTpbMapper) original.clone();

        assertEquals(original, clone);
    }

    @Test
    void testModifyingClone_doesNotModifyOriginal() throws Exception {
        FBTpbMapper original = new FBTpbMapper();
        FBTpbMapper clone = (FBTpbMapper) original.clone();

        TransactionParameterBuffer newTpb =
                FBTpbMapper.processMapping("isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_wait");
        clone.setMapping(Connection.TRANSACTION_READ_COMMITTED, newTpb);

        assertNotEquals(original, clone);
    }
}
