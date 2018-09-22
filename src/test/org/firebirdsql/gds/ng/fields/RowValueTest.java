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
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class RowValueTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
    private static final RowDescriptor EMPTY_ROW_DESCRIPTOR = RowDescriptor.empty(datatypeCoder);
    private static final List<FieldDescriptor> TEST_FIELD_DESCRIPTORS;
    static {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(new FieldDescriptor(0, datatypeCoder, 1, 1, 1, 1, "A", "1", "1", "1", "1"));
        fields.add(new FieldDescriptor(1, datatypeCoder, 2, 2, 2, 2, "B", "2", "2", "2", "2"));
        fields.add(new FieldDescriptor(2, datatypeCoder, 3, 3, 3, 3, "C", "3", "3", "3", "3"));

        TEST_FIELD_DESCRIPTORS = Collections.unmodifiableList(fields);
    }

    @Test
    public void testDefaultFor_emptyRowDescriptor_returns_EMPTY_ROW_VALUE() {
        assertSame(RowValue.EMPTY_ROW_VALUE, RowValue.defaultFor(EMPTY_ROW_DESCRIPTOR));
    }

    @Test
    public void testDefaultFor_fieldsNotInitialized() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();

        RowValue rowValue = RowValue.defaultFor(rowDescriptor);

        assertEquals("count", TEST_FIELD_DESCRIPTORS.size(), rowValue.getCount());
        for (int idx = 0; idx < TEST_FIELD_DESCRIPTORS.size(); idx++) {
            assertNull(idx + " expected null fieldData", rowValue.getFieldData(idx));
            assertFalse(idx + " expected uninitialized", rowValue.isInitialized(idx));
        }
    }

    @Test
    public void setFieldData_null_marksFieldAsInitialized() {
        checkSetFieldDataMarksFieldAsInitialized(null);
    }

    @Test
    public void setFieldData_emptyArray_marksFieldAsInitialized() {
        checkSetFieldDataMarksFieldAsInitialized(new byte[0]);
    }

    @Test
    public void setFieldData_nonEmptyArray_marksFieldAsInitialized() {
        checkSetFieldDataMarksFieldAsInitialized(new byte[] { 1, 2, 3, 4});
    }

    private void checkSetFieldDataMarksFieldAsInitialized(byte[] value) {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .addField(TEST_FIELD_DESCRIPTORS.get(0))
                .toRowDescriptor();

        RowValue rowValue = RowValue.defaultFor(rowDescriptor);

        assumeFalse("field should not be initialized", rowValue.isInitialized(0));
        rowValue.setFieldData(0, value);

        assertTrue("field should be initialized", rowValue.isInitialized(0));
    }

    @Test
    public void of_byteArrayOnly_empty_returns_EMPTY_ROW_VALUE() {
        assertSame(RowValue.EMPTY_ROW_VALUE, RowValue.of());
    }

    @Test
    public void of_byteArrayOnly_initializesWithData() {
        RowValue rowValue = RowValue.of(new byte[] { 0 }, new byte[] { 1 }, new byte[] { 2 });

        assertEquals(3, rowValue.getCount());
        for (int idx = 0; idx < 3; idx++) {
            assertTrue(idx + " expected initialized", rowValue.isInitialized(idx));
            assertArrayEquals(idx + " unexpected value", new byte[] { (byte) idx }, rowValue.getFieldData(idx));
        }
    }

    @Test
    public void of_withDescriptor_empty_returns_EMPTY_ROW_VALUE() {
        assertSame(RowValue.EMPTY_ROW_VALUE, RowValue.of(EMPTY_ROW_DESCRIPTOR));
    }

    @Test
    public void of_withDescriptor_initializesWithData() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();

        RowValue rowValue = RowValue.of(rowDescriptor, new byte[] { 0 }, new byte[] { 1 }, new byte[] { 2 });

        assertEquals(3, rowValue.getCount());
        for (int idx = 0; idx < 3; idx++) {
            assertTrue(idx + " expected initialized", rowValue.isInitialized(idx));
            assertArrayEquals(idx + " unexpected value", new byte[] { (byte) idx }, rowValue.getFieldData(idx));
        }
    }

    @Test
    public void of_withDescriptor_countMismatch_throwsIllegalArgumentException() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();

        expectedException.expect(IllegalArgumentException.class);

        RowValue.of(rowDescriptor, new byte[] { 0 });
    }

    @Test
    public void reset_clearsAndMarksAsUninitialized() {
        RowValue rowValue = RowValue.of(new byte[] { 0 }, new byte[] { 1 }, new byte[] { 2 });

        rowValue.reset();

        for (int idx = 0; idx < 3; idx++) {
            assertFalse(idx + " expected initialized", rowValue.isInitialized(idx));
            assertNull(idx + " expected null", rowValue.getFieldData(idx));
        }
    }

    @Test
    public void reset_onEmpty_noException() {
        RowValue.EMPTY_ROW_VALUE.reset();
    }

    @Test
    public void initializeFieldsMarksUninitializedFieldsAsInitialized_fullyInitialized() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();
        RowValue rowValue = RowValue.of(rowDescriptor, new byte[] { 0 }, new byte[] { 1 }, new byte[] { 2 });

        rowValue.initializeFields();

        for (int idx = 0; idx < 3; idx++) {
            assertTrue(idx + " expected initialized", rowValue.isInitialized(idx));
            assertArrayEquals(idx + " unexpected value", new byte[] { (byte) idx }, rowValue.getFieldData(idx));
        }
    }

    @Test
    public void initializeFieldsMarksUninitializedFieldsAsInitialized_fullyUninitialized() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();
        RowValue rowValue = RowValue.defaultFor(rowDescriptor);

        rowValue.initializeFields();

        for (int idx = 0; idx < 3; idx++) {
            assertTrue(idx + " expected initialized", rowValue.isInitialized(idx));
            assertNull(idx + " expected null", rowValue.getFieldData(idx));
        }
    }

    @Test
    public void initializeFieldsMarksUninitializedFieldsAsInitialized_partiallyUninitialized() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();

        RowValue rowValue = RowValue.defaultFor(rowDescriptor);
        rowValue.setFieldData(0, new byte[] { 0 });
        rowValue.setFieldData(2, new byte[] { 2 });
        rowValue.initializeFields();

        assertTrue("0 expected initialized", rowValue.isInitialized(0));
        assertArrayEquals("0 unexpected", new byte[] { 0 }, rowValue.getFieldData(0));
        assertTrue("1 expected initialized", rowValue.isInitialized(1));
        assertNull("1 expected null", rowValue.getFieldData(1));
        assertTrue("2 expected initialized", rowValue.isInitialized(2));
        assertArrayEquals("2 unexpected", new byte[] { 2 }, rowValue.getFieldData(2));
    }

    @Test
    public void deepCopy_empty_returns_EMPTY_ROW_VALUE() {
        assertSame(RowValue.EMPTY_ROW_VALUE, RowValue.EMPTY_ROW_VALUE.deepCopy());
    }

    @Test
    public void deepCopy_clonesByteData() {
        RowValue original = RowValue.of(new byte[] { 0 }, new byte[] { 1 }, new byte[] { 2 });

        RowValue rowValue = original.deepCopy();

        assertEquals(3, rowValue.getCount());
        for (int idx = 0; idx < 3; idx++) {
            assertTrue(idx + " expected initialized", rowValue.isInitialized(idx));
            assertArrayEquals(idx + " unexpected value", original.getFieldData(idx), rowValue.getFieldData(idx));
            assertNotSame(idx + " expected cloned value", original.getFieldData(idx), rowValue.getFieldData(idx));
        }
    }

    @Test
    public void deepCopy_retainsUninitializedState() {
        RowDescriptor rowDescriptor = createTestRowDescriptor();

        RowValue original = RowValue.defaultFor(rowDescriptor);
        original.setFieldData(0, new byte[] { 0 });
        original.setFieldData(2, new byte[] { 2 });

        RowValue rowValue = original.deepCopy();

        assertTrue("0 expected initialized", rowValue.isInitialized(0));
        assertArrayEquals("0 unexpected", original.getFieldData(0), rowValue.getFieldData(0));
        assertNotSame("0 expected cloned value", original.getFieldData(0), rowValue.getFieldData(0));
        assertFalse("1 expected not initialized", rowValue.isInitialized(1));
        assertNull("1 expected null", rowValue.getFieldData(1));
        assertTrue("2 expected initialized", rowValue.isInitialized(2));
        assertArrayEquals("2 unexpected", original.getFieldData(2), rowValue.getFieldData(2));
        assertNotSame("2 expected cloned value", original.getFieldData(2), rowValue.getFieldData(2));
    }

    private static RowDescriptor createTestRowDescriptor() {
        RowDescriptorBuilder builder = new RowDescriptorBuilder(TEST_FIELD_DESCRIPTORS.size(), datatypeCoder);
        for (FieldDescriptor fieldDescriptor : TEST_FIELD_DESCRIPTORS) {
            builder.addField(fieldDescriptor);
        }
        return builder.toRowDescriptor();
    }
}