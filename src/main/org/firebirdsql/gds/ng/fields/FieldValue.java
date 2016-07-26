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

import org.firebirdsql.jdbc.field.FieldDataProvider;

/**
 * Holder object for the value of a (statement) parameter or result set field.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class FieldValue implements FieldDataProvider, Cloneable {

    private byte[] fieldData;
    private boolean initialized;
    private Object cachedObject;

    /**
     * Creates an uninitialized FieldValue instance.
     */
    public FieldValue() {
        this(null, false);
    }

    /**
     * Creates an initialized FieldValue instance with the supplied <code>fieldData</code>.
     *
     * @param fieldData
     *         Byte array with the value encoded as required by the type described in <code>fieldDescriptor</code>
     */
    public FieldValue(final byte[] fieldData) {
        this(fieldData, true);
    }

    /**
     * Creates a FieldValue instance with the supplied <code>fieldData</code> and <code>initialized</code> value.
     *
     * @param fieldData
     *         Byte array with the value encoded as required by the type described in <code>fieldDescriptor</code>
     * @param initialized
     *         Is this field in the initialized state
     */
    private FieldValue(final byte[] fieldData, final boolean initialized) {
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
     * Clones this <code>FieldValue</code> instance.
     * <p>
     * The contained field data is cloned as well, but the cached object is identical.
     * </p>
     *
     * @return Clone of this instance
     */
    @Override
    public FieldValue clone() {
        try {
            FieldValue clonedFieldValue = (FieldValue) super.clone();
            if (fieldData != null) {
                clonedFieldValue.fieldData = fieldData.clone();
            }
            return clonedFieldValue;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Unexpected (and impossible) CloneNotSupportedException");
        }
    }
}
