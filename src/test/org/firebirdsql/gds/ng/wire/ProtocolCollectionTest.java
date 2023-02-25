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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.EmptyProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.Version10Descriptor;
import org.firebirdsql.gds.ng.wire.version11.Version11Descriptor;
import org.firebirdsql.gds.ng.wire.version12.Version12Descriptor;
import org.firebirdsql.gds.ng.wire.version13.Version13Descriptor;
import org.firebirdsql.gds.ng.wire.version15.Version15Descriptor;
import org.firebirdsql.gds.ng.wire.version16.Version16Descriptor;
import org.firebirdsql.gds.ng.wire.version18.Version18Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
class ProtocolCollectionTest {

    /**
     * Tests if the available collection contains the expected classes.
     * <p>
     * As the available collection depends on the content of the classpath, this test is not a statement that these
     * classes are the ones loaded in actual usage.
     * </p>
     */
    @Test
    void testGetAvailableProtocols() {
        assertProtocolCollection(ProtocolCollection.getAvailableProtocols(),
                Arrays.asList(
                        Version10Descriptor.class,
                        Version11Descriptor.class,
                        Version12Descriptor.class,
                        Version13Descriptor.class,
                        Version15Descriptor.class,
                        Version16Descriptor.class,
                        Version18Descriptor.class));
    }

    /**
     * Tests if the supported collection contains the expected classes.
     * <p>
     * As the supported collection depends on the content of the classpath, this test is not a statement that these
     * classes are the ones loaded in actual usage.
     * </p>
     */
    @Test
    void testGetSupportedProtocols() {
        assertProtocolCollection(ProtocolCollection.getSupportedProtocols(),
                Arrays.asList(
                        Version13Descriptor.class,
                        Version15Descriptor.class,
                        Version16Descriptor.class,
                        Version18Descriptor.class));
    }

    /**
     * Tests if {@link ProtocolCollection#create(ProtocolDescriptor...)} returns
     * a collection with the supplied descriptors.
     */
    @Test
    void testCreate() {
        // Version 10 with weight 3, type 1
        ProtocolDescriptor alternativeDescriptorV10Weight3_1 = new EmptyProtocolDescriptor(
                WireProtocolConstants.PROTOCOL_VERSION10, WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc, WireProtocolConstants.ptype_batch_send, 3);
        // Version 10 with weight 2
        ProtocolDescriptor alternativeDescriptorV10Weight2 = new EmptyProtocolDescriptor(
                WireProtocolConstants.PROTOCOL_VERSION10, WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc, WireProtocolConstants.ptype_batch_send, 2);
        // Version 10 with weight 3, type 2
        ProtocolDescriptor alternativeDescriptorV10Weight3_2 = new EmptyProtocolDescriptor(
                WireProtocolConstants.PROTOCOL_VERSION10, WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc, WireProtocolConstants.ptype_batch_send, 3);

        ProtocolCollection collection = ProtocolCollection.create(new Version10Descriptor(),
                alternativeDescriptorV10Weight3_1, alternativeDescriptorV10Weight2, alternativeDescriptorV10Weight3_2);

        // We expect the descriptor 'Version 10 with weight 3, type 1' to be returned
        assertProtocolCollection(collection, singletonList(alternativeDescriptorV10Weight3_1.getClass()));
    }

    /**
     * Tests {@link ProtocolCollection#create(ProtocolDescriptor...)} to see if
     * it selects the descriptor with the highest weight when given two
     * ProtocolDescriptors with the same version.
     */
    @Test
    void testCreateSameVersion() {
        ProtocolDescriptor alternativeDescriptor = new EmptyProtocolDescriptor(
                WireProtocolConstants.PROTOCOL_VERSION10, WireProtocolConstants.arch_generic,
                WireProtocolConstants.ptype_rpc, WireProtocolConstants.ptype_batch_send, 2);
        ProtocolCollection collection = ProtocolCollection.create(new Version10Descriptor(), alternativeDescriptor);

        assertProtocolCollection(collection, singletonList(alternativeDescriptor.getClass()));
    }

