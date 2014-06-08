/*
 * $Id$
 *
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

import org.firebirdsql.jdbc.field.FieldDataProvider;

/**
 * Holder object for the value of a (statement) parameter or result set field.
 * <p>
 * TODO: Consider revision, see for example .NET provider where DbValue stores the (decoded) object instead of the encoded byte array
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class FieldValue implements FieldDataProvider, Cloneable {

    private final FieldDescriptor fieldDescriptor;
    private byte[] fieldData;
    private boolean initialized;
    private Object cachedObject;

    /**
     * Creates an uninitialized FieldValue instance with the supplied {@link FieldDescriptor}.
     *
     * @param fieldDescriptor
     *         <code>FieldDescriptor</code> object.
     */
    public FieldValue(FieldDescriptor fieldDescriptor) {
        this(fieldDescriptor, null, false);
    }

    /**
     * Creates an initialized FieldValue instance with the supplied {@link FieldDescriptor} and <code>fieldData</code>.
     *
     * @param fieldDescriptor
     *         <code>FieldDescriptor</code> object.
     * @param fieldData
     *         Byte array with the value encoded as required by the type described in <code>fieldDescriptor</code>
     */
    public FieldValue(final FieldDescriptor fieldDescriptor, final byte[] fieldData) {
        this(fieldDescriptor, fieldData, true);
    }

    /**
     * Creates a FieldValue instance with the supplied {@link FieldDescriptor} and <code>fieldData</code> and <code>initialized</code> value.
     *
     * @param fieldDescriptor
     *         <code>FieldDescriptor</code> object.
     * @param fieldData
     *         Byte array with the value encoded as required by the type described in <code>fieldDescriptor</code>
     * @param initialized
     *         Is this field in the initialized state
     */
    private FieldValue(final FieldDescriptor fieldDescriptor, final byte[] fieldData, final boolean initialized) {
        this.fieldDescriptor = fieldDescriptor;
        // TODO Defensively copy fieldData?
        this.fieldData = fieldData;
        this.initialized = initialized;
    }

    @Override
    public byte[] getFieldData() {
        return fieldData;
    }

    @Override
    public void setFieldData(byte[] fieldData) {
        this.fieldData = fieldData;
        initialized = true;
    }

    /**
     * @return Cached object
     */
    public Object getCachedObject() {
        return cachedObject;
    }

    /**
     * Stores a cached object in this field.
     * <p>
     * This is mostly used to store blob data for batched execution.
     * </p>
     *
     * @param cachedObject
     *         Object to cache
     */
    public void setCachedObject(Object cachedObject) {
        this.cachedObject = cachedObject;
        initialized = true;
    }

    /**
     * Is this field in an initialized state (meaning: was it explicitly set to a value (or null)).
     *
     * @return <code>true</code> if initialized, <code>false</code> otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Resets this field to an uninitialized state.
     */
    public void reset() {
        initialized = false;
        fieldData = null;
        cachedObject = null;
    }

    /**
     * @return The field descriptor instance of this field.
     */
    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }

    public FieldValue clone() {
        try {
            FieldValue clonedFieldValue = (FieldValue) super.clone();
            if (fieldData != null) {
                clonedFieldValue.fieldData = fieldData.clone();
            }
            return clonedFieldValue;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Unexpected (and impossible) CloneNotSupportedException", e);
        }
    }
}
