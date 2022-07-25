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
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class RowDescriptorBuilderTest {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    private static final List<FieldDescriptor> TEST_FIELD_DESCRIPTORS;
    static {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(new FieldDescriptor(0, datatypeCoder, 1, 1, 1, 1, "1", "1", "1", "1", "1"));
        fields.add(new FieldDescriptor(1, datatypeCoder, 2, 2, 2, 2, "2", "2", "2", "2", "2"));
        fields.add(new FieldDescriptor(2, datatypeCoder, 3, 3, 3, 3, "3", "3", "3", "3", "3"));

        TEST_FIELD_DESCRIPTORS = Collections.unmodifiableList(fields);
    }

    private static final FieldDescriptor SOURCE = new FieldDescriptor(-1, datatypeCoder, 1, 2, 3, 4, "5", "6", "7", "8", "9");

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
                        .setOriginalTableName("8")
                        .setOwnerName("9")
                        .toFieldDescriptor();

        assertEquals(0, descriptor.getPosition(), "Unexpected Position");
        assertEquals(1, descriptor.getType(), "Unexpected Type");
        assertEquals(2, descriptor.getSubType(), "Unexpected SubType");
        assertEquals(3, descriptor.getScale(), "Unexpected Scale");
        assertEquals(4, descriptor.getLength(), "Unexpected Length");
        assertEquals("5", descriptor.getFieldName(), "Unexpected FieldName");
        assertEquals("6", descriptor.getTableAlias(), "Unexpected TableAlias");
        assertEquals("7", descriptor.getOriginalName(), "Unexpected OriginalName");
        assertEquals("8", descriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertEquals("9", descriptor.getOwnerName(), "Unexpected OwnerName");
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
        assertEquals("8", fieldDescriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertEquals("9", fieldDescriptor.getOwnerName(), "Unexpected OwnerName");
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
        assertNull(fieldDescriptor.getOriginalTableName(), "Unexpected OriginalTableName");
        assertNull(fieldDescriptor.getOwnerName(), "Unexpected OwnerName");
    }

    @Test
    void testEmpty() {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .toRowDescriptor();

        assertEquals(0, rowDescriptor.getCount(), "Unexpected count of fields in RowDescriptor");
    }

    @Test
    void testBasicInitialization() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(TEST_FIELD_DESCRIPTORS.size(), datatypeCoder);
        for (FieldDescriptor fieldDescriptor : TEST_FIELD_DESCRIPTORS) {
            builder.addField(fieldDescriptor);
        }
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
}
