/*
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestRowDescriptorBuilder {

    private static final DatatypeCoder datatypeCoder =
            new DefaultDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

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
    public void testEmptyField() {
        FieldDescriptor descriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .toFieldDescriptor();

        assertEquals("Unexpected Position", 0, descriptor.getPosition());
        assertEquals("Unexpected Type", 0, descriptor.getType());
        assertEquals("Unexpected SubType", 0, descriptor.getSubType());
        assertEquals("Unexpected Scale", 0, descriptor.getScale());
        assertEquals("Unexpected Length", 0, descriptor.getLength());
        assertNull("Unexpected FieldName", descriptor.getFieldName());
        assertNull("Unexpected TableAlias", descriptor.getTableAlias());
        assertNull("Unexpected OriginalName", descriptor.getOriginalName());
        assertNull("Unexpected OriginalTableName", descriptor.getOriginalTableName());
        assertNull("Unexpected OwnerName", descriptor.getOwnerName());
    }

    @Test
    public void testBasicFieldInitialization() {
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

        assertEquals("Unexpected Position", 0, descriptor.getPosition());
        assertEquals("Unexpected Type", 1, descriptor.getType());
        assertEquals("Unexpected SubType", 2, descriptor.getSubType());
        assertEquals("Unexpected Scale", 3, descriptor.getScale());
        assertEquals("Unexpected Length", 4, descriptor.getLength());
        assertEquals("Unexpected FieldName", "5", descriptor.getFieldName());
        assertEquals("Unexpected TableAlias", "6", descriptor.getTableAlias());
        assertEquals("Unexpected OriginalName", "7", descriptor.getOriginalName());
        assertEquals("Unexpected OriginalTableName", "8", descriptor.getOriginalTableName());
        assertEquals("Unexpected OwnerName", "9", descriptor.getOwnerName());
    }

    @Test
    public void testCopyFrom() {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .copyFieldFrom(SOURCE)
                .toFieldDescriptor();

        assertEquals("Unexpected Position", 0, fieldDescriptor.getPosition());
        assertEquals("Unexpected Type", 1, fieldDescriptor.getType());
        assertEquals("Unexpected SubType", 2, fieldDescriptor.getSubType());
        assertEquals("Unexpected Scale", 3, fieldDescriptor.getScale());
        assertEquals("Unexpected Length", 4, fieldDescriptor.getLength());
        assertEquals("Unexpected FieldName", "5", fieldDescriptor.getFieldName());
        assertEquals("Unexpected TableAlias", "6", fieldDescriptor.getTableAlias());
        assertEquals("Unexpected OriginalName", "7", fieldDescriptor.getOriginalName());
        assertEquals("Unexpected OriginalTableName", "8", fieldDescriptor.getOriginalTableName());
        assertEquals("Unexpected OwnerName", "9", fieldDescriptor.getOwnerName());
    }

    @Test
    public void testResetField() {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .copyFieldFrom(SOURCE)
                .resetField()
                .toFieldDescriptor();

        assertEquals("Unexpected Position", 0, fieldDescriptor.getPosition());
        assertEquals("Unexpected Type", 0, fieldDescriptor.getType());
        assertEquals("Unexpected SubType", 0, fieldDescriptor.getSubType());
        assertEquals("Unexpected Scale", 0, fieldDescriptor.getScale());
        assertEquals("Unexpected Length", 0, fieldDescriptor.getLength());
        assertNull("Unexpected FieldName", fieldDescriptor.getFieldName());
        assertNull("Unexpected TableAlias", fieldDescriptor.getTableAlias());
        assertNull("Unexpected OriginalName", fieldDescriptor.getOriginalName());
        assertNull("Unexpected OriginalTableName", fieldDescriptor.getOriginalTableName());
        assertNull("Unexpected OwnerName", fieldDescriptor.getOwnerName());
    }

    @Test
    public void testEmpty() {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(0, datatypeCoder)
                .toRowDescriptor();

        assertEquals("Unexpected count of fields in RowDescriptor", 0, rowDescriptor.getCount());
    }

    @Test
    public void testBasicInitialization() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(TEST_FIELD_DESCRIPTORS.size(), datatypeCoder);
        for (FieldDescriptor fieldDescriptor : TEST_FIELD_DESCRIPTORS) {
            builder.addField(fieldDescriptor);
        }
        RowDescriptor rowDescriptor = builder.toRowDescriptor();

        assertEquals("Unexpected count of fields in RowDescriptor", TEST_FIELD_DESCRIPTORS.size(), rowDescriptor.getCount());
        assertEquals("Unexpected list content", TEST_FIELD_DESCRIPTORS, rowDescriptor.getFieldDescriptors());
    }

    @Test
    public void testAddingFieldsChained() {
        RowDescriptorBuilder descriptorBuilder = new RowDescriptorBuilder(2, datatypeCoder)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field1")
                .setTableAlias("Alias1")
                .addField();

        assertEquals("Unexpected value for getCurrentFieldIndex()", 1, descriptorBuilder.getCurrentFieldIndex());
        descriptorBuilder
                .setType(2)
                .setFieldName("Field2")
                .addField();
        assertEquals("Unexpected value for getCurrentFieldIndex()", 2, descriptorBuilder.getCurrentFieldIndex());

        RowDescriptor descriptor = descriptorBuilder.toRowDescriptor();

        assertEquals("Unexpected count of fields in RowDescriptor", 2, descriptor.getCount());
        FieldDescriptor field1 = descriptor.getFieldDescriptor(0);
        assertEquals("Field1.getPosition()", 0, field1.getPosition());
        assertEquals("Field1.getType()", 1, field1.getType());
        assertEquals("Field1.getSubType()", 1, field1.getSubType());
        assertEquals("Field1.getFieldName()", "Field1", field1.getFieldName());
        assertEquals("Field1.getTableAlias()", "Alias1", field1.getTableAlias());
        FieldDescriptor field2 = descriptor.getFieldDescriptor(1);
        assertEquals("Field2.getPosition()", 1, field2.getPosition());
        assertEquals("Field2.getType()", 2, field2.getType());
        assertEquals("Field2.getSubType()", 0, field2.getSubType());
        assertEquals("Field2.getFieldName()", "Field2", field2.getFieldName());
        assertEquals("Field2.getTableAlias()", null, field2.getTableAlias());
    }

    @Test
    public void testAddingFieldsChained_DifferentOrder() {
        RowDescriptorBuilder descriptorBuilder = new RowDescriptorBuilder(2, datatypeCoder)
                .setFieldIndex(1)
                .setType(1)
                .setSubType(1)
                .setFieldName("Field1")
                .setTableAlias("Alias1")
                .addField();

        assertEquals("Unexpected value for getCurrentFieldIndex()", 2, descriptorBuilder.getCurrentFieldIndex());
        descriptorBuilder
                .setFieldIndex(0)
                .setType(2)
                .setFieldName("Field2")
                .addField();
        assertEquals("Unexpected value for getCurrentFieldIndex()", 1, descriptorBuilder.getCurrentFieldIndex());

        RowDescriptor descriptor = descriptorBuilder.toRowDescriptor();

        assertEquals("Unexpected count of fields in RowDescriptor", 2, descriptor.getCount());
        FieldDescriptor field1 = descriptor.getFieldDescriptor(1);
        assertEquals("Field1.getPosition()", 1, field1.getPosition());
        assertEquals("Field1.getType()", 1, field1.getType());
        assertEquals("Field1.getSubType()", 1, field1.getSubType());
        assertEquals("Field1.getFieldName()", "Field1", field1.getFieldName());
        assertEquals("Field1.getTableAlias()", "Alias1", field1.getTableAlias());
        FieldDescriptor field2 = descriptor.getFieldDescriptor(0);
        assertEquals("Field2.getPosition()", 0, field2.getPosition());
        assertEquals("Field2.getType()", 2, field2.getType());
        assertEquals("Field2.getSubType()", 0, field2.getSubType());
        assertEquals("Field2.getFieldName()", "Field2", field2.getFieldName());
        assertEquals("Field2.getTableAlias()", null, field2.getTableAlias());
    }
}
