// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
class RowDescriptorBuilderTest {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    private static final List<FieldDescriptor> TEST_FIELD_DESCRIPTORS = List.of(
            new FieldDescriptor(0, datatypeCoder, 1, 1, 1, 1, "1", "1", "1", "1", "1", "1"),
            new FieldDescriptor(1, datatypeCoder, 2, 2, 2, 2, "2", "2", "2", "2", "2", "2"),
            new FieldDescriptor(2, datatypeCoder, 3, 3, 3, 3, "3", "3", "3", "3", "3", "3"));

    private static final FieldDescriptor SOURCE =
            new FieldDescriptor(-1, datatypeCoder, 1, 2, 3, 4, "5", "6", "7", "8", "9", "10");

    @Test
    void testEmptyField() {
        FieldDescriptor descriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .toFieldDescriptor();

        assertEquals(0, descriptor.getPosition(), "Unexpected Position");
        assertEquals(0, descriptor.getType(), "Unexpected Type");
        assertEquals(0, descriptor.getSubType(), "Unexpected SubType");
        assertEquals(0, descriptor.getScale(), "Unexpected Scale");
        assertEquals(0, descriptor.getLength(), "Unexpected Length");
        assertNull(descriptor.getFieldName(), "Unexpected FieldName");
        assertNull(descriptor.getTableAlias(), "Unexpected TableAlias");
        assertNull(descriptor.getOriginalName(), "Unexpected OriginalName");
        assertNull(descriptor.getOriginalSchema(), "Unexpected OriginalSchema");
        assertNull(descriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertNull(descriptor.getOwnerName(), "Unexpected OwnerName");
    }

    @Test
    void testBasicFieldInitialization() {
        FieldDescriptor descriptor =
                new RowDescriptorBuilder(1, datatypeCoder)
                        .setType(1)
                        .setSubType(2)
                        .setScale(3)
                        .setLength(4)
                        .setFieldName("5")
                        .setTableAlias("6")
                        .setOriginalName("7")
                        .setOriginalSchema("8")
                        .setOriginalTableName("9")
                        .setOwnerName("10")
                        .toFieldDescriptor();

        assertEquals(0, descriptor.getPosition(), "Unexpected Position");
        assertEquals(1, descriptor.getType(), "Unexpected Type");
        assertEquals(2, descriptor.getSubType(), "Unexpected SubType");
        assertEquals(3, descriptor.getScale(), "Unexpected Scale");
        assertEquals(4, descriptor.getLength(), "Unexpected Length");
        assertEquals("5", descriptor.getFieldName(), "Unexpected FieldName");
        assertEquals("6", descriptor.getTableAlias(), "Unexpected TableAlias");
        assertEquals("7", descriptor.getOriginalName(), "Unexpected OriginalName");
        assertEquals("8", descriptor.getOriginalSchema(), "Unexpected OriginalSchema");
        assertEquals("9", descriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertEquals("10", descriptor.getOwnerName(), "Unexpected OwnerName");
    }

    @Test
    void testCopyFrom() {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .copyFieldFrom(SOURCE)
                .toFieldDescriptor();

        assertEquals(0, fieldDescriptor.getPosition(), "Unexpected Position");
        assertEquals(1, fieldDescriptor.getType(), "Unexpected Type");
        assertEquals(2, fieldDescriptor.getSubType(), "Unexpected SubType");
        assertEquals(3, fieldDescriptor.getScale(), "Unexpected Scale");
        assertEquals(4, fieldDescriptor.getLength(), "Unexpected Length");
        assertEquals("5", fieldDescriptor.getFieldName(), "Unexpected FieldName");
        assertEquals("6", fieldDescriptor.getTableAlias(), "Unexpected TableAlias");
        assertEquals("7", fieldDescriptor.getOriginalName(), "Unexpected OriginalName");
        assertEquals("8", fieldDescriptor.getOriginalSchema(), "Unexpected OriginalSchema");
        assertEquals("9", fieldDescriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertEquals("10", fieldDescriptor.getOwnerName(), "Unexpected OwnerName");
    }

    @Test
    void testResetField() {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .copyFieldFrom(SOURCE)
                .resetField()
                .toFieldDescriptor();

        assertEquals(0, fieldDescriptor.getPosition(), "Unexpected Position");
        assertEquals(0, fieldDescriptor.getType(), "Unexpected Type");
        assertEquals(0, fieldDescriptor.getSubType(), "Unexpected SubType");
        assertEquals(0, fieldDescriptor.getScale(), "Unexpected Scale");
        assertEquals(0, fieldDescriptor.getLength(), "Unexpected Length");
        assertNull(fieldDescriptor.getFieldName(), "Unexpected FieldName");
        assertNull(fieldDescriptor.getTableAlias(), "Unexpected TableAlias");
        assertNull(fieldDescriptor.getOriginalName(), "Unexpected OriginalName");
        assertNull(fieldDescriptor.getOriginalSchema(), "Unexpected OriginalSchema");
        assertNull(fieldDescriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertNull(fieldDescriptor.getOwnerName(), "Unexpected OwnerName");
    }

    @Test
    void testEmpty() {
        var builder = new RowDescriptorBuilder(0, datatypeCoder);
        assertTrue(builder.isComplete(), "Unexpected incomplete");

        RowDescriptor rowDescriptor = builder
                .toRowDescriptor();

        assertEquals(0, rowDescriptor.getCount(), "Unexpected count of fields in RowDescriptor");
    }

    @Test
    void testBasicInitialization() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(TEST_FIELD_DESCRIPTORS.size(), datatypeCoder);
        for (FieldDescriptor fieldDescriptor : TEST_FIELD_DESCRIPTORS) {
            builder.addField(fieldDescriptor);
        }
        assertTrue(builder.isComplete(), "Unexpected incomplete");

        RowDescriptor rowDescriptor = builder.toRowDescriptor();

        assertEquals(TEST_FIELD_DESCRIPTORS.size(), rowDescriptor.getCount(), "Unexpected count of fields in RowDescriptor");
        assertEquals(TEST_FIELD_DESCRIPTORS, rowDescriptor.getFieldDescriptors(), "Unexpected list content");
    }

    @Test
    void testAddingFieldsChained() {
        RowDescriptorBuilder descriptorBuilder = new RowDescriptorBuilder(2, datatypeCoder)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field1")
                .setTableAlias("Alias1")
                .addField();

        assertEquals(1, descriptorBuilder.getCurrentFieldIndex(), "Unexpected value for getCurrentFieldIndex()");
        descriptorBuilder
                .setType(2)
                .setFieldName("Field2")
                .addField();
        assertEquals(2, descriptorBuilder.getCurrentFieldIndex(), "Unexpected value for getCurrentFieldIndex()");

        RowDescriptor descriptor = descriptorBuilder.toRowDescriptor();

        assertEquals(2, descriptor.getCount(), "Unexpected count of fields in RowDescriptor");
        FieldDescriptor field1 = descriptor.getFieldDescriptor(0);
        assertEquals(0, field1.getPosition(), "Field1.getPosition()");
        assertEquals(1, field1.getType(), "Field1.getType()");
        assertEquals(1, field1.getSubType(), "Field1.getSubType()");
        assertEquals("Field1", field1.getFieldName(), "Field1.getFieldName()");
        assertEquals("Alias1", field1.getTableAlias(), "Field1.getTableAlias()");
        FieldDescriptor field2 = descriptor.getFieldDescriptor(1);
        assertEquals(1, field2.getPosition(), "Field2.getPosition()");
        assertEquals(2, field2.getType(), "Field2.getType()");
        assertEquals(0, field2.getSubType(), "Field2.getSubType()");
        assertEquals("Field2", field2.getFieldName(), "Field2.getFieldName()");
        assertNull(field2.getTableAlias(), "Field2.getTableAlias()");
    }

    @Test
    void testAddingFieldsChained_DifferentOrder() {
        RowDescriptorBuilder descriptorBuilder = new RowDescriptorBuilder(2, datatypeCoder)
                .setFieldIndex(1)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field1")
                .setTableAlias("Alias1")
                .addField();

        assertEquals(2, descriptorBuilder.getCurrentFieldIndex(), "Unexpected value for getCurrentFieldIndex()");
        descriptorBuilder
                .setFieldIndex(0)
                .setType(2)
                .setFieldName("Field2")
                .addField();
        assertEquals(1, descriptorBuilder.getCurrentFieldIndex(), "Unexpected value for getCurrentFieldIndex()");

        RowDescriptor descriptor = descriptorBuilder.toRowDescriptor();

        assertEquals(2, descriptor.getCount(), "Unexpected count of fields in RowDescriptor");
        FieldDescriptor field1 = descriptor.getFieldDescriptor(1);
        assertEquals(1, field1.getPosition(), "Field1.getPosition()");
        assertEquals(1, field1.getType(), "Field1.getType()");
        assertEquals(1, field1.getSubType(), "Field1.getSubType()");
        assertEquals("Field1", field1.getFieldName(), "Field1.getFieldName()");
        assertEquals("Alias1", field1.getTableAlias(), "Field1.getTableAlias()");
        assertFalse(field1.isDbKey(), "Field1.isDbKey()");
        FieldDescriptor field2 = descriptor.getFieldDescriptor(0);
        assertEquals(0, field2.getPosition(), "Field2.getPosition()");
        assertEquals(2, field2.getType(), "Field2.getType()");
        assertEquals(0, field2.getSubType(), "Field2.getSubType()");
        assertEquals("Field2", field2.getFieldName(), "Field2.getFieldName()");
        assertNull(field2.getTableAlias(), "Field2.getTableAlias()");
        assertFalse(field1.isDbKey(), "Field2.isDbKey()");
    }

    @Test
    void testIsDbKey() {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .setFieldIndex(0)
                .setType(ISCConstants.SQL_TEXT)
                .setSubType(ISCConstants.CS_BINARY)
                .setOriginalName("DB_KEY")
                .toFieldDescriptor();

        assertTrue(fieldDescriptor.isDbKey(), "isDbKey()");
    }

    @Test
    void toRowDescriptor_incomplete_noneDefined() {
        var builder = new RowDescriptorBuilder(1, datatypeCoder);

        assertFalse(builder.isComplete(), "Unexpected complete");
        var exception = assertThrows(IllegalStateException.class, builder::toRowDescriptor);
        assertEquals("Fields at indices [0] have not been defined", exception.getMessage());
    }

    @Test
    void toRowDescriptor_incomplete_firstDefined() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(2, datatypeCoder)
                .at(0)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field1")
                .setTableAlias("Alias1")
                .addField();

        assertFalse(builder.isComplete(), "Unexpected complete");
        var exception = assertThrows(IllegalStateException.class, builder::toRowDescriptor);
        assertEquals("Fields at indices [1] have not been defined", exception.getMessage());
    }

    @Test
    void toRowDescriptor_incomplete_lastDefined() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(3, datatypeCoder)
                .at(2)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field2")
                .addField();

        assertFalse(builder.isComplete(), "Unexpected complete");
        var exception = assertThrows(IllegalStateException.class, builder::toRowDescriptor);
        assertEquals("Fields at indices [0, 1] have not been defined", exception.getMessage());
    }

}
