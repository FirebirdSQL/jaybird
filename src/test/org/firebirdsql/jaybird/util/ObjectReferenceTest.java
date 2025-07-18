// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ObjectReference}.
 * <p>
 * Some parts are tested through {@link IdentifierTest} and {@link IdentifierChainTest}.
 * </p>
 */
class ObjectReferenceTest {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Test
    void of_String() {
        final String name = "TestName";
        Identifier identifier = ObjectReference.of(name);
        assertEquals(name, identifier.name());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void of_String_nullOrEmptyOrBlank_throwsIllegalArgumentException(String name) {
        assertThrows(IllegalArgumentException.class, () -> ObjectReference.of(name));
    }

    @Test
    void of_StringArray_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, ObjectReference::of);
    }

    @Test
    void of_StringArray_singleName_returnsIdentifier() {
        String name = "TestName";
        var objectReference = ObjectReference.of(new String[] { name });

        Identifier asIdentifier = assertInstanceOf(Identifier.class, objectReference);
        assertEquals(name, asIdentifier.name());
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 10 })
    void of_StringArray_multipleNames_returnsIdentifierChain(int nameCount) {
        String[] names = IntStream.rangeClosed(1, nameCount).mapToObj(i -> "name" + i).toArray(String[]::new);
        ObjectReference objectReference = ObjectReference.of(names);

        IdentifierChain asChain = assertInstanceOf(IdentifierChain.class, objectReference);
        List<String> namesOfChain = asChain.stream().map(Identifier::name).toList();
        assertEquals(Arrays.asList(names), namesOfChain);
    }

    // Given of(String...) calls of(List<String>), we perform some tests of(List<String>) through of(String...)

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            prefixCount, prefixValue, suffixList
            0,           <NIL>,       NAME1.NAME2
            1,           <NIL>,       NAME1
            2,           '',          NAME1.NAME2
            3,           '',          NAME1.NAME2
            3,           <NIL>,       NAME1.NAME2.NAME3
            """)
    void of_StringList_prefixMayBeNullOrEmpty(int prefixCount, String prefixValue, String suffixList) {
        assertTrue(isNullOrEmpty(prefixValue), "prefixValue must be null or empty");
        List<String> suffixNames = toNames(suffixList);
        var names = new ArrayList<String>(prefixCount + suffixNames.size());
        for (int i = 0; i < prefixCount; i++) {
            names.add(prefixValue);
        }
        names.addAll(suffixNames);

        assertEquals(ObjectReference.of(suffixNames), ObjectReference.of(names));
    }

    @Test
    void of_StringArray_prefixMayNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> ObjectReference.of(" ", "NAME"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void of_StringList_allNullOrEmpty_throwsIllegalArgumentException(boolean useNull) {
        List<String> names = useNull ? Arrays.asList(null, null, null) : Arrays.asList("", "", "");

        assertThrows(IllegalArgumentException.class, () -> ObjectReference.of(names));
    }

    @Test
    void of_StringArr_suffixSingleName_createsIdentifier() {
        var objectReference = ObjectReference.of("", null, "", "NAME");

        Identifier asIdentifier = assertInstanceOf(Identifier.class, objectReference);
        assertEquals("NAME", asIdentifier.name());
    }

    @Test
    void of_StringArr_suffixMultipleNames_createsIdentifierChain() {
        var objectReference = ObjectReference.of("", null, "", "NAME1", "NAME2");

        IdentifierChain asIdentifierChain = assertInstanceOf(IdentifierChain.class, objectReference);
        assertEquals(ObjectReference.of("NAME1", "NAME2"), asIdentifierChain);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schema, object, expectedEmpty
            <NIL>,  TABLE, false
            '',     TABLE, false
            SCHEMA, TABLE, false
            SCHEMA, <NIL>, true
            SCHEMA, '',    true
            <NIL>,  <NIL>, true
            '',     '',    true
            """)
    void testOfTable(@Nullable String schema, String object, boolean expectedEmpty) {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .setOriginalSchema(schema)
                .setOriginalTableName(object)
                .toFieldDescriptor();
        Optional<ObjectReference> optName = ObjectReference.ofTable(fieldDescriptor);
        assertEquals(expectedEmpty, optName.isEmpty(), "empty");
        if (!expectedEmpty) {
            ObjectReference name = optName.get();
            int tableNameIndex;
            if (isNullOrEmpty(schema)) {
                assertEquals(1, name.size(), "size");
                tableNameIndex = 0;
            } else {
                assertEquals(2, name.size(), "size");
                assertEquals(schema, name.at(0).name(), "schema");
                tableNameIndex = 1;
            }
            assertEquals(object, name.at(tableNameIndex).name(), "object");
        }
    }

    private static List<String> toNames(String nameList) {
        return List.of(nameList.split("\\."));
    }

}