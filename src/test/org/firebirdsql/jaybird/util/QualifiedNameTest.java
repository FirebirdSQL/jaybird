// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QualifiedNameTest {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @ParameterizedTest
    @NullAndEmptySource
    void nullOrEmptySchemaReportedAsEmpty(@Nullable String schema) {
        var name = new QualifiedName(schema, "COLUMN");
        assertEquals("COLUMN", name.object());
        assertEquals("", name.schema());
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = " ")
    void blankObject_throwsException(String object) {
        assertThrows(IllegalArgumentException.class, () -> new QualifiedName("SCHEMA_NAME", object));
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void nullObject_throwsException() {
        assertThrows(NullPointerException.class, () -> new QualifiedName("SCHEMA_NAME", null));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schema, object, quoteStrategy, expectedLength, expectedIdentifierChain
            <NIL>,  OBJECT, DIALECT_3,     8,              "OBJECT"
            '',     OBJECT, DIALECT_1,     8,              OBJECT
            SCHEMA, OBJECT, DIALECT_3,     17,             "SCHEMA"."OBJECT"
            SCHEMA, OBJECT, DIALECT_1,     17,             SCHEMA.OBJECT
            # Weird case
            SCHEM"QUOTE, OBJ"QUOTE, DIALECT_3, 25,         "SCHEM""QUOTE"."OBJ""QUOTE"
            """)
    void identifierChainProduction(@Nullable String schema, String object, QuoteStrategy quoteStrategy,
            int expectedLength, String expectedIdentifierChain) {
        var name = new QualifiedName(schema, object);
        assertAll(
                () -> assertEquals(expectedLength, name.estimatedLength(), "estimatedLength"),
                () -> assertEquals(expectedIdentifierChain, name.toString(quoteStrategy), "toString(QuoteStrategy)"),
                () -> {
                    var sb = new StringBuilder(name.estimatedLength());
                    name.append(sb, quoteStrategy);
                    assertEquals(expectedIdentifierChain, sb.toString(), "append(StringBuilder, QuoteStrategy)");
                }
        );
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
    void testOf(@Nullable String schema, String object, boolean expectedEmpty) {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .setOriginalSchema(schema)
                .setOriginalTableName(object)
                .toFieldDescriptor();
        Optional<QualifiedName> optName = QualifiedName.of(fieldDescriptor);
        assertEquals(expectedEmpty, optName.isEmpty(), "empty");
        if (!expectedEmpty) {
            QualifiedName name = optName.get();
            String expectedSchema = schema == null || schema.isBlank() ? "" : schema;
            assertEquals(expectedSchema, name.schema(), "schema");
            assertEquals(object, name.object(), "object");
        }
    }

}