    /**
     * Tests if {@link ProtocolCollection#getProtocolDescriptor(int)} correctly
     * retrieves the descriptor based on its version.
     */
    @Test
    void testGetProtocolDescriptor_existingVersion() {
        ProtocolDescriptor descriptor = new Version10Descriptor();
        ProtocolCollection collection = ProtocolCollection.create(descriptor);

        assertSame(descriptor, collection.getProtocolDescriptor(descriptor.getVersion()),
                "Unexpected ProtocolDescriptor returned");
    }

    /**
     * Tests if {@link ProtocolCollection#getProtocolDescriptor(int)} correctly
     * returns null when a version is requested that is not contained in the
     * collection.
     */
    @Test
    void testGetProtocolDescriptor_nonExistentVersion() {
        ProtocolDescriptor descriptor = new Version10Descriptor();
        ProtocolCollection collection = ProtocolCollection.create(descriptor);

        assertNull(collection.getProtocolDescriptor(-1), "Unexpected ProtocolDescriptor returned");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGetProtocol_default(String enableProtocol) {
        ProtocolCollection collection = ProtocolCollection.getProtocols(enableProtocol);

        // NOTE: This is an implementation detail that it returns the same instance
        assertSame(ProtocolCollection.getSupportedProtocols(), collection, "expected only supported protocols");
    }

    @ParameterizedTest
    @ValueSource(strings = { "*", " * " })
    void testGetProtocol_all(String allValue) {
        ProtocolCollection collection = ProtocolCollection.getProtocols(allValue);

        // NOTE: This is an implementation detail that it returns the same instance
        assertSame(ProtocolCollection.getAvailableProtocols(), collection, "expected available protocols");
    }

    @ParameterizedTest
    @MethodSource
    void testGetProtocol(String enableProtocol, List<Class<? extends ProtocolDescriptor>> expected) {
        ProtocolCollection collection = ProtocolCollection.getProtocols(enableProtocol);

        assertProtocolCollection(collection, expected);
    }

    public static Stream<Arguments> testGetProtocol() {
        List<Class<? extends ProtocolDescriptor>> supported = new ArrayList<>();
        for (ProtocolDescriptor descriptor : ProtocolCollection.getSupportedProtocols()) {
            supported.add(descriptor.getClass());
        }
        supported = unmodifiableList(supported);

        return Stream.of(
                Arguments.of("10", concat(supported, Version10Descriptor.class)),
                Arguments.of("notAVersion", supported),
                Arguments.of("10,notAVersion, 12",
                        concat(supported, Version10Descriptor.class, Version12Descriptor.class)),
                // * only represent all protocols when used on its own
                Arguments.of("11,*", concat(supported, Version11Descriptor.class)),
                // spaces allowed
                Arguments.of(" 10, 11, 12 ", concat(supported,
                        Version10Descriptor.class, Version11Descriptor.class, Version12Descriptor.class))
        );
    }

    @SafeVarargs
    private static List<Class<? extends ProtocolDescriptor>> concat(
            List<Class<? extends ProtocolDescriptor>> baseList,
            Class<? extends ProtocolDescriptor>... additionalClasses) {
        List<Class<? extends ProtocolDescriptor>> newList = new ArrayList<>(baseList.size() + additionalClasses.length);
        newList.addAll(baseList);
        Collections.addAll(newList, additionalClasses);
        return unmodifiableList(newList);
    }

    private void assertProtocolCollection(ProtocolCollection collection,
                                          List<Class<? extends ProtocolDescriptor>> expected) {
        Set<Class<? extends ProtocolDescriptor>> expectedProtocols = new HashSet<>(expected);
        Set<Class<? extends ProtocolDescriptor>> unexpectedProtocols = new HashSet<>();

        for (ProtocolDescriptor descriptor : collection) {
            Class<? extends ProtocolDescriptor> descriptorClass = descriptor.getClass();
            if (!expectedProtocols.remove(descriptorClass)) {
                unexpectedProtocols.add(descriptorClass);
            }
        }

        assertTrue(expectedProtocols.isEmpty(),
                () -> "One or more expected descriptors not found: " + expectedProtocols);
        assertTrue(unexpectedProtocols.isEmpty(),
                () -> "One or more unexpected descriptors found: " + unexpectedProtocols);
    }
}